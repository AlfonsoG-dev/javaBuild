javac -Werror -Xlint:all -d .\bin\ .\src\*.java .\src\Operations\*.java .\src\Utils\*.java
jar -cfm JavaBuild.jar Manifesto.txt -C .\bin\ .
java -jar JavaBuild.jar
