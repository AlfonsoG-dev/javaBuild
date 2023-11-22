$clases = " .\src\*.java .\src\Utils\*.java .\src\Operations\*.java"
$compile = "javac -d .\bin\ " + "$clases"
$createJar = "jar -cfm JavaBuild.jar Manifesto.txt -C .\bin\ ."
$javaCommand = "java -jar JavaBuild.jar"
$runCommand = "$compile"  + " && " + "$createJar" + " && " + "$javaCommand"
Invoke-Expression $runCommand
