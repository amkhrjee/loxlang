package com.amkhrjee.lox;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import com.amkhrjee.lox.Expr.Get;
import com.amkhrjee.lox.Expr.Set;
import com.amkhrjee.lox.Expr.Super;
import com.amkhrjee.lox.Expr.This;
import com.amkhrjee.lox.Stmt.Class;
import com.amkhrjee.lox.Stmt.Function;

// The job of the Resolver is to tell the Interpreter the number of hops
// its should make in the list of "scopes" for a particular variable.

class Resolver implements Expr.Visitor<Void>, Stmt.Visitor<Void> {
    private final Interpreter interpreter;
    // Whereas scopes are implemented as Linked List in the Interpreter,
    // here in the Resolver they are implemented as Stack for Semantic Analysis
    private final Stack<Map<String, Boolean>> scopes = new Stack<>();
    private FunctionType currentFunction = FunctionType.NONE;
    private ClassType currentClass = ClassType.NONE;

    Resolver(Interpreter interpreter) {
        this.interpreter = interpreter;
    }

    private enum FunctionType {
        NONE,
        METHOD,
        FUNCTION,
        INITIALIZER
    }

    private enum ClassType {
        NONE,
        CLASS,
        SUBCLASS
    }

    // Utils
    private void resolveLocal(Expr expr, Token name) {
        for (int i = scopes.size() - 1; i >= 0; i--) {
            if (scopes.get(i).containsKey(name.lexeme)) {
                interpreter.resolve(expr, scopes.size() - 1 - i);
                return;
            }
        }
    }

    // Pushing a new scope to the stack of scopes
    private void beginScope() {
        scopes.push(new HashMap<String, Boolean>());
    }

    // Resolving each statement in the scope
    void resolve(List<Stmt> statements) {
        for (Stmt statement : statements) {
            resolve(statement);
        }
    }

    // Removing the scope from the stack after the ??? <<--- Learn about this
    private void endScope() {
        scopes.pop();
    }

    // Resolving statements
    private void resolve(Stmt statement) {
        statement.accept(this);
    }

    // Resolving Expressions
    private void resolve(Expr expression) {
        expression.accept(this);
    }

    // Visiting Nodes in the Syntax Tree

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        resolve(stmt.expression);
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        resolve(stmt.expression);
        return null;
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        declare(stmt.name);
        if (stmt.initializer != null)
            resolve(stmt.initializer);

        define(stmt.name);
        return null;
    }

    private void define(Token name) {
        if (scopes.isEmpty())
            return;
        scopes.peek().put(name.lexeme, true);
    }

    private void declare(Token name) {
        if (scopes.isEmpty())
            return;

        Map<String, Boolean> scope = scopes.peek();
        if (scope.containsKey(name.lexeme))
            Lox.error(name, "Already a variable with this same name in scope.");
        scope.put(name.lexeme, false);
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        beginScope();
        resolve(stmt.statements);
        endScope();
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        resolve(stmt.condition);
        resolve(stmt.thenBranch);
        if (stmt.elseBranch != null)
            resolve(stmt.elseBranch);
        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        resolve(stmt.condition);
        resolve(stmt.body);
        return null;
    }

    @Override
    public Void visitFunctionStmt(Stmt.Function stmt) {
        declare(stmt.name);
        define(stmt.name);

        resolveFunction(stmt, FunctionType.FUNCTION);
        return null;
    }

    private void resolveFunction(Function function, FunctionType type) {
        FunctionType enclosingFunction = currentFunction;
        currentFunction = type;

        beginScope();
        for (Token param : function.params) {
            declare(param);
            define(param);
        }

        resolve(function.body);
        endScope();

        currentFunction = enclosingFunction;
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt) {
        if (currentFunction == FunctionType.NONE)
            Lox.error(stmt.keyword, "Can't return from top-level code");

        if (stmt.value != null) {
            if (currentFunction == FunctionType.INITIALIZER)
                Lox.error(stmt.keyword, "Can't return a value from an initializer.");
            resolve(stmt.value);
        }

        return null;
    }

    @Override
    public Void visitBinaryExpr(Expr.Binary expr) {
        resolve(expr.left);
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitGroupingExpr(Expr.Grouping expr) {
        resolve(expr.expression);
        return null;
    }

    @Override
    public Void visitLiteralExpr(Expr.Literal expr) {
        return null;
    }

    @Override
    public Void visitUnaryExpr(Expr.Unary expr) {
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitVariableExpr(Expr.Variable expr) {
        if (!scopes.isEmpty() && scopes.peek().get(expr.name.lexeme) == Boolean.FALSE)
            Lox.error(expr.name, "Can't read local variable in its own initializer.");

        resolveLocal(expr, expr.name);
        return null;
    }

    @Override
    public Void visitAssignExpr(Expr.Assign expr) {
        resolve(expr.value);
        resolveLocal(expr, expr.name);
        return null;
    }

    @Override
    public Void visitLogicalExpr(Expr.Logical expr) {
        resolve(expr.left);
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitCallExpr(Expr.Call expr) {
        resolve(expr.callee);

        for (Expr argument : expr.arguments)
            resolve(argument);

        return null;
    }

    @Override
    public Void visitClassStmt(Class stmt) {
        ClassType enclosingClass = currentClass;
        currentClass = ClassType.CLASS;

        declare(stmt.name);
        define(stmt.name);

        if (stmt.superclass != null && stmt.name.lexeme.equals(stmt.superclass.name.lexeme)) {
            Lox.error(stmt.superclass.name, "A class can't inherit from itself.");
        }

        // I don't understand why we are using repeated if checks
        // for the same condition, but this is how it is done
        // in the original implementation:
        // https://github.com/munificent/craftinginterpreters/blob/master/java/com/craftinginterpreters/lox/Resolver.java#L70
        // Has this something to do with "separation of concerns"?
        // If you are reading this and you know the reason
        // please make a PR with the explanation here

        if (stmt.superclass != null) {
            currentClass = ClassType.SUBCLASS;
            resolve(stmt.superclass);
        }

        if (stmt.superclass != null) {
            beginScope();
            scopes.peek().put("super", true);
        }

        beginScope();
        scopes.peek().put("this", true);

        for (Stmt.Function method : stmt.methods) {
            FunctionType declaration = FunctionType.METHOD;

            if (method.name.lexeme.equals("init"))
                declaration = FunctionType.INITIALIZER;

            resolveFunction(method, declaration);
        }

        endScope();

        if (stmt.superclass != null)
            endScope();

        currentClass = enclosingClass;
        return null;
    }

    @Override
    public Void visitGetExpr(Get expr) {
        resolve(expr.object);
        return null;
    }

    @Override
    public Void visitSetExpr(Set expr) {
        resolve(expr.value);
        resolve(expr.object);
        return null;
    }

    @Override
    public Void visitThisExpr(This expr) {
        if (currentClass == ClassType.NONE) {
            Lox.error(expr.keyword, "Can't use 'this' outside of a class.");
            return null;
        }

        resolveLocal(expr, expr.keyword);
        return null;
    }

    @Override
    public Void visitSuperExpr(Super expr) {
        if (currentClass == ClassType.NONE)
            Lox.error(expr.keyword, "Can't use 'super' outside of a class.");
        else if (currentClass != ClassType.SUBCLASS) {
            Lox.error(expr.keyword, "Can't use 'super' in a class with no superclass.");
        }
        resolveLocal(expr, expr.keyword);
        return null;
    }
}