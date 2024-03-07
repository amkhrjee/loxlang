#ifndef clox_chunk_h
#define clox_chunk_h

#include "value.h"
#include "common.h"

// Chunk = sequence of bytecodes

typedef enum
{
    OP_CONSTANT, // produces a particular constant
    OP_RETURN,
} OpCode;

typedef struct
{
    int count;
    int capacity;
    uint8_t *code;
    ValueArray constants;
} Chunk;

void initChunk(Chunk *chunk);
void freeChunk(Chunk *chunk);
void writeChunk(Chunk *chunk, uint8_t byte);
int addConstant(Chunk *chunk, Value value);

#endif
