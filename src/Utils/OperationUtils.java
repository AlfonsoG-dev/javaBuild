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
        String data = "";
        try {
            BufferedReader mio = new BufferedReader(new InputStreamReader(miInputStream));
            while(mio.read() != -1) {
                data += mio.readLine() + "\n";
            }
            mio.close();
        } catch(Exception e) {
            System.err.println(e);
        }
        System.out.println(data);
    }
    public void CreateProyectFiles()  {
        try {
            String mainDirName = new File(localPath).getCanonicalPath();
            String mainClass = new File(mainDirName).getName();
            fileOperation.CreateFiles(".gitignore", "");
            fileOperation.CreateFiles("Manifesto.txt", mainClass);
            fileOperation.CreateFiles(mainClass + ".java", mainClass);
        } catch(Exception e) {
            System.err.println(e);
        }
    }
    public String srcClases() {
        String names = "";
        try {
            File srcFile = new File(localPath + "\\src");
            if(srcFile.exists()) {
                if(srcFile.listFiles().length > 0) {
                    for(File f: srcFile.listFiles()) {
                        if(f.isFile() && f.getName().contains(".java")) {
                            names += ".\\src\\*.java ";
                            break;
                        }
                    }
                }
                String[] srcdirs = fileOperation.listSRCDirectories("src").split("\n");
                if(srcdirs.length > 0) {
                    for(String s: srcdirs) {
                        if(s.isEmpty() == false && new FileUtils().countFilesInDirectory(new File(s)) != -1) {
                            names += s + "*.java ";
                        }
                    }
                }
            } else {
                System.out.println("error in: " + localPath + "\\SRC\\ folder not found");
            }
        } catch(Exception e) {
            System.err.println(e);
        }
        return names;
    }

    public String libJars() {
        String names = "";
        String[] libfiles = fileOperation.listLibFiles().split("\n");
        for(String l: libfiles) {
            File libFile = new File(l);
            if(libFile.exists() && libFile.isFile() && libFile.getName().contains(".jar")) {
                names += libFile.getPath() + "\n";
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
        String[] listFiles = new FileUtils().listFilesFromPath(extractionFile.getPath()).split("\n");
        for(String l: listFiles) {
            String jarFileName = new File(l).getName();
            if(jarFileName.contains(".jar")) {
                String jarParent = new File(l).getParent();
                command += "cd " + jarParent + " && " + "jar -xf " + jarFileName + " && " + "rm -r " + jarFileName + "\n";
            }
        }
        return command;
    }
    public String CreateJarFileCommand() throws IOException {
        String command = "";
        String mainName = "";
        if(FileUtils.GetMainClass(localPath) != "") {
            mainName = FileUtils.GetMainClass(localPath) + ".jar";
        }
        File extractionFile = new File(localPath + "\\extractionFiles");
        String directory = "";
        if(extractionFile.exists() && extractionFile.listFiles() != null) {
            for(File extractionDir: extractionFile.listFiles()) {
                directory += " -C " + extractionDir.getPath() + "\\ .";
            }
        } 
        if(mainName != "" && directory != "") {
            command = "jar -cfm " + mainName + " Manifesto.txt -C .\\bin\\ ." + directory;
        } else if(mainName != "" && directory == "") {
            command = "jar -cfm " + mainName + " Manifesto.txt -C .\\bin\\ .";
        }
        if(mainName == "" && directory != "") {
            String mainDir = new File(localPath).getCanonicalPath();
            command = "jar -cf " + new File(mainDir).getName() + ".jar -C .\\bin\\ ." + directory;
        } else if(mainName == "" && directory == "") {
            String mainDir = new File(localPath).getCanonicalPath();
            command = "jar -cf " + new File(mainDir).getName() + ".jar -C .\\bin\\ .";
        }
        return command;
    }
    public boolean CreateAddJarFileCommand(String jarFilePath) throws Exception {
        System.out.println("adding jar dependency in process ...");
        String sourceFilePath = "";
        boolean addJar = false;
        if(new File(jarFilePath).exists() == false) {
            throw new Exception("jar file not found");
        }
        if(new File(jarFilePath).isFile()) {
            sourceFilePath = new File(jarFilePath).getParent();
        } else {
            sourceFilePath = new File(jarFilePath).getPath();
        }
        String externalJarName = new File(sourceFilePath).getName();
        File libFile = new File(localPath + "\\lib\\" + externalJarName);
        if(libFile.exists() == false) {
            new FileOperation(localPath).CopyFilesfromSourceToTarget(sourceFilePath, new File(localPath + "\\lib").getPath());
            addJar = true;
        } else {
            System.out.println("DEPENDENCY ALREADY INSIDE THE PROYECT");
        }
        return addJar;
    }
    public String CreateRunComman() {
        String command = "";
        String mainName = FileUtils.GetMainClass(localPath) + ".jar";
        fileOperation.CreateFiles("java-exe.ps1", mainName);
        command = ".\\java-exe.ps1";
        return command;
    }
}
