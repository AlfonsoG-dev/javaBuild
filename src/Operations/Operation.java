package Operations;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

import java.util.ArrayList;
import java.util.stream.Collectors;

import Utils.FileUtils;
import Utils.OperationUtils;

public class Operation {
    private String localPath;
    private OperationUtils operationUtils;
    private FileUtils fileUtils;
    public Operation(String nLocalPath){
        localPath = nLocalPath;
        operationUtils = new OperationUtils(localPath);
        fileUtils = new FileUtils(localPath);
    }
    public void createProyectOperation() {
        String[] names = {
            "bin",
            "lib",
            "src",
            "docs",
            "extractionFiles"
        };
        System.out.println("[ INFO ]: Creating the proyect structure ...");

        for(String n: names) {
            File f = new File(localPath + "\\" + n);
            if(!f.exists()) {
                f.mkdir();
                System.out.println("[ INFO ] Created: " + f.getPath());
            }
        }
    }
    public void createFilesOperation() {
        File localFile = new File(localPath);
        System.out.println("[ INFO ]: creating files ...");
        DirectoryStream<Path> files = null;
        try {
            files = Files.newDirectoryStream(localFile.toPath());
            for(Path p: files) {
                File f = p.toFile();
                if(f.getName().equals("src")) {
                    File n = new File(localPath + File.pathSeparator + "src");
                    if(n.listFiles() == null) {
                        operationUtils.createProyectFiles();
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
    public void compileProyectOperation() {
        String srcClases = operationUtils.srcClases();
        String compileCommand = operationUtils.createCompileClases(
                operationUtils.libJars(),
                srcClases
        );
        try {
            System.out.println("[CMD]: " + compileCommand);
            Process compileProcess = Runtime.getRuntime().exec("pwsh -NoProfile -Command "  + compileCommand);
            System.out.println("[ INFO ]: compile ...");
            if(compileProcess.errorReader() != null) {
                operationUtils.CMDOutputError(compileProcess.errorReader());
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
    public void extractJarDependencies() {
        ArrayList<String> jars = operationUtils.libJars();
        jars
            .parallelStream()
            .forEach(e -> {
            try {
                boolean libAlreadyExists = new FileOperation(localPath).extractionDirContainsPath(e);
                if(libAlreadyExists == false) {
                    operationUtils.createExtractionFiles(jars);
                    ArrayList<String> extractions = operationUtils.createExtractionCommand();
                    extractions
                        .parallelStream()
                        .forEach(p -> {
                            System.out.println("[ INFO ]: extracting jar dependencies ...");
                            if(!e.isEmpty()) {
                                System.out.println("[CMD]: " + p);
                                try {
                                    Process extracProcess = Runtime.getRuntime().exec(
                                            "pwsh -NoProfile -Command " + p
                                    );
                                    if(extracProcess.errorReader() != null) {
                                        operationUtils.CMDOutputError(extracProcess.errorReader());
                                    }
                                } catch(IOException err) {
                                    err.printStackTrace();
                                }
                            } else {
                                System.out.println("[ INFO ]: NO EXTRACTION FILES");
                            }
                        });
                } else {
                    System.out.println("[ INFO ]: THERE IS NO DEPENDENCIES TO EXTRACT");
                }
            } catch(IOException err) {
                err.printStackTrace();
            }
        });
    }
    public void createJarOperation(boolean includeExtraction) {
        try {
            String command = operationUtils.createJarFileCommand(includeExtraction);
            System.out.println("[CMD]: " + command);
            if(command.equals("")) {
                throw new Exception("[ ERROR ]: while trying to create ther jar file");
            }
            Process createJarProcess = Runtime.getRuntime().exec("pwsh -NoProfile -Command " + command);
            System.out.println("[ INFO ]: creating jar file ...");
            if(createJarProcess.errorReader() != null) {
                operationUtils.CMDOutputError(createJarProcess.errorReader());
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    public boolean haveIncludeExtraction() {
        boolean haveInclude = true; 
        BufferedReader myReader = null;
        try {
            File miFile = new File(localPath + "\\Manifesto.txt");
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
    public void createIncludeExtractions(boolean includeExtraction) {
        System.out.println("[ INFO ]: creating manifesto ...");
        if(!includeExtraction) {
            ArrayList<String> libJars = operationUtils.libJars();
            String jarFiles = libJars
                .parallelStream()
                .filter(e -> !e.isEmpty())
                .map(e -> e + " ")
                .collect(Collectors.joining());

            fileUtils.writeManifesto(
                    "Manifesto.txt",
                    includeExtraction,
                    jarFiles
            );
        } else {
            fileUtils.writeManifesto(
                    "Manifesto.txt",
                    includeExtraction,
                    ""
            );
        }
    }
    public void createAddJarFileOperation(String jarFilePath) {
        try {
            boolean command = operationUtils.createAddJarFileCommand(jarFilePath);
            System.out.println("[CMD]: " + command);
            if(command == true) {
                System.out.println("[ INFO ]: jar dependency has been added to lib folder");
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    public void createBuildScript(boolean includeExtraction) {
        operationUtils.createBuildCommand(includeExtraction);
    }
    public void runAppOperation(String className) {
        String command = operationUtils.createRunCommand(
                operationUtils.libJars(),
                className
        );
        try {
            System.out.println("[CMD]: " + command);
            Process runProcess = Runtime.getRuntime().exec("pwsh -NoProfile -Command " + command);
            System.out.println("[ INFO ]: running ... ");
            if(runProcess.getInputStream() != null) {
                operationUtils.CMDOutput(runProcess.getInputStream());
            }
            if(runProcess.errorReader() != null) {
                operationUtils.CMDOutputError(runProcess.errorReader());
            }
        } catch(IOException e) {
            e.printStackTrace();
        }

    }
}
