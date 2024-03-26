#! /usr/bin/sh

(
    javac com/amkhrjee/lox/*.java -d ./build/classes
    cd ./build/classes || exit
    jar cmvf ../java/META-INF/MANIFEST.INF ../../bin/jlox.jar .
)