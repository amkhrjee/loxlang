#! /usr/bin/sh

(
    javac com/amkhrjee/lox/*.java -d ./build/classes
    cd ./build/classes || exit
    jar cmvf ../../META-INF/MANIFEST.INF ../../../bin/jlox.jar .
)