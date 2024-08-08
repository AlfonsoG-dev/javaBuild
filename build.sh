srcClases="./src/*.java ./src/Operations/*.java ./src/Utils/*.java "
libFiles=""
javac -Werror -Xlint:all -d ./bin/ $srcClases
jar -cfm JavaBuild.jar Manifesto.txt -C ./bin/ .
java -jar JavaBuild.jar
