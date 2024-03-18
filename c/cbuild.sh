#! /usr/bin/sh

(
    cd /home/amkhrjee/Code/loxlang/c || exit
    gcc -o ./build/clox ./src/*.c
    cd ./build || exit 
    ./clox 
)