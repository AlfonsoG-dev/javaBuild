package Operations;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

import java.util.List;
import java.util.stream.Collectors;

import Utils.CommandUtils;
import Utils.FileUtils;
import Utils.OperationUtils;

/**
 * Its the perform class of the java command
 */
public class Operation {
    private String localPath;
    private OperationUtils operationUtils;
    private CommandUtils commandUtils;
    private Command myCommand;
    private FileUtils fileUtils;
    private FileOperation fileOperation;
    public Operation(String nLocalPath){
        localPath = nLocalPath;
        operationUtils = new OperationUtils(localPath);
        fileUtils = new FileUtils(localPath);
        fileOperation = new FileOperation(nLocalPath);
        commandUtils = new CommandUtils(nLocalPath);
        myCommand = new Command(nLocalPath);
    }
    /**
     * creates the folder or directory structure:
     * bin, lib, src, docs, extractionFiles.
     */
    public void createProyectOperation() {
        String[] names = {
            "bin",
            "lib",
            "src",
            "docs",
            "extractionFiles"
        };
        System.out.println("[Info] Creating the project structure ...");

        for(String n: names) {
            File f = new File(localPath + File.separator + n);
            if(!f.exists() && f.mkdir()) {
                System.out.println("[Info] Created " + f.getPath());
            }
        }
    }
    /**
     * creates the initial files needed in at the start:
     *  Manifesto, gitignore and the mainClass file for java.
     *  @param author its the name of the author for the Manifesto file.
     *  @throws IOException
     */
    public void createFilesOperation(String author) {
        File localFile = new File(localPath);
        System.out.println("[Info] creating files ...");
        DirectoryStream<Path> files = null;
        try {
            files = Files.newDirectoryStream(localFile.toPath());
            for(Path p: files) {
                File f = p.toFile();
                if(f.getName().equals("src")) {
                    File n = new File(localPath + File.pathSeparator + "src");
                    if(n.listFiles() == null) {
                        operationUtils.createProyectFiles(author);
                    }
                }
            }
        } catch(IOException err) {
            err.printStackTrace();
        } finally {
            if(files != null) {
                try {
                    files.close();
                } catch(Exception e) {
                    e.printStackTrace();
                }
                files = null;
            }
        }
    }
    /**
     * used to list the .java or .jar or .class files in the project.
     * @param source its the folder/directory where the files are stored.
     * @throws IOException
     */
    public void listProjectFiles(String source) {
        String dirPath = localPath + File.separator + new File(source).toPath().normalize();
        try {
            File read = new File(dirPath);
            if(read.isFile()) {
                System.out.println("[Info] Only directory types are allow but here you have it°!");
                System.out.println(read.getPath());
            }
            if(read.isDirectory()) {
                fileUtils.listFilesFromDirectory(Files.newDirectoryStream(new File(dirPath).toPath()))
                    .stream()
                    .map(e -> e.getPath())
                    .filter(e -> e.contains(".java") || e.contains(".jar") || e.contains(".class"))
                    .forEach(e -> {
                        System.out.println(e);
                    });
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * Performs the compile operation using the compile command.
     * @param target the folder/directory where you want to store the .class files. Its ./bin/ by default.
     * @throws Exception when the compile command gets an error.
     */
    public void compileProyectOperation(String source, String target) {
        if(source.isEmpty()) source = "src";
        if(target.isEmpty()) target = "bin";
        String compileCommand = myCommand.getCompileCommand(
                source,
                target
        );
        try {
            System.out.println("[Command] " + compileCommand);
            System.out.println("[Info] compile ...");
            operationUtils.executeCommand(compileCommand);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * Performs the extraction operation using the extract command.
     * Usually you will extract the .jar files stored in the lib folder. 
     * @param extracFile the file to extract.
     * @throws IOException the file doesn't exists
     */
    public void executeExtractionCommand(String extracFile) throws IOException {
        myCommand.getExtractionsCommand()
            .parallelStream()
            .forEach(p -> {
                if(!extracFile.isEmpty()) {
                    System.out.println("[Command] " + p);
                    try {
                        operationUtils.executeCommand(p);
                    } catch(Exception err) {
                        err.printStackTrace();
                    }
                } else {
                    System.out.println("[Info] NO EXTRACTION FILES");
                }
            });
    }
    /**
     * helper function to get the lib folder/directory dependency or .jar files
     * @throws IOException
     */
    public void extractJarDependencies() {
        List<String> jars = commandUtils.getLibFiles();
        jars
            .parallelStream()
            .forEach(e -> {
            try {
                boolean libAlreadyExists = new FileOperation(localPath).extractionDirContainsPath(e);
                if(libAlreadyExists == false) {
                    System.out.println("[Info] extracting jar dependencies ...");
                    operationUtils.createExtractionFiles(jars);
                    // the extraction files can be more than 1
                    executeExtractionCommand(e);
                } else {
                    System.out.println("[Info] THERE IS NO DEPENDENCIES TO EXTRACT");
                }
            } catch(IOException err) {
                err.printStackTrace();
            }
        });
    }
    /**
     * Performs create jar operation using the command.
     * @param includeExtraction boolean valuea that indicates if you add or not to the build the lib file.
     * @param source the folder to include in the build.
     * @throws Exception when creating a jar file gets an error.
     */
    public void createJarOperation(boolean includeExtraction, String source) {
        try {
            String command = myCommand.getJarFileCommand(includeExtraction, source);
            System.out.println("[Command] " + command);
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
        BufferedReader myReader = null;
        try {
            File miFile = new File(localPath + File.separator + "Manifesto.txt");
            if(miFile.exists()) {
                myReader = new BufferedReader(new FileReader(miFile));
                while(myReader.ready()) {
                    String lines = myReader.readLine();
                    if(lines.contains("Class-Path:")) {
                        haveInclude = false;
                    }
                }
            }
        } catch(IOException e) {
            e.printStackTrace();
        } finally {
            if(myReader != null) {
                try {
                    myReader.close();
                } catch(Exception e) {
                    e.printStackTrace();
                }
                myReader = null;
            }
        }
        return haveInclude;
    }
    /**
     * Helper function that allow to get the author name of the manifesto file.
     * @throws IOException
     */
    public String getAuthorName() {
        String author = "";
        BufferedReader myReader = null;
        try {
            File miFile = new File(localPath + File.separator + "Manifesto.txt");
            if(miFile.exists()) {
                myReader = new BufferedReader(new FileReader(miFile));
                while(myReader.ready()) {
                    String lines = myReader.readLine();
                    if(lines.contains("Created-By:")) {
                        author = lines.trim().split(":")[1];
                        break;
                    }
                }
            }
        } catch(IOException e) {
            e.printStackTrace();
        } finally {
            if(myReader != null) {
                try {
                    myReader.close();
                } catch(Exception e) {
                    e.printStackTrace();
                }
                myReader = null;
            }
        }
        return author;
    }
    /**
     * Heper function that writes in the manifesto the lib .jar dependencies.
     * @param includeExtraction boolean value that indicates if you include or not the jar dependencies in the build.
     * @param author name of the author of the project.
     */
    public void createIncludeExtractions(boolean includeExtraction, String author) {
        System.out.println("[Info] creating manifesto ...");
        if(author.isEmpty() && !getAuthorName().isEmpty()) {
            author = getAuthorName();
        }
        if(!includeExtraction) {
            List<String> libJars = commandUtils.getLibFiles();
            String jarFiles = libJars
                .parallelStream()
                .filter(e -> !e.isEmpty())
                .map(e -> e + " ")
                .collect(Collectors.joining());

            fileOperation.writeManifesto(
                    includeExtraction,
                    jarFiles,
                    author
            );
        } else {
            fileOperation.writeManifesto(
                    includeExtraction,
                    "",
                    author
            );
        }
    }
    /**
     * Performs the add jar dependency operation.
     * This takes the .jar or the folder and copies it in the lib folder.
     * @param jarFilePath the path to the jar file, it can be the .jar or the folder.
     * @throws Exception while trying to copy the jar dependency.
     */
    public void createAddJarFileOperation(String jarFilePath) {
        try {
            boolean command = operationUtils.addJarDependency(jarFilePath);
            if(command == true) {
                System.out.println("[Info] jar dependency has been added to lib folder");
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * Performs the create build script operation.
     * <br><b>Post: </b> If the OS is windows creates a *.ps1* script, otherwise creates a *.sh* script.
     * @param includeExtraction boolean value that indicates if you include or not the jar dependencies in the build.
     */
    public void buildScript(boolean includeExtraction, String fileName) {
        operationUtils.createBuildScript(includeExtraction, fileName);
    }
    /**
     * Performs the run operation using the compile or *.class* folder path.
     * @param className the main class name or the name of the class that you want to execute.
     * @param source the *.class* folder path. By default its *./bin/*
     * @throws Exception while trying to execute run operation.
     */
    public void runAppOperation(String className, String source) {
        String command = myCommand.getRunCommand(
                commandUtils.getLibFiles(),
                className,
                source
        );
        try {
            System.out.println("[Command] " + command);
            System.out.println("[Info] running ... ");
            operationUtils.executeCommand(command);
        } catch(Exception e) {
            e.printStackTrace();
        }

    }
}
