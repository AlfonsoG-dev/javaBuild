package Operations;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

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
        for(File f: localFile.listFiles()) {
            if(f.getName().equals("src")) {
                File srcMainFile = new File(localPath + "\\src");
                if(srcMainFile.listFiles().length == 0) {
                    operationUtils.createProyectFiles();
                }
            }
        }
    }
    public void compileProyectOperation() {
        String srcClases = operationUtils.srcClases();
        String libJars = operationUtils.libJars();
        String compileCommand = operationUtils.createCompileClases(
                libJars,
                srcClases
        );
        try {
            Process compileProcess = Runtime.getRuntime().exec("pwsh -NoProfile -Command "  + compileCommand);
            System.out.println("compile ...");
            if(compileProcess.getErrorStream() != null) {
                operationUtils.CMDOutput(compileProcess.getErrorStream());
            }
        } catch(Exception e) {
            System.err.println(e);
        }
    }
    public void extractJarDependencies() {
        try {
            String[] jars = operationUtils.libJars().split("\n");
            if(jars.length > 0) {
                for(String j: jars) {
                    boolean libAlreadyExists = new FileOperation(localPath).extractionDirContainsPath(j);
                    if(libAlreadyExists == false) {
                        operationUtils.createExtractionFiles(jars);
                        String[] extractions = operationUtils.createExtractionCommand().split("\n");
                        for(String e: extractions) {
                            System.out.println("extracting jar dependencies ...");
                            if(!e.isEmpty()) {
                                Process extracProcess = Runtime.getRuntime().exec(
                                        "pwsh -NoProfile -Command " + e
                                );
                                if(extracProcess.getErrorStream() != null) {
                                    operationUtils.CMDOutput(extracProcess.getErrorStream());
                                }
                            } else {
                                System.out.println("NO EXTRACTION FILES");
                            }
                        }
                    } else {
                        System.out.println("THERE IS NO DEPENDENCIES TO EXTRACT");
                    }
                }
            }
        } catch(Exception e) {
            System.err.println(e);
        }
    }
    public void createJarOperation(boolean includeExtraction) {
        try {
            String command = operationUtils.createJarFileCommand(includeExtraction);
            if(command.equals("")) {
                throw new Exception("error while trying to create ther jar file");
            }
            Process createJarProcess = Runtime.getRuntime().exec("pwsh -NoProfile -Command " + command);
            System.out.println("creating jar file ...");
            if(createJarProcess.getErrorStream() != null) {
                operationUtils.CMDOutput(createJarProcess.getErrorStream());
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
            if(includeExtraction == true) {
                String libJars = operationUtils.libJars();
                String[] libs = libJars.split("\n");
                String jarFiles = "";
                for(String l: libs) {
                    if(!l.isEmpty()) {
                        jarFiles += l + " ";
                    }
                }
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

    public void createRunOperation() {
        operationUtils.CreateRunComman();
    }
}
