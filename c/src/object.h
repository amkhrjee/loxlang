#ifndef clox_object_h
#define clox_object_h

#include "common.h"
#include "chunk.h"
#include "value.h"

#define OBJ_TYPE(value) (AS_OBJ(value)->type)

#define IS_NATIVE(value) isObjType(value, OBJ_NATIVE)
#define IS_STRING(value) isObjType(value, OBJ_STRING)
#define IS_CLOSURE(value) isObjType(value, OBJ_CLOSURE)
#define IS_FUNCTION(value) isObjType(value, OBJ_FUNCTION)

#define AS_STRING(value) ((ObjString *)AS_OBJ(value))
#define AS_CLOSURE(value) ((ObjClosure *)AS_OBJ(value))
#define AS_FUNCTION(value) ((ObjFunction *)AS_OBJ(value))
#define AS_CSTRING(value) (((ObjString *)AS_OBJ(value))->chars)
#define AS_NATIVE(value) (((ObjNative *)AS_OBJ(value))->function)

typedef enum
{
    OBJ_NATIVE,
    OBJ_STRING,
    OBJ_CLOSURE,
    OBJ_FUNCTION,
} ObjType;

struct Obj
{
    ObjType type;
    struct Obj *next; // Linked List node for garbage collection
};

typedef struct
{
    Obj obj; // <- enables struct inheritance
    int arity;
    int upvalueCount;
    Chunk chunk;
    ObjString *name;
} ObjFunction;

typedef Value (*NativeFn)(int argCount, Value *args);

typedef struct
{
    Obj obj;
    NativeFn function;
} ObjNative;

struct ObjString
{
    Obj obj;
    int length;
    char *chars;
    uint32_t hash;
};

// Every function is a closure object even if it doesn't close over
typedef struct
{
    Obj obj;
    ObjFunction *function;
} ObjClosure;

ObjClosure *newClosure(ObjFunction *function);
ObjFunction *newFunction();
ObjNative *newNative(NativeFn function);

// Takes ownership of the string passed in
ObjString *takeString(char *chars, int length);
// Does not take ownership of the string you passed in
ObjString *copyString(const char *chars, int length);
void printObject(Value value);

/*
 Why not a macro?
 ---------------------
It’s because the body uses value twice.
A macro is expanded by inserting the argument expression
every place the parameter name appears in the body. If a macro uses a
parameter more than once, that expression gets evaluated multiple times.
*/
static inline bool isObjType(Value value, ObjType type)
{
    return IS_OBJ(value) && AS_OBJ(value)->type == type;
}

#endif