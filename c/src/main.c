#include "vm.h"
#include "common.h"
#include "chunk.h"
#include "debug.h"
#include <stdio.h>

int main(int argc, const char *argv[])
{
    initVM();
    Chunk chunk;
    initChunk(&chunk);
    // this actually returns the constant's offset in the ValuesArray
    int constant = addConstant(&chunk, 1.2);
    writeChunk(&chunk, OP_CONSTANT, 123);
    writeChunk(&chunk, constant, 123);
    writeChunk(&chunk, OP_RETURN, 123);
    disassembleChunk(&chunk, "test chunk");
    interpret(&chunk);
    freeVM();
    freeChunk(&chunk);
    return 0;
}