package Operations;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

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
    public Operation(String nLocalPath){
        operationUtils = new OperationUtils(nLocalPath);
        localPath = nLocalPath;
    }
    public void createProyectOperation() {
        String[] names = {
            "bin",
            "lib",
            "src",
            "docs",
            "extractionFiles"
        };
        System.out.println("Creating the proyect structure ...");
        for(String n: names) {
            File miFile = new File(localPath + "\\" + n);
            if(miFile.exists() == false) {
                String u = miFile.mkdir() == true ? miFile.getPath() : "error";
                System.out.println(u);
            }
        }
    }
    public void createFilesOperation() {
        File localFile = new File(localPath);
        System.out.println("creating files ...");
        try {
            DirectoryStream<Path> files = Files.newDirectoryStream(localFile.toPath());
            files
                .forEach(e -> {
                    File f = e.toFile();
                    if(f.getName().equals("src")) {
                        File srcMainFile = new File(localPath + "\\src");
                        if(srcMainFile.listFiles().length == 0) {
                            operationUtils.createProyectFiles();
                        }
                    }
                });
        } catch(Exception err) {
            err.printStackTrace();
        }
    }
    public void compileProyectOperation() {
        String srcClases = operationUtils.srcClases();
        String compileCommand = operationUtils.createCompileClases(
                operationUtils.libJars(),
                srcClases
        );
        try {
            Process compileProcess = Runtime.getRuntime().exec("pwsh -NoProfile -Command "  + compileCommand);
            System.out.println("compile ...");
            if(compileProcess.errorReader() != null) {
                operationUtils.CMDOutputError(compileProcess.errorReader());
            }
        } catch(Exception e) {
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
                            System.out.println("extracting jar dependencies ...");
                            if(!e.isEmpty()) {
                                try {
                                    Process extracProcess = Runtime.getRuntime().exec(
                                            "pwsh -NoProfile -Command " + p
                                    );
                                    if(extracProcess.errorReader() != null) {
                                        operationUtils.CMDOutputError(extracProcess.errorReader());
                                    }
                                } catch(Exception err) {
                                    err.printStackTrace();
                                }
                            } else {
                                System.out.println("NO EXTRACTION FILES");
                            }
                        });
                } else {
                    System.out.println("THERE IS NO DEPENDENCIES TO EXTRACT");
                }
            } catch(Exception err) {
                err.printStackTrace();
            }
        });
    }
    public void createJarOperation(boolean includeExtraction) {
        try {
            String command = operationUtils.createJarFileCommand(includeExtraction);
            if(command.equals("")) {
                throw new Exception("error while trying to create ther jar file");
            }
            Process createJarProcess = Runtime.getRuntime().exec("pwsh -NoProfile -Command " + command);
            System.out.println("creating jar file ...");
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
        } catch(Exception e) {
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
        try {
            if(!includeExtraction) {
                ArrayList<String> libJars = operationUtils.libJars();
                String jarFiles = libJars
                    .parallelStream()
                    .filter(e -> !e.isEmpty())
                    .map(e -> e + " ")
                    .collect(Collectors.joining());

                new FileUtils().writeManifesto(
                        new File(localPath),
                        "Manifesto.txt",
                        includeExtraction,
                        jarFiles
                );
            } else {
                new FileUtils().writeManifesto(
                        new File(localPath),
                        "Manifesto.txt",
                        includeExtraction,
                        ""
                );
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    public void createAddJarFileOperation(String jarFilePath) {
        try {
            boolean command = operationUtils.createAddJarFileCommand(jarFilePath);
            if(command == true) {
                System.out.println("jar dependency has been added to lib folder");
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    public void createBuildScript(boolean includeExtraction) {
        operationUtils.createBuildCommand(includeExtraction);
    }
    public void runAppOperation() {
        String command = operationUtils.createRunCommand(operationUtils.libJars());
        try {
            Process runProcess = Runtime.getRuntime().exec("pwsh -NoProfile -Command " + command);
            System.out.println("running ... ");
            if(runProcess.getInputStream() != null) {
                operationUtils.CMDOutput(runProcess.getInputStream());
            }
            if(runProcess.errorReader() != null) {
                operationUtils.CMDOutputError(runProcess.errorReader());
            }
        } catch(Exception e) {
            e.printStackTrace();
        }

    }
}
