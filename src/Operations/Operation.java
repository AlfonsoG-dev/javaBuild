package Operations;

import java.io.File;

import Utils.OperationUtils;
import Operations.FileOperation;
public class Operation {
    private String localPath;
    private OperationUtils operationUtils;
    public Operation(String nLocalPath){
        operationUtils = new OperationUtils(nLocalPath);
        localPath = nLocalPath;
    }
    public void CreateProyectOperation() {
        String[] names = {"bin", "lib", "src", "docs", "extractionFiles"};
        System.out.println("Creating the proyect structure ...");
        for(String n: names) {
            File miFile = new File(localPath + "\\" + n);
            if(miFile.exists() == false) {
                String u = miFile.mkdir() == true ? miFile.getPath():"error";
                System.out.println(u);
            }
        }
    }
    public void CreateFilesOperation() {
        File localFile = new File(localPath);
        System.out.println("creating the util files ...");
        for(File f: localFile.listFiles()) {
            if(f.getName().equals("Manifesto.txt") == false && f.getName().equals("src")) {
                File srcMainFile = new File(localPath + "\\src");
                if(srcMainFile.listFiles().length == 0) {
                    operationUtils.CreateProyectFiles();
                }
            }
        }
    }
    public String CompileProyectOperation() {
        String srcClases = operationUtils.srcClases();
        String libJars = operationUtils.libJars();
        String compileCommand = operationUtils.CreateCompileClases(libJars, srcClases);
        try {
            Process compileProcess = Runtime.getRuntime().exec("pwsh -NoProfile -Command "  + compileCommand);
            System.out.println("compile ...");
            if(compileProcess.getErrorStream() != null) {
                operationUtils.CMDOutput(compileProcess.getErrorStream());
            }
        } catch(Exception e) {
            System.err.println(e);
        }
        return compileCommand;
    }
    public String ExtractJarDependencies() {
        String extractionCommand = "";
        try {
            String[] jars = operationUtils.libJars().split("\n");
            if(jars.length > 0) {
                for(String j: jars) {
                    if(new FileOperation(localPath).ExtractionDirContainsPath(j) == false) {
                        operationUtils.CreateExtractionFiles(jars);
                        String[] extractions = operationUtils.CreateExtractionCommand().split("\n");
                        for(String e: extractions) {
                            System.out.println("extracting jar dependencies ...");
                            if(!e.isEmpty()) {
                                extractionCommand += e + "\n";
                                Process extracProcess = Runtime.getRuntime().exec("pwsh -NoProfile -Command " + e);
                                if(extracProcess.getErrorStream() != null) {
                                    operationUtils.CMDOutput(extracProcess.getErrorStream());
                                }
                                if(extracProcess.exitValue() == 0) {
                                    System.out.println("extracting jar dependencies terminated");
                                }
                            } else {
                                System.out.println("NO EXTRACTION FILES");
                            }
                        }
                    } else {
                        System.out.println("NO NEW EXTRACTION DENEPENDENCY FOUND");
                    }
                }
            }
        } catch(Exception e) {
            System.err.println(e);
        }
        return extractionCommand;
    }
    public String CreateJarOperation() {
        String command = "";
        try {
            command = operationUtils.CreateJarFileCommand();
            if(command.equals("")) {
                throw new Exception("error while trying to create ther jar file");
            }
            Process createJarProcess = Runtime.getRuntime().exec("pwsh -NoProfile -Command " + command);
            System.out.println("creating jar file ...");
            if(createJarProcess.getErrorStream() != null) {
                operationUtils.CMDOutput(createJarProcess.getErrorStream());
            }
        } catch(Exception e) {
            System.err.println(e);
        }
        return command;
    }
    public String CreateAddJarFileOperation(String jarFilePath) {
        String command = "";
        Process addExternarJarProcess = null;
        try {
            command = operationUtils.CreateAddJarFileCommand(jarFilePath);
            if(command != "") {
                addExternarJarProcess = Runtime.getRuntime().exec("pwsh -NoProfile -Command " + command);
                System.out.println("adding dependency in process ...");
                if(addExternarJarProcess.getErrorStream() != null) {
                    operationUtils.CMDOutput(addExternarJarProcess.getErrorStream());
                }
                addExternarJarProcess.waitFor();
                System.out.println("jar dependency has been added to lib folder");
            }
        } catch(Exception e) {
            System.err.println(e);
        }
        return command;
    }

    public void CreateRunOperation() {
        try {
            String command = operationUtils.CreateRunComman();
            Process runProcess = Runtime.getRuntime().exec("pwsh -NoProfile -Command " + command);
            System.out.print("Adding run script ...");
            if(runProcess.getErrorStream() != null) {
                operationUtils.CMDOutput(runProcess.getErrorStream());
            }
            if(runProcess.getInputStream() != null) {
                operationUtils.CMDOutput(runProcess.getInputStream());
            }
        } catch(Exception e) {
            System.err.println(e.getLocalizedMessage());
        }
    }
}
