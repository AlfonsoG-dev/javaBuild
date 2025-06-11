package Application.Builders;

import Application.Utils.FileUtils;

import java.io.File;
import java.io.IOException;

import java.util.List;
import java.util.stream.Collectors;

public class ScriptBuilder {

    private String localPath;
    private FileUtils fileUtils;

    public ScriptBuilder(String nLocalPath) {
        localPath = nLocalPath;
        fileUtils = new FileUtils(nLocalPath);
    }

    private String getRunScriptCommand() {
        String command = "";
        if(System.getProperty("os.name").toLowerCase().contains("windows")) {
            command = "$runCommand = " + "\"$compile\" +" + " \" && \" +" + " \"$createJar\" " +
                "+ \" && \" +" + "\"$javaCommand\"" + "\n";
        }
        return command;
    }
    private String getJavaScriptCommand(String mainClass) {
        String command = "";
        if(System.getProperty("os.name").toLowerCase().contains("windows")) {
            command = "$javaCommand = \"java -jar " + mainClass + "\""  + "\n";
        } else if(System.getProperty("os.name").toLowerCase().contains("linux")) {
            command = "java -jar " + mainClass + "\n";
        }
        return command;
    }
    private String getBuildScriptCommand() {
        String command = "";
        if(System.getProperty("os.name").toLowerCase().contains("windows")) {
            command = "$runCommand = " + "\"$compile\" +" + " \" && \" +" + " \"$createJar\" \n";
        }
        return command;
    }

    public String getScriptLines(String srcClases, String libFiles,
            String compile, String extractJar, String runJar, String runCommand) {
        
        StringBuffer sb = new StringBuffer();
        if(System.getProperty("os.name").toLowerCase().contains("windows")) {
            sb.append(
                "$srcClases = \"" + srcClases + "\"\n" +
                "$libFiles = \"" + libFiles + "\"\n" +
                "$compile = \"" + compile + "\"\n" + 
                "$createJar = " + "\"" + extractJar + "\"" + "\n" + 
                runJar + 
                runCommand +
                "Invoke-Expression $runCommand \n"
            );
        } else if(System.getProperty("os.name").toLowerCase().contains("linux")) {
            sb.append(
                "srcClases=" + "\"" + srcClases + "\"\n" + 
                "libFiles=" + "\"" + libFiles + "\"\n" + 
                compile + "\n" + 
                extractJar + "\n" + 
                runJar
            );
        }
        return sb.toString();
    }

    public void writeManifesto(String libFiles, String authorName, String mainClass, boolean extract) {
        String author = authorName.trim();

        StringBuffer m = new StringBuffer();

        m.append("Manifest-Version: 1.0");
        m.append("\n");

        if(!author.isEmpty()) {
            m.append("Created-By: ");
            m.append(author);
            m.append("\n");
        }
        if(!mainClass.isEmpty()) {
            m.append("Main-Class: ");
            m.append(mainClass);
            m.append("\n");
        }
        if(!libFiles.isEmpty() && !extract) {
            m.append("Class-Path: ");
            m.append(libFiles);
            m.append("\n");
        }

        // write lines to file
        fileUtils.writeToFile(m.toString(), fileUtils.resolvePaths(localPath, "Manifesto.txt").getPath());
    }
    /**
     * create the sentences for the build script
     * @param fileName: path where the build script is created
     * @param mainClass: main class name
     * @param extract: true if you want to include the lib files as part of the jar file, false otherwise
     * @throws IOException: exception while trying to create the build script
     */
    public void writeBuildFile(String fileName, String mainClass, String source, String target, List<String> dirNames,
    List<String> libNames, boolean extract) {
        CommandBuilder cBuilder = new CommandBuilder(localPath);

        StringBuffer sourceFiles = new StringBuffer();
        File sourceDir = fileUtils.resolvePaths(localPath, source);
        if(fileUtils.countFilesInDirectory(sourceDir) > 0) {
            sourceFiles.append(sourceDir + File.separator + "*.java ");
        }
        String
            libFiles = "",
            compile = "javac -Werror -Xlint:all -d ." + File.separator + target + File.separator,
            runJar = "",
            runCommand = "";

        sourceFiles.append(
                dirNames
                .parallelStream()
                .filter(e -> fileUtils.countFilesInDirectory(new File(e)) > 0)
                .map(e -> e + "*.java ")
                .collect(Collectors.joining())
        );


        libFiles += libNames
            .stream()
            .map(e -> e + ";")
            .collect(Collectors.joining());

        if(!libFiles.isEmpty()) {
            compile += " -cp '$libFiles' $srcClases";
        } else {
            compile += " $srcClases";
        }
        if(!mainClass.isEmpty()) {
            runJar = getJavaScriptCommand(mainClass);
            runCommand = getRunScriptCommand();
        } else {
            runCommand = getBuildScriptCommand();
        }
        try {
            String lines = getScriptLines(
                    sourceFiles.toString(),
                    libFiles,
                    compile,
                    cBuilder.getJarFileCommand(extract, target, source),
                    runJar,
                    runCommand
            );

            File buildFile = fileUtils.resolvePaths(localPath, fileName);
            fileUtils.writeToFile(lines, buildFile.getPath());
            if(buildFile.setExecutable(true)) {
                System.out.println("[Info] change file to executable " + buildFile.getPath());
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
