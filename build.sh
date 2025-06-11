srcClases="./src/*.java ./src/Application/Builders/*.java ./src/Application/Operations/*.java ./src/Application/Utils/*.java "
libFiles=""
javac -Werror -Xlint:all -d ./bin/ $srcClases
jar -cfm JavaBuild.jar Manifesto.txt -C ./bin/ .
java -jar JavaBuild.jar
