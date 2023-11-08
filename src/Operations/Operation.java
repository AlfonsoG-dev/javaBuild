package Operations;

import java.io.File;

import Utils.FileUtils;
public class Operation {
    private String localPath;
    private FileOperation fileOperation;
    private FileUtils fileUtils;
    public Operation(String nLocalPath){
        fileOperation = new FileOperation(nLocalPath);
        fileUtils = new FileUtils();
        localPath = nLocalPath;
    }
    public void CreateProyectOperation() {
        String[] names = {"bin", "lib", "src", "docs", "extractionFiles"};
        for(String n: names) {
            File miFile = new File(localPath + "\\" + n);
            if(miFile.exists() == false) {
                miFile.mkdir();
            }
        }

    }
    public void CompileProyectOperation() {
        try {
            String[] srcFiles = fileOperation.listSRCDirectories().split("\n");
            String srcClases = "";
            for(String s: srcFiles) {
                srcClases += s + "*.java ";
            }
            String[] libfiles = fileOperation.listLibFiles().split("\n");
            String libJars = "";
            for(String l: libfiles) {
                if(l.contains(".jar")) {
                    libJars += l + "\n";
                }
            }
            String forCommand = "\"";
            String[] libs = libJars.split("\n");
            String b = "";
            for(String l: libs) {
                if(l.isEmpty() == false) {
                    b += l + ";";
                }
            }
            String compileCommand = "";
            if(b.isEmpty() == false) {
                 compileCommand = "javac -d .\\bin\\ " + srcClases;
            } else {
                String cb = b.substring(0, b.length()-1);
                forCommand += cb + "\" " + srcClases;
                compileCommand = "javac -d .\\bin\\ -cp " + forCommand;
            }
            Runtime.getRuntime().exec("pwsh -Command "  + compileCommand);
        } catch(Exception e) {
            System.err.println(e);
        }
    }
    public void CreateJarOperation() {
        try {
            File miFile = new File(localPath + "\\lib\\");
            File extraction = new File(localPath + "\\extractionFiles");
            String libFiles = "";
            for(File f: miFile.listFiles()) {
                if(f.isDirectory()) {
                    libFiles = fileUtils.listFilesFromDirectory(f.listFiles());
                }
            }
            String[] libNames = libFiles.split("\n");
            String lnames = "";
            for(String n: libNames) {
                if(n.contains(".jar")) {
                    fileOperation.CopyFilesfromSourceToTarget(n, extraction.getPath());
                }
            }
            for(File f: extraction.listFiles()) {
                if(f.isDirectory()) {
                    for(File mf: f.listFiles()) {
                        Runtime.getRuntime().exec("pwsh -Command cd " + mf.getParent() + " && " + "jar -xf " + mf.getCanonicalPath() + " && " + "rm -r " + mf.getName() + " cd ../..");
                        Runtime.getRuntime().exec("pwsh -Command " + "jar -cfm testApp.jar Manifesto.txt -C .\\bin\\ . -C " + mf.getParent() + "\\ .");
                    }
                }
            }
        } catch(Exception e) {
            System.err.println(e);
        }
    }
    public void RunProyectOperation() {
    }
}
