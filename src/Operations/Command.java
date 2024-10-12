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

    /**
     * create the compile command using lib files and src files to build.
     * @param target its the folder/directory to allocate the .class files.
     * @return the compile command
     */
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
            compile.append(srcClases);
        } else if(!mainClass.isEmpty() && !libFiles.isEmpty()) {
            String cb = libFiles.substring(0, libFiles.length()-1);
            cLibFiles.append("'" + cb + "' ");
            compile.append(format);
            compile.append(cLibFiles);
            compile.append(srcClases);
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
    /**
     * create a list of jar files to extract for the build process.
     * <br/><b>pre: </b> The extraction jars are the .jar files in the lib folder.
     * @return the list of jar files to extract.
     */
    public List<String> getExtractionsCommand() throws IOException {
        File extractionFile = new File(localPath + File.separator + "extractionFiles");
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
    /**
     * create the jar command for the build process.
     * <br/><b>pre: </b> use the Manifesto, main class files to create the java cli command for the jar file.
     * @param includeExtraction boolean value that indicates if you want to include or not the lib files in the build.
     * @param source where the lib files are.
     * @return the jar command for the build process.
     */
    public String getJarFileCommand(boolean includeExtraction, String source) throws IOException {
        String
            command = "",
            directory = "";

        File extractionFile = new File(localPath + File.separator + "extractionFiles");

        if(extractionFile.listFiles() != null) {
            for(File extractionDir: extractionFile.listFiles()) {
                directory += " -C " + extractionDir.getPath() + File.separator + " .";
            }
        } 
        if(includeExtraction) {
            command = commandUtils.jarTypeUnion(directory, source);
        } else {
            command = commandUtils.jarTypeUnion("", source);
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
    public String getRunCommand(List<String> libJars, String className, String source) {
        String command  = "";
        StringBuffer 
            jarFiles = new StringBuffer(),
            runClass = commandUtils.runClassOption(className);
        if(source.isEmpty()) {
            source = "." + File.separator + "bin" + File.separator + ";";
        } else {
            source = new File(source).getPath() + File.separator + ";";
        }
        jarFiles.append("'");
        jarFiles.append(source);
        jarFiles.append(libJars
                .parallelStream()
                .filter(e -> !e.isEmpty())
                .map(e -> e + ";")
                .collect(Collectors.joining())
        );
        if(jarFiles.isEmpty()) {
            command = "java -d " + source + runClass;
        } else {
            String cleanLibs = jarFiles.substring(0, jarFiles.length()-1) + "'";
            command = "java -cp " + cleanLibs + runClass;
        }
        return command;
    }

}
