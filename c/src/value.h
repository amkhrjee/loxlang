#ifndef clox_value_h
#define clox_value_h

#include "common.h"

typedef enum
{
    VAL_BOOL,
    VAL_NIL,
    VAL_NUMBER
} ValueType;

// We're calling this "tagged union"
typedef struct
{
    ValueType type;
    union
    {
        bool boolean;
        double number;
    } as;
} Value;

// Type checking to be done before retrieval
#define IS_BOOL(value) ((value).type == VAL_BOOL)
#define IS_NIL(value) ((value).type == VAL_NIL)
#define IS_NUMBER(value) ((value).type == VAL_NUMBER)

// Retrieving a Lox value into a C value
#define AS_BOOL(value) ((value).as.boolean)
#define AS_NUMBER(value) ((value).as.number)

// Storing a C value into a Lox value
#define BOOL_VAL(value) ((Value){VAL_BOOL, {.boolean = value}})
#define NIL_VAL(value) ((Value){VAL_NIL, {.number = 0}})
#define NUMBER_VAL(value) ((Value){VAL_NUMBER, {.number = value}})

typedef struct
{
    int capacity;
    int count;
    Value *values;
} ValueArray;

void initValueArray(ValueArray *array);
void writeValueArray(ValueArray *array, Value value);
void freeValueArray(ValueArray *array);
void printValue(Value value);

#endif