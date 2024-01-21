package Operations;

import java.io.File;

import Utils.OperationUtils;

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
                String u = miFile.mkdir() == true ? miFile.getPath() : "error";
                System.out.println(u);
            }
        }
    }
    public void CreateFilesOperation() {
        File localFile = new File(localPath);
        System.out.println("creating files ...");
        for(File f: localFile.listFiles()) {
            if(f.getName().equals("src")) {
                File srcMainFile = new File(localPath + "\\src");
                if(srcMainFile.listFiles().length == 0) {
                    operationUtils.CreateProyectFiles();
                }
            }
        }
    }
    public void CompileProyectOperation() {
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
    }
    public void ExtractJarDependencies() {
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
                                Process extracProcess = Runtime.getRuntime().exec("pwsh -NoProfile -Command " + e);
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
    public void CreateJarOperation() {
        try {
            String command = operationUtils.CreateJarFileCommand();
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
    }
    public void CreateAddJarFileOperation(String jarFilePath) {
        try {
            boolean command = operationUtils.CreateAddJarFileCommand(jarFilePath);
            if(command == true) {
                System.out.println("jar dependency has been added to lib folder");
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void CreateRunOperation() {
        operationUtils.CreateRunComman();
    }
}
