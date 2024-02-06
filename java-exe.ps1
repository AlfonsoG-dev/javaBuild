$compile = "javac -Xlint:all -d .\bin\ .\src\*.java .\src\Operations\*.java .\src\Utils\*.java "
$createJar = "jar -cfm javaBuild.jar Manifesto.txt -C .\bin\ ."
$javaCommand = "java -jar javaBuild.jar"
$runCommand = "$compile" + " && " + "$createJar" + " && " +"$javaCommand"
Invoke-Expression $runCommand