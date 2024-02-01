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
    public void createProyectFiles()  {
        try {
            String mainDirName = new File(localPath).getCanonicalPath();
            String mainClass = new File(mainDirName).getName();
            fileOperation.createFiles(".gitignore", "");
            fileOperation.createFiles("Manifesto.txt", mainClass);
            fileOperation.createFiles(mainClass + ".java", mainClass);
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
                        int countFiles = new FileUtils().countFilesInDirectory(new File(s));
                        if(s.isEmpty() == false && countFiles != -1) {
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

    /**
     * list all the jar files whitin lib folder
     */
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
    public String createCompileClases(String libJars, String srcClases) {
        String forCommand = "'";
        String[] libs = libJars.split("\n");
        String jarFiles = "";
        for(String l: libs) {
            if(l.isEmpty() == false) {
                jarFiles += l + ";";
            }
        }
        String compileCommand = "";
        if(jarFiles.isEmpty() == true) {
             compileCommand = "javac -Xlint:all -d .\\bin\\ " + srcClases;
        } else {
            String cb = jarFiles.substring(0, jarFiles.length()-1);
            forCommand += cb + "' " + srcClases;
            compileCommand = "javac -Xlint:all -d .\\bin\\ -cp " + forCommand;
        }
        return compileCommand;
    }
    public void createExtractionFiles(String[] jars) {
        File extraction = new File(localPath + "\\extractionFiles");
        if(extraction.exists() == false) {
            extraction.mkdir();
        }
        String[] libNames = jars;
        for(String n: libNames) {
            fileOperation.copyFilesfromSourceToTarget(n, extraction.getPath());
        }
    }
    public String createExtractionCommand() throws IOException {
        String command = "";
        File extractionFile = new File(localPath + "\\extractionFiles");
        String[] listFiles = new FileUtils().listFilesFromPath(extractionFile.getPath()).split("\n");
        for(String l: listFiles) {
            String jarFileName = new File(l).getName();
            if(jarFileName.contains(".jar")) {
                String jarParent = new File(l).getParent();
                String extracJAR = "jar -xf " + jarFileName;
                String deleteJAR = "rm -r " + jarFileName + "\n";
                command += "cd " + jarParent + " && " + extracJAR + " && " + deleteJAR;
            }
        }
        return command;
    }
    public String createJarFileCommand() throws IOException {
        String command = "";
        String mainName = "";
        if(FileUtils.getMainClass(localPath) != "") {
            mainName = FileUtils.getMainClass(localPath) + ".jar";
        }
        File extractionFile = new File(localPath + "\\extractionFiles");
        // TODO: when adding a dependency use the Class-Path in the Manifesto file for that purpose
        // Class-Path: .\lib\java-mysql-eje\java-mysql-eje.jar
        // and when creating the jar file don't add the extraction dependency files

        String directory = "";
        if(extractionFile.exists() && extractionFile.listFiles() != null) {
            for(File extractionDir: extractionFile.listFiles()) {
                directory += " -C " + extractionDir.getPath() + "\\ .";
            }
        } 
        // TODO: if there is no Manifesto use the mainClassName as entry point
        // jar -cfe App.jar mainClassName -C .\bin\ .
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
    public boolean createUpdateJarFileCommand() {
        boolean updated = false;
        // TODO: implement the update a jar file command
        // jar -uf jar_file.jar -C .\folder\ .
        // jar -uf jar_file.jar .\bin\App.class
        return updated;
    }
    public boolean createAddJarFileCommand(String jarFilePath) throws Exception {
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
            fileOperation.copyFilesfromSourceToTarget(
                    sourceFilePath,
                    new File(localPath + "\\lib").getPath()
            );
            addJar = true;
        } else {
            System.out.println("DEPENDENCY ALREADY INSIDE THE PROYECT");
        }
        return addJar;
    }
    public void CreateRunComman() {
        String mainName = FileUtils.getMainClass(localPath) + ".jar";
        fileOperation.createFiles("java-exe.ps1", mainName);
        System.out.println("Adding build script ...");
    }
}
