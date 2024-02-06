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
    public void CMDOutputError(BufferedReader miCmdReader) {
        BufferedReader miReader = null;
        try {
            miReader = miCmdReader;
            char[] mr = new char[1024];
            while(miReader.read(mr) != -1) {
                System.out.println(miReader.readLine() + "\n");
            }
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            if(miReader != null) {
                try {
                    miReader.close();
                } catch(Exception e) {
                    e.printStackTrace();
                }
                miReader = null;
            }
        }
    }
    public void CMDOutput(InputStream miCmdStream) {
        BufferedReader miReader = null;
        try {
            miReader = new BufferedReader(new InputStreamReader(miCmdStream));
            String line = "";
            while(true) {
                line = miReader.readLine();
                if(line == null) {
                    break;
                }
                System.out.println(line);
            }
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            if(miReader != null) {
                try {
                    miReader.close();
                } catch(Exception e) {
                    e.printStackTrace();
                }
                miReader = null;
            }
        }
    }
    public void createProyectFiles()  {
        try {
            String mainDirName = new File(localPath).getCanonicalPath();
            String mainClass = new File(mainDirName).getName();
            fileOperation.createFiles(".gitignore", "");
            fileOperation.createFiles("Manifesto.txt", mainClass);
            fileOperation.createFiles(mainClass + ".java", mainClass);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    public String srcClases() {
        String names = "";
        try {
            File srcFile = new File(localPath + "\\src");
            if(srcFile.exists() && srcFile.listFiles() != null) {
                for(File f: srcFile.listFiles()) {
                    if(f.isFile() && f.getName().contains(".java")) {
                        names += ".\\src\\*.java ";
                        break;
                    }
                }
                String[] srcdirs = fileOperation.listSRCDirectories("src").split("\n");
                if(srcdirs.length > 0) {
                    for(String s: srcdirs) {
                        int countFiles = new FileUtils().countFilesInDirectory(new File(s));
                        if(s.isEmpty() == false && countFiles > 0) {
                            names += s + "*.java ";
                        }
                    }
                }
            } else {
                System.out.println("error in: " + localPath + "\\SRC\\ folder not found");
            }
        } catch(Exception e) {
            e.printStackTrace();
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
    private boolean manifestoIsCreated() {
        boolean isCreated = false;
        try {
            File miFile = new File(localPath + "\\Manifesto.txt");
            if(miFile.exists()) {
                isCreated = true;
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        return isCreated;
    }
    private String jarTypeFormat(String mainName, String directory) throws IOException {
        String jarFormat = "";
        if(manifestoIsCreated()) {
            jarFormat = "jar -cfm ";
        }
        if(!manifestoIsCreated() && mainName == "") {
            jarFormat = "jar -cf ";
        }
        if(!manifestoIsCreated() && mainName != "") {
            jarFormat = "jar -cfe ";
        }
        return jarFormat;
    }
    private String jarTypeUnion(String mainName, String directory) throws IOException {
        String build = "";
        String localParent = new File(localPath).getCanonicalPath();
        String jarFormat = jarTypeFormat(mainName, directory);
        switch(jarFormat) {
            case "jar -cfm ":
                if(mainName != "" && directory != "") {
                    build = jarFormat + mainName + " Manifesto.txt -C .\\bin\\ ." + directory;
                } else if(mainName != "" && directory == "") {
                    build = jarFormat + mainName + " Manifesto.txt -C .\\bin\\ .";
                }
                break;
            case "jar -cf ":
                String jarName = new File(localParent).getName() + ".jar";
                if(directory != "") {
                    build = jarFormat + jarName + " -C .\\bin\\ ." + directory;
                } else {
                    build = jarFormat + jarName + " -C .\\bin\\ .";
                }
                break;
            case "jar -cfe ":
                String mainClassName = FileUtils.getMainClass(localPath);
                if(directory != "") {
                    build = jarFormat + mainName + " " + mainClassName +" -C .\\bin\\ ." + directory;
                } else {
                    build = jarFormat + mainName + " " + mainClassName +" -C .\\bin\\ .";
                }
                break;
        }
        return build;
    }
    public String createJarFileCommand(boolean includeExtraction) throws IOException {
        String mainName = FileUtils.getMainClass(localPath) + ".jar";
        File extractionFile = new File(localPath + "\\extractionFiles");

        String directory = "";
        if(extractionFile.exists() && extractionFile.listFiles() != null) {
            for(File extractionDir: extractionFile.listFiles()) {
                directory += " -C " + extractionDir.getPath() + "\\ .";
            }
        } 
        String command = "";
        if(includeExtraction) {
            command = jarTypeUnion(mainName, directory);
        } else {
            command = jarTypeUnion(mainName, "");
        }
        return command;
    }
    public boolean createUpdateJarFileCommand() {
        boolean updated = false;
        // TODO: implement the update a jar file command
        // only works if in the root of the project alredy exists a jar file with the main class name
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
    public String createRunCommand(String libJars) {
        String command = "";
        String mainName = FileUtils.getMainClass(localPath) + ".java";
        String[] libs = libJars.split("\n");
        String jarFiles = "'.\\bin\\;";
        for(String l: libs) {
            if(l.isEmpty() == false) {
                jarFiles += l + ";";
            }
        }
        if(jarFiles.isEmpty()) {
            command = "java -XX:+ExtensiveErrorReports -d .\\bin\\" + " .\\src\\" + mainName;
        } else {
            String cleanLibs = jarFiles.substring(0, jarFiles.length()-1) + "'";
            command = "java -XX:+ExtensiveErrorReports -cp " + cleanLibs + " .\\src\\" + mainName;
        }
        return command;
    }
    public void createBuildCommand() {
        String mainName = FileUtils.getMainClass(localPath) + ".jar";
        fileOperation.createFiles("java-exe.ps1", mainName);
        System.out.println("Adding build script ...");
    }
}
