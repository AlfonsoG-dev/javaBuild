package Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import Operations.FileOperation;

public class OperationUtils {
    private FileOperation fileOperation;
    private String localPath;
    public OperationUtils(String nLocalPath) {
        fileOperation = new FileOperation(nLocalPath);
        localPath = nLocalPath;
    }
    public void CMDOutput(InputStream miInputStream) {
        try {
            BufferedReader mio = new BufferedReader(new InputStreamReader(miInputStream));
            while(mio.read() != -1) {
                System.out.println(mio.readLine());
            }
            mio.close();
        } catch(Exception e) {
            System.err.println(e);
        }
    }
    public void CreateProyectFiles()  {
        File localFile = new File(localPath);
        try {
            String[] names = localFile.getCanonicalPath().split("\\\\");
            String mainClass = names[names.length-1];
            fileOperation.CreateFiles(".gitignore", "");
            fileOperation.CreateFiles("Manifesto.txt", mainClass);
            fileOperation.CreateFiles(mainClass + ".java", mainClass);
        } catch(Exception e) {
        }
    }
    public String srcClases() {
        String names = "", cNames = "";
        try {
            String[] srcdirs = fileOperation.listSRCDirectories(".\\src").split("\n");
            for(String s: srcdirs) {
                if(s.isEmpty() == false) {
                    names += "." + s + "*.java ";
                }
            }
            if(srcdirs.length > 0) {
                cNames = ".\\src\\*.java " + names.substring(0, names.length()-1);
            } else {
                cNames = ".\\src\\*java";
            }
        } catch(Exception e) {
            System.err.println(e);
        }
        return cNames;
    }

    public String libJars() {
        String names = "";
        String[] libfiles = fileOperation.listLibFiles().split("\n");
        for(String l: libfiles) {
            if(l.contains(".jar")) {
                names += l + "\n";
            }
        }
        return names;
    }
    public String CreateCompileClases(String libJars, String srcClases) {
        String forCommand = "\"";
        String[] libs = libJars.split("\n");
        String b = "";
        for(String l: libs) {
            if(l.isEmpty() == false) {
                b += l + ";";
            }
        }
        String compileCommand = "";
        if(b.isEmpty() == true) {
             compileCommand = "javac -d .\\bin\\ " + srcClases;
        } else {
            String cb = b.substring(0, b.length()-1);
            forCommand += cb + "\" " + srcClases;
            compileCommand = "javac -d .\\bin\\ -cp " + forCommand;
        }
        return compileCommand;
    }
    public void CreateExtractionFiles(String[] jars) {
        File extraction = new File(localPath + "\\extractionFiles");
        if(extraction.exists() == false) {
            extraction.mkdir();
        }
        String[] libNames = jars;
        for(String n: libNames) {
            fileOperation.CopyFilesfromSourceToTarget(n, extraction.getPath());
        }
    }
    public String CreateExtractionCommand() throws IOException {
        String command = "";
        File extractionFile = new File(localPath + "\\extractionFiles");
        for(File f: extractionFile.listFiles()) {
            if(f.isDirectory()) {
                for(File mf: f.listFiles()) {
                    command += "cd " + mf.getParent() + " && " + "jar -xf " + mf.getName() + " && " + "rm -r " + mf.getName() + "\n";
                }
            }
        }
        return command;
    }
    public String CreateJarFileCommand() {
        String command = "";
        File srcFile = new File(localPath + "\\src");
        String mainName = "";
        for(File f: srcFile.listFiles()) {
            if(f.isFile() && f.getName().contains(".java")) {
                mainName = f.getName().split(".java")[0] + ".jar";
            }
        }
        File extractionFile = new File(localPath + "\\extractionFiles");
        String directory = "";
        if(extractionFile.exists() && extractionFile.listFiles().length > 0) {
            for(File f: extractionFile.listFiles()) {
                directory += " -C " +f.getPath() + "\\ .";
            }
            command = "jar -cfm " + mainName + " Manifesto.txt -C .\\bin\\ ." + directory;
        } else {
            command = "jar -cfm " + mainName + " Manifesto.txt -C .\\bin\\ .";
        }
        return command;
    }
    public String CreateRunComman() {
        String command = "";
        File localFile = new File(localPath);
        File srcFile = new File(localFile.getAbsoluteFile() + "\\src");
        String mainName = "";
        for(File f: srcFile.listFiles()) {
            if(f.isFile() && f.getName().contains(".java")) {
                mainName = f.getName().split(".java")[0] + ".jar";
            }
        }
        fileOperation.CreateFiles("java-exe.ps1", mainName);
        command = "java-exe.ps1";
        return command;
    }
}
