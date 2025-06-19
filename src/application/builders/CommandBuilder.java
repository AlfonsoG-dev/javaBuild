package builders;

import java.util.List;
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
     * get the compile command from the compile model
     * @param source where the .java files are.
     * @param target where to store the .class files 
     * @param flags the compile flags from config file
     * @return the compile command
     */
    public String getCompileCommand(String source, String target, String flags, int release) {
        return new CompileModel(source, target, flags).getCompileCommand(release);
    }
    /**
     * create a list of jar files to extract for the build process.
     * <br/>pre The extraction jars are the .jar files in the lib folder.
     * @return the list of jar files to extract.
     */
    public List<String> getExtractionsCommand() {
        File extractionFile = fileUtils.resolvePaths(localPath, "extractionFiles");
    
        return executor.executeConcurrentCallableList(fileUtils.listFilesFromPath(extractionFile.getPath()))
            .stream()
            .filter(file -> file.isFile() && file.getName().endsWith(".jar"))
            .map(file -> {
                String jarFileName = file.getName();
                String jarParent   = file.getParent();
                String extractJAR  = "jar -xf " + jarFileName;
                String deleteJAR   = "rm -r " + jarFileName;
                return "cd " + jarParent + " && " + extractJAR + " && " + deleteJAR;
            })
            .toList();
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
        if(className.equals(" ")) {
            return null;
        }

        if(jarFiles.isEmpty()) {
            command = "java -cp " + target + className;
        } else {
            jarFiles.append("'");
            jarFiles.append(target);
            jarFiles.append(";");
            jarFiles.append(libJars
                    .stream()
                    .map(e -> e + ";")
                    .collect(Collectors.joining())
            );
            String cleanLibs = jarFiles.substring(0, jarFiles.length()-1) + "'";
            command = "java -cp " + cleanLibs + className;
        }
        return command;
    }

}
