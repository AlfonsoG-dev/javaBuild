$compile = "javac -Werror -g -Xlint:all -d .\bin\ .\src\*.java -sourcepath .\src\"
$createJar = "jar -cfm JavaBuild.jar Manifesto.txt -C .\bin\ ."
$javaCommand = "java -jar JavaBuild.jar"
$runCommand = "$compile" + " && " + "$createJar" + " && " +"$javaCommand"
Invoke-Expression $runCommand 
