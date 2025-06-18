package builders;

import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

import models.CompileModel;
import operations.ExecutorOperation;

import java.io.File;
import java.io.IOException;

import utils.CommandUtils;
import utils.FileUtils;
public class CommandBuilder {

    private String localPath;
    private CommandUtils commandUtils;
    private FileUtils fileUtils;
    private ExecutorOperation executor;
    public CommandBuilder(String localPath) {
        this.localPath = localPath;
        commandUtils = new CommandUtils(localPath);
        fileUtils = new FileUtils(localPath);
        executor = new ExecutorOperation();
    }
    public CommandBuilder(String localPath, CommandUtils commandUtils, FileUtils fileUtils) {
        this.localPath = localPath;
        this.commandUtils = commandUtils;
        this.fileUtils = fileUtils;
        executor = new ExecutorOperation();
    }

    /**
     * create the compile command using lib files and src files to build.
     * @param target its the folder/directory to allocate the .class files.
     * @return the compile command
     */
    public String getCompileCommand(String source, String target, String flags, int release) {
        return new CompileModel(source, target, flags).getCompileCommand(release);
    }
    /**
     * create a list of jar files to extract for the build process.
     * <br/><b>pre: </b> The extraction jars are the .jar files in the lib folder.
     * @return the list of jar files to extract.
     */
    public List<String> getExtractionsCommand() throws IOException {
        File extractionFile = fileUtils.resolvePaths(localPath, "extractionFiles");
        List<String> commands = new ArrayList<>();

        executor.executeConcurrentCallableList(fileUtils.listFilesFromPath(extractionFile.getPath()))
            .stream()
            .filter(e -> e.getName().contains(".jar"))
            .forEach(e -> {
                String 
                    jarFileName = e.getName(),
                    jarParent   = e.getParent(),
                    extractJAR   = "jar -xf " + jarFileName,
                    deleteJAR   = "rm -r " + jarFileName + "\n";
                commands.add("cd " + jarParent + " && " + extractJAR + " && " + deleteJAR);
            });
        return commands;
    }
    /**
     * create the jar command for the build process.
     * <br/><b>pre: </b> use the Manifesto, main class files to create the java cli command for the jar file.
     * @param includeExtraction boolean value that indicates if you want to include or not the lib files in the build.
     * @param source where the lib files are.
     * @return the jar command for the build process.
     */
    public String getJarFileCommand(boolean includeExtraction, String source, String target) throws IOException {
        String
            command = "",
            directory = "";

        File extractionFile = fileUtils.resolvePaths(localPath, "extractionFiles");

        if(extractionFile.listFiles() != null) {
            for(File extractionDir: extractionFile.listFiles()) {
                directory += " -C " + extractionDir.getPath() + File.separator + " .";
            }
        } 
        if(includeExtraction) {
            command = commandUtils.jarTypeUnion(directory, source, target);
        } else {
            command = commandUtils.jarTypeUnion("", source, target);
        }
        return command;
    }
    /**
     * creates the run command or the execute command.
     * @param libJars a list of jar files of lib folder.
     * @param className the main class name.
     * @param source the source folder of the .class files.
     * @return the run or execute command.
     */
    public String getRunCommand(List<String> libJars, String className, String source, String target) {
        String command  = "";
        StringBuffer jarFiles = new StringBuffer();
        String runClass = commandUtils.runClassOption(className, source);

        if(jarFiles.isEmpty()) {
            command = "java -cp " + target + runClass;
        } else {
            jarFiles.append("'");
            jarFiles.append(target);
            jarFiles.append(";");
            jarFiles.append(libJars
                    .stream()
                    .filter(e -> !e.isEmpty())
                    .map(e -> e + ";")
                    .collect(Collectors.joining())
            );
            String cleanLibs = jarFiles.substring(0, jarFiles.length()-1) + "'";
            command = "java -cp " + cleanLibs + runClass;
        }
        return command;
    }

}
