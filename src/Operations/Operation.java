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
        // TODO: adicionar la creación del main file
        for(String n: names) {
            File miFile = new File(localPath + "\\" + n);
            if(miFile.exists() == false) {
                miFile.mkdir();
            }
        }

    }
    public void CompileProyectOperation() {
        try {
            String srcClases = operationUtils.srcClases();
            String libJars = operationUtils.libJars();
            String compileCommand = operationUtils.CreateCompileClases(libJars, srcClases);
            Runtime.getRuntime().exec("cmd /k "  + compileCommand);
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
                Runtime.getRuntime().exec("pwsh -Command " + e);
            }
        } catch(Exception e) {
            System.err.println(e);
        }
    }
    public void CreateJarOperation() {
        try {
            String command = operationUtils.CreateJarFileCommand();
            Runtime.getRuntime().exec("pwsh -Command " + command);
        } catch(Exception e) {
            System.err.println(e);
        }
    }
}
