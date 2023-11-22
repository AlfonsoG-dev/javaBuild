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
        for(String n: names) {
            File miFile = new File(localPath + "\\" + n);
            if(miFile.exists() == false) {
                String u = miFile.mkdir() == true ? miFile.getPath():"error";
                System.out.println(u);
            }
            System.out.println("Creating the proyect structure ...");
        }
    }
    public void CreateFilesOperation() {
        File localFile = new File(localPath);
        for(File f: localFile.listFiles()) {
            if(f.getName().equals("Manifesto.txt") == false && f.getName().equals("src")) {
                File srcMainFile = new File(localPath + "\\src");
                if(srcMainFile.listFiles().length == 0) {
                    operationUtils.CreateProyectFiles();
                }
            }
            System.out.println("creating the util files ...");
        }
    }
    public void CompileProyectOperation() {
        try {
            String srcClases = operationUtils.srcClases();
            String libJars = operationUtils.libJars();
            String compileCommand = operationUtils.CreateCompileClases(libJars, srcClases);
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
        } catch(Exception e) {
            System.err.println(e);
        }
    }
    public void CreateJarOperation() {
        try {
            String command = operationUtils.CreateJarFileCommand();
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
            String command = operationUtils.CreateAddJarFileCommand(jarFilePath);
            if(command != "") {
                Process addExternarJarProcess = Runtime.getRuntime().exec("pwsh -NoProfile -Command " + command);
                System.out.println("adding dependency in process ...");
                if(addExternarJarProcess.getErrorStream() != null) {
                    operationUtils.CMDOutput(addExternarJarProcess.getErrorStream());
                }
                System.out.println("external dependency has been added to lib folder");
            } else {
                System.out.println("external dependency it already in lib folder");
            }
        } catch(Exception e) {
            System.err.println(e);
        }
    }

    public void CreateRunOperation() {
        try {
            String command = operationUtils.CreateRunComman();
            Process runProcess = Runtime.getRuntime().exec("pwsh -NoProfile -Command " + command);
            System.out.print("RUN IN PROCESS...");
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
