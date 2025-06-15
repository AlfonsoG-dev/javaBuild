srcClases="./src/application/*.java ./src/application/builders/*.java ./src/application/operations/*.java ./src/application/utils/*.java "
libFiles=""
javac -Werror -Xlint:all -d ./bin/ $srcClases
jar -cfm JavaBuild.jar Manifesto.txt -C ./bin/ .
java -jar JavaBuild.jar
