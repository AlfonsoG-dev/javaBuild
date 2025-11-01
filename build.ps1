$srcClases = "src\Application\*.java src\Application\builders\*.java src\Application\models\*.java src\Application\operations\*.java src\Application\utils\*.java "
$libFiles = ""
$compile = "javac --release 23 -Werror -Xlint:all -d .\bin\ $srcClases"
$createJar = "jar -cfm JavaBuild.jar Manifesto.txt -C .\bin\ ."
$javaCommand = "java -jar JavaBuild.jar --compile"
$runCommand = "$compile" + " && " + "$createJar" + " && " +"$javaCommand"
Invoke-Expression $runCommand 
