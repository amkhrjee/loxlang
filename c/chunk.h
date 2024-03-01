#ifndef clox_chunk_h
#define clox_chunk_h

#include "common.h"

// Chunk = sequence of bytecodes

typedef enum
{
    OP_RETURN,
} OpCode;

typedef struct
{
    uint8_t *code;
} Chunk;

#endif
