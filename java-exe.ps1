$clases = " .\src\*.java .\src\Utils\*.java .\src\Operations\*.java"
$compile = "javac -d .\bin\" + "$clases"
$jarFile = "jar -cfm test_app.jar Manifesto.txt -C .\bin\ ."
$javaRun = "java -jar test_app.jar"

$runCommand = "$compile" + " && " + "$jarFile" + " && " + "$javaRun"

Invoke-Expression $runCommand
