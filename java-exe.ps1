$clases = " .\src\*.java .\src\Utils\*.java .\src\Operations\*.java"
$compile = "javac -d .\bin\"  + "$clases"
$jarFile = "jar -cfm JavaBuild.jar Manifesto.txt -C .\bin\ ."
$javaRun = "java -jar JavaBuild.jar"

$runCommand = "$compile" + " && " + "$jarFile" + " && " + "$javaRun"

Invoke-Expression $runCommand
