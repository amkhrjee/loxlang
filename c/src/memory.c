#include <stdlib.h>
#include "memory.h"

void *reallocate(void *pointer, size_t oldSize, size_t newSize)
{
    // we handle the simple case
    if (newSize == 0)
    {
        free(pointer);
        return NULL;
    }

    // we let the C standard function handle the rest
    void *result = realloc(pointer, newSize);
    if (result == NULL)
        exit(1);
    return result;
}