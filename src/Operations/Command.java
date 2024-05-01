package Operations;

import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

import java.io.File;
import java.io.IOException;

import Utils.CommandUtils;
import Utils.FileUtils;
public class Command {

    private String localPath;
    private CommandUtils commandUtils;
    private FileUtils fileUtils;
    public Command(String localPath) {
        this.localPath = localPath;
        commandUtils = new CommandUtils(localPath);
        fileUtils = new FileUtils(localPath);
    }

    public String getCompileCommand(String target) {
        // create jar files command for compile operation
        StringBuffer 
            libFiles = new StringBuffer(),
            cLibFiles = new StringBuffer(),
            compile = new StringBuffer();

        List<String> libJars = commandUtils.getLibFiles();
        // lib files
        libFiles.append(
                libJars
                .stream()
                .filter(e -> !e.isEmpty())
                .map(e -> e + ";")
                .collect(Collectors.joining())
        );
        String
            srcClases = commandUtils.getSrcClases(),
            mainClass = commandUtils.getMainClass(),
            format = commandUtils.compileFormatType(target);

        if(!mainClass.isEmpty() && libFiles.isEmpty()) {
            compile.append(format);
            compile.append(".\\src\\*.java -sourcepath .\\src\\");
        } else if(!mainClass.isEmpty() && !libFiles.isEmpty()) {
            String cb = libFiles.substring(0, libFiles.length()-1);
            cLibFiles.append("'" + cb + "' " + srcClases);
            compile.append(format);
            compile.append(cLibFiles);
            compile.append(" .\\src\\*.java -sourcepath .\\src\\");
        } else if(mainClass.isEmpty() && libFiles.isEmpty()) {
            compile.append(format);
             compile.append(srcClases);
        } else if(mainClass.isEmpty() && !libFiles.isEmpty()) {
            String cb = libFiles.substring(0, libFiles.length()-1);
            cLibFiles.append("'" + cb + "' " + srcClases);
            compile.append(format);
            compile.append(cLibFiles);
        }
        return compile.toString();
    }
    public List<String> getExtractionsCommand() throws IOException {
        File extractionFile = new File(localPath + "\\extractionFiles");
        List<String> commands = new ArrayList<>();

        fileUtils.listFilesFromPath(extractionFile.getPath())
            .parallelStream()
            .filter(e -> e.getName().contains(".jar"))
            .forEach(e -> {
                String 
                    jarFileName = e.getName(),
                    jarParent   = e.getParent(),
                    extracJAR   = "jar -xf " + jarFileName,
                    deleteJAR   = "rm -r " + jarFileName + "\n";
                commands.add("cd " + jarParent + " && " + extracJAR + " && " + deleteJAR);
            });
        return commands;
    }
    public String getJarFileCommand(boolean includeExtraction) throws IOException {
        String
            command = "",
            directory = "";

        File extractionFile = new File(localPath + "\\extractionFiles");

        if(extractionFile.listFiles() != null) {
            for(File extractionDir: extractionFile.listFiles()) {
                directory += " -C " + extractionDir.getPath() + "\\ .";
            }
        } 
        if(includeExtraction) {
            command = commandUtils.jarTypeUnion(directory);
        } else {
            command = commandUtils.jarTypeUnion("");
        }
        return command;
    }
    public String getRunCommand(List<String> libJars, String className) {
        String command  = "";
        StringBuffer 
            jarFiles = new StringBuffer(),
            runClass = commandUtils.runClassOption(className);
        jarFiles.append("'.\\bin\\;");
        jarFiles.append(libJars
                .parallelStream()
                .filter(e -> !e.isEmpty())
                .map(e -> e + ";")
                .collect(Collectors.joining())
        );
        if(jarFiles.isEmpty()) {
            command = "java -XX:+ExtensiveErrorReports -d .\\bin\\" + runClass;
        } else {
            String cleanLibs = jarFiles.substring(0, jarFiles.length()-1) + "'";
            command = "java -XX:+ExtensiveErrorReports -cp " + cleanLibs + runClass;
        }
        return command;
    }

}
