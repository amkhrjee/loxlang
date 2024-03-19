#ifndef clox_object_h
#define clox_object_h

#include "common.h"
#include "value.h"

#define OBJ_TYPE(value) (AS_OBJ(value)->type)
#define IS_STRING(value) isObjType(value, OBJ_STRING)
#define AS_STRING(value) ((ObjString *)AS_OBJ(value))
#define AS_CSTRING(value) (((ObjString *)AS_OBJ(value))->chars)

typedef enum
{
    OBJ_STRING,
} ObjType;

struct Obj
{
    ObjType type;
    struct Obj *next; // Linked List node for garbage collection
};

// "Struct Inheritance"
struct ObjString
{
    Obj obj;
    int length;
    char *chars;
};

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