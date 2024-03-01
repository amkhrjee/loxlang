javac .\java\com\amkhrjee\lox\*.java -d .\build\classes\ 
cd .\build\classes\
jar cmvf ..\..\java\META-INF\MANIFEST.INF ..\..\bin\jlox.jar .