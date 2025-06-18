$srcClases = ".\src\application\*.java src\application\*.java src\application\builders\*.java src\application\operations\*.java src\application\utils\*.java "
$libFiles = ""
$compile = "javac -Werror -Xlint:all -Xdiags:verbose -d .\bin\ $srcClases"
$createJar = "jar -cfm JavaBuild.jar Manifesto.txt -C .\bin\ ."
$javaCommand = "java -jar JavaBuild.jar"
$runCommand = "$compile" + " && " + "$createJar" + " && " +"$javaCommand"
Invoke-Expression $runCommand 
