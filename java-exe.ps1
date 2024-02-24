$compile = "javac -Xlint:all -d .\bin\ .\src\*.java .\src\Operations\*.java .\src\Utils\*.java "
$createJar = "jar -cfm JavaBuild.jar Manifesto.txt -C .\bin\ ."
$javaCommand = "java -jar JavaBuild.jar"
$runCommand = "$compile" + " && " + "$createJar" + " && " +"$javaCommand"
Invoke-Expression $runCommand
