package operations;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import java.util.Optional;
import java.util.List;
import java.util.HashMap;


import utils.OperationUtils;
import utils.CommandUtils;
import utils.ModelUtils;

import utils.FileUtils;
import builders.CommandBuilder;
import builders.ConfigBuilder;

/**
 * Its the perform class of the java command
 */
public class Operation {
    private String localPath;
    private OperationUtils operationUtils;
    private FileUtils fileUtils;

    private CommandBuilder cBuilder;
    private ConfigBuilder configBuilder;

    private ModelUtils modelUtils;

    private FileOperation fileOperation;
    private ExecutorOperation executor;

    public Operation(String nLocalPath) {
        localPath = nLocalPath;
        fileUtils = new FileUtils(localPath);
        executor = new ExecutorOperation();

        fileOperation = new FileOperation(nLocalPath, fileUtils);
        cBuilder = new CommandBuilder(nLocalPath, new CommandUtils(nLocalPath), fileUtils);
        operationUtils = new OperationUtils(localPath, fileOperation);
        configBuilder = new ConfigBuilder(localPath, fileUtils, fileOperation);
    }
    private HashMap<String, String> getConfigData() {
        return configBuilder.getConfigValues();
    }
    public void startUpCompileModel() {
        modelUtils = new ModelUtils(
            getConfigData().get("Source-path"),
             getConfigData().get("Class-Path"),
             localPath,
             fileUtils,
             fileOperation,
             executor
        );
    }
    /**
     * creates the folder or directory structure:
     * bin, lib, src, docs, extractionFiles.
     */
    public void createProjectOperation() {
        String[] names = {
            getConfigData().get("Class-Path"),
            "lib",
            getConfigData().get("Source-Path"),
            "docs",
            "extractionFiles"
        };
        System.out.println("[Info] Creating project ...");
        for(String n: names) {
            File f = fileUtils.resolvePaths(localPath, n);
            if(!f.exists() && f.mkdir()) {
                System.out.println("[Info] Created " + f.getPath());
            }
        }
    }
    /**
     * create the config file operation
     * @param source where the .java files are
     * @param target where to store the .class files
     */
    public void createConfigFile(String source, String target) {
        Optional<String> oSource = Optional.ofNullable(source);
        Optional<String> oTarget = Optional.ofNullable(target);

        configBuilder.writeConfigFile(oSource.orElse("src"), oTarget.orElse("bin"));
    }

    /**
     * Helper function that allow to get the author name of the manifesto file.
     * @throws IOException
     */
    public String getAuthorName() {
        String author = null;
        File f = fileUtils.resolvePaths(localPath, "Manifesto.txt");
        if(f.exists()) {
            try (BufferedReader myReader = new BufferedReader(new FileReader(f))){
                while(myReader.ready()) {
                    String lines = myReader.readLine();
                    if(lines.contains("Created-By:")) {
                        author = lines.trim().split(":")[1];
                        break;
                    }
                }
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
        return Optional.ofNullable(author).orElse("System-Owner");
    }
    /**
     * creates the initial files needed in at the start:
     *  Manifesto, gitignore and the mainClass file for java.
     *  @param author its the name of the author for the Manifesto file.
     *  @throws IOException
     */
    public void createFilesOperation(String author, String source, String target) {
        Optional<String> oAuthor = Optional.ofNullable(author);
        oAuthor.ifPresentOrElse(
                value -> System.out.println("[Info] Using Author name " + value),
                () -> System.out.println("[Info] No author provided, now using " + getAuthorName())
        );
        String oSource = Optional.ofNullable(source).orElse(getConfigData().get("Source-Path"));
        String oTarget = Optional.ofNullable(target).orElse(getConfigData().get("Class-Path"));
        operationUtils.createProjectFiles(oAuthor.orElse(getAuthorName()), oSource, oTarget);
    }
    /**
     * used to list the .java or .jar or .class files in the project.
     * @param source its the folder/directory where the files are stored.
     * @throws IOException
     */
    public void listProjectFiles(String source) {
        Optional<String> oSource = Optional.ofNullable(source);
        File read = fileUtils.resolvePaths(localPath, oSource.orElse(getConfigData().get("Source-Path")));
        if(read.isFile()) {
            System.out.println("[Info] Only directory types are allow but here you have it°!");
            System.out.println(read.getPath());
        }
        if(read.isDirectory()) {
            executor.executeConcurrentCallableList(fileUtils.listFilesFromPath(read.getPath()))
                .stream()
                .map(e -> e.getPath())
                .filter(e -> e.contains(".java") || e.contains(".jar") || e.contains(".class"))
                .forEach(e -> {
                    System.out.println(e);
                }
            );
        }
    }
    /**
     * Performs the compile operation using the compile command.
     * @param target the folder/directory where you want to store the .class files. Its ./bin/ by default.
     * @throws Exception when the compile command gets an error.
     */
    public void compileProjectOperation(String source, String target, String release) {
        String javaVersion = System.getProperty("java.specification.version");

        String compileCommand = cBuilder.getCompileCommand(
            Optional.ofNullable(source).orElse(getConfigData().get("Source-Path")),
            Optional.ofNullable(target).orElse(getConfigData().get("Class-Path")),
            Optional.of(getConfigData().get("Compile-Flags")).get(),
            Integer.parseInt(Optional.ofNullable(release).orElse(javaVersion))
        );
        System.out.println("[Info] compile ...");
        operationUtils.executeCommand(compileCommand);
    }
    public void deleteDirectory(String dirPath) {
        System.out.println("[Info] deleting directory...");
        operationUtils.executeCommand(
            "rm -r " + Optional.ofNullable(dirPath).orElse(getConfigData().get("Class-Path"))
        );
    }
    /**
     * Performs the extraction operation using the extract command.
     * Usually you will extract the .jar files stored in the lib folder. 
     * @param extractFile the file to extract.
     * @throws IOException the file doesn't exists
     */
    public void executeExtractionCommand() {
        List<String> extractions = cBuilder.getExtractionsCommand();
        if(!extractions.isEmpty()) {
            for(String e: extractions) {
                operationUtils.executeCommand(e);
            }
        } else {
            System.out.println("[Info] NO EXTRACTION FILES");
        }
    }
    /**
     * helper function to get the lib folder/directory dependency or .jar files
     * @throws IOException
     */
    public void extractJarDependencies() {
        List<String> jars = modelUtils.getLibFiles();
        for(String j: jars) {
            if(!fileOperation.extractionDirContainsPath(j)) {
                System.out.println("[Info] extracting jar dependencies ...");
                operationUtils.createExtractionFiles(jars);
                // the extraction files can be more than 1
                executeExtractionCommand();
            } else {
                System.out.println("[Info] THERE IS NO DEPENDENCIES TO EXTRACT");
            }
        }
    }
    /**
     * Performs create jar operation using the command.
     * @param extract boolean value that indicates if you add or not to the build the lib file.
     * @param source the folder to include in the build.
     * @throws Exception when creating a jar file gets an error.
     */
    public void createJarOperation(boolean extract, String source, String target) {
        try {
            String command = cBuilder.getJarFileCommand(
                extract,
                Optional.ofNullable(source).orElse(getConfigData().get("Class-Path")),
                Optional.ofNullable(target).orElse(getConfigData().get("Source-Path"))
            );
            System.out.println("[Info] creating jar file ...");
            operationUtils.executeCommand(command);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * Helper function that allow to identified if you include or not in the build the lib dependency.
     * By adding the Class-Path property in the Manifesto file you exclude the lib dependency in the build.
     * @throws IOException
     */
    public boolean haveIncludeExtraction() {
        boolean haveInclude = true; 
        File f = fileUtils.resolvePaths(localPath, "Manifesto.txt");
        if(!f.exists()) {
            System.err.println("[Error] Manifesto doesn't exists");
            return false;
        }
        String[] lines = fileUtils.readFileLines(f.getPath()).split("\n");
        for(String l: lines) {
            if(l.contains("Class-Path:")) {
                haveInclude = false;
                break;
            }
        }
        return haveInclude;
    }
    /**
     * Helper function that writes in the manifesto the lib .jar dependencies.
     * @param extract boolean value that indicates if you include or not the jar dependencies in the build.
     * @param author name of the author of the project.
     */
    public void createIncludeExtractions(boolean extract, String author, String mainClass, String source, String target) {
        String oSource = Optional.ofNullable(source).orElse(getConfigData().get("Source-Path"));
        System.out.println("[Info] creating manifesto ...");

        fileOperation.createManifesto(oSource, Optional.ofNullable(author).orElse(getAuthorName()), extract);
    }
    /**
     * Performs the add jar dependency operation.
     * This takes the .jar or the folder and copies it in the lib folder.
     * @param filePath the path to the jar file, it can be the .jar or the folder.
     * @throws Exception while trying to copy the jar dependency.
     */
    public void createAddJarFileOperation(String filePath) {
        try {
            if(operationUtils.addJarDependency(filePath)) {
                System.out.println("[Info] jar dependency has been added to lib folder");
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * Performs the create build script operation.
     * <br><b>Post: </b> If the OS is windows creates a *.ps1* script, otherwise creates a *.sh* script.
     * @param extract boolean value that indicates if you include or not the jar dependencies in the build.
     */
    public void buildScript(boolean extract, String fileName, String source, String target) {
        System.out.println("[Info] Creating build script...");
        fileOperation.createScript(
            Optional.ofNullable(source).orElse(getConfigData().get("Source-Path")),
            Optional.ofNullable(target).orElse(getConfigData().get("Class-Path")),
            operationUtils.getBuildFileName(fileName),
            extract
        );
    }
    /**
     * Performs the run operation using the compile or *.class* folder path.
     * @param className the main class name or the name of the class that you want to execute.
     * @param source the *.class* folder path. By default its *./bin/*
     * @throws Exception while trying to execute run operation.
     */
    public void runAppOperation(String className, String source, String target) {

        String command = cBuilder.getRunCommand(
            modelUtils.getLibFiles(),
            Optional.ofNullable(className).orElse(" " + getConfigData().get("Main-Class")),
            Optional.ofNullable(source).orElse(getConfigData().get("Source-Path")),
            Optional.ofNullable(target).orElse(getConfigData().get("Class-Path"))
        );
        System.out.println("[Info] running ... ");
        operationUtils.executeCommand(command);

    }
}
