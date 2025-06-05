$srcClases = ".\src\*.\src\Operations\*.\src\Utils\*"
$libFiles = ""
$compile = "javac -Werror -Xlint:all -d .\target\ $srcClases"
$createJar = "jar -cfm JavaBuild.jar Manifesto.txt -C .\bin\ ."
$javaCommand = "java -jar JavaBuild.jar"
$runCommand = "$compile" + " && " + "$createJar" + " && " +"$javaCommand"
Invoke-Expression $runCommand 
