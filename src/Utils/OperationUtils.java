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
                        }
                    }
                }
                String[] srcdirs = fileOperation.listSRCDirectories(".\\src").split("\n");
                if(srcdirs.length > 0) {
                    for(String s: srcdirs) {
                        if(s.isEmpty() == false) {
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
        int count = 0;
        for(String l: libfiles) {
            if(new File(l).getName().contains(".jar")) {
                ++count;
            }
        }
        if(count == 1) {
            for(String l: libfiles) {
                if(new File(l).getName().contains(".jar")) {
                    names += l + "\n";
                }
            }
        } else {
            System.out.println("LIB jar is a --module-path");
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
        if(extractionFile.exists() && extractionFile.listFiles().length > 0) {
            String[] exFiles = new FileOperation(localPath).listSRCDirectories(extractionFile.getPath()).split("\n");
            for(String ex: exFiles) {
                String exParentName = new File(ex).getParent() + "\\";
                directory += " -C " + exParentName + "\\ .";
            }
            if(mainName != "") {
                command = "jar -cfm " + mainName + " Manifesto.txt -C .\\bin\\ ." + directory;
            } else {
                String mainDir = new File(localPath).getCanonicalPath();
                command = "jar -cf " + new File(mainDir).getName() + " -C .\\bin\\ ." + directory;
            }
        } else {
            if(mainName != "") {
                command = "jar -cfm " + mainName + " Manifesto.txt -C .\\bin\\ .";
            } else {
                String mainDir = new File(localPath).getCanonicalPath();
                command = "jar -cfm " + new File(mainDir).getName() + " Manifesto.txt -C .\\bin\\ .";
            }
        }
        return command;
    }
    public String CreateAddJarFileCommand(String jarFilePath) throws Exception {
        String command = "";
        if(new File(jarFilePath).exists() == false) {
            throw new Exception("jar file not found");
        }
        String[] libFiles = new FileOperation(localPath).listLibFiles().split("\n");
        String externalJarName = new File(jarFilePath).getName();
        String targetFileName = new File(jarFilePath).getName();
        File libFile = new File(localPath + "\\lib\\" + targetFileName);
        if(libFile.exists() == false) {
            if(libFiles.length > 0) {
                for(String l: libFiles) {
                    String libFileName = new File(l).getName();
                    if(libFileName.equals(externalJarName) == false) {
                        command = "filem -cp " + new File(jarFilePath).getPath() + " to " + new File(localPath + "\\lib");
                    }
                }
            } else {
                command = "filem -cp " + new File(jarFilePath).getPath() + " to " + libFile.getCanonicalPath();
            }
        } else {
            System.out.println("DEPENDENCY ALREADY INSIDE THE PROYECT");
        }
        return command;
    }
    public String CreateRunComman() {
        String command = "";
        String mainName = FileUtils.GetMainClass(localPath) + ".jar";
        fileOperation.CreateFiles("java-exe.ps1", mainName);
        command = ".\\java-exe.ps1";
        return command;
    }
}
