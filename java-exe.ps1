$srcClases = ".\src\*.java .\src\Operations\*.java .\src\Utils\*.java "
$libFiles = ""
$compile = "javac -Werror -Xlint:all -d .\bin\ $srcClases"
$createJar = "jar -cfm JavaBuild.jar Manifesto.txt -C .\bin\ ."
$javaCommand = "java -jar JavaBuild.jar"
$runCommand = "$compile" + " && " + "$createJar" + " && " +"$javaCommand"
Invoke-Expression $runCommand 
