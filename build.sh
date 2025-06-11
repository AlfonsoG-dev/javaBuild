srcClases="./src/main/application/*.java ./src/main/application/builders/*.java ./src/main/application/operations/*.java ./src/main/application/utils/*.java "
libFiles=""
javac -Werror -Xlint:all -d ./bin/ $srcClases
jar -cfm JavaBuild.jar Manifesto.txt -C ./bin/ .
java -jar JavaBuild.jar
