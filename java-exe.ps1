$compile = "javac -Xlint:all -d .\bin\ .\src\*.java .\src\Operations\*.java .\src\Utils\*.java"
$createJar = "jar -cf javaBuild.jar -C .\bin\ ."
$javaCommand = "java -jar .jar"
$runCommand = "$compile" + " && " + "$createJar" + " && " +"$javaCommand"
Invoke-Expression $runCommand
