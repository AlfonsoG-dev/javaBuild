srcClases="src/application/*.java src/application/operations/*.java src/application/utils/*.java src/application/builders/*.java src/application/models/*.java "
libFiles=""
javac --release 23 -Werror -Xlint:all -d ./bin/ $srcClases
jar -cfm JavaBuild.jar Manifesto.txt -C ./bin/ .
java -jar JavaBuild
