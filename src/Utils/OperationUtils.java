package Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.util.ArrayList;
import java.util.stream.Collectors;

import Operations.FileOperation;

public class OperationUtils {
    private FileOperation fileOperation;
    private String localPath;
    private FileUtils fileUtils;
    public OperationUtils(String nLocalPath) {
        fileOperation = new FileOperation(nLocalPath);
        fileUtils = new FileUtils(localPath);
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
            fileOperation.createFiles(".gitignore", "", false);
            fileOperation.createFiles("Manifesto.txt", mainClass, false);
            fileOperation.createFiles(mainClass + ".java", mainClass, false);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    public String srcClases() {
        String b = "";
        ArrayList<String> names = new ArrayList<>();
        try {
            File srcFile = new File(localPath + "\\src");
            if(srcFile.exists() && srcFile.listFiles() != null) {
                for(File f: srcFile.listFiles()) {
                    if(f.isFile() && f.getName().contains(".java")) {
                        names.add(".\\src\\*.java ");
                        break;
                    }
                }
                ArrayList<String> srcdirs = fileOperation.listSRCDirectories("src");
                srcdirs
                    .parallelStream()
                    .filter(e -> !e.isEmpty())
                    .forEach(e -> {
                        int countFiles = fileUtils.countFilesInDirectory(new File(e));
                        if(countFiles > 0) {
                            names.add(e + "*.java ");
                        }
                    });
            } else {
                System.out.println("error in: " + localPath + "\\SRC\\ folder not found");
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        b += names
            .parallelStream()
            .sorted()
            .collect(Collectors.joining());
        return b;
    }

    /**
     * list all the jar files whitin lib folder
     */
    public ArrayList<String> libJars() {
        ArrayList<String> 
            names = new ArrayList<>(),
            libfiles = fileOperation.listLibFiles();

        libfiles
            .stream()
            .map(e -> new File(e))
            .filter(e -> e.exists() && e.isFile() && e.getName().contains(".jar"))
            .forEach(e -> {
                names.add(e.getPath());
            });
        return names;
    }
    public String createCompileClases(ArrayList<String> libJars, String srcClases) {
        String
            compileCommand = "";

        // create jar files command for compile operation
        StringBuffer 
            b = new StringBuffer(),
            forCommand = new StringBuffer();
        b.append(
                libJars
                .stream()
                .filter(e -> !e.isEmpty())
                .map(e -> e + ";")
                .collect(Collectors.joining())
        );

        if(b.isEmpty()) {
             compileCommand = "javac -Xlint:all -d .\\bin\\ " + srcClases;
        } else {
            String cb = b.substring(0, b.length()-1);
            forCommand.append("'" + cb + "' " + srcClases);
            compileCommand = "javac -Xlint:all -d .\\bin\\ -cp " + forCommand;
        }
        return compileCommand;
    }
    public void createExtractionFiles(ArrayList<String> jars) {
        File extraction = new File(localPath + "\\extractionFiles");
        if(extraction.exists() == false) {
            extraction.mkdir();
        }
        jars
            .parallelStream()
            .forEach(e -> {
                fileOperation.copyFilesfromSourceToTarget(e, extraction.getPath());
            });
    }
    public ArrayList<String> createExtractionCommand() throws IOException {
        File extractionFile = new File(localPath + "\\extractionFiles");
        ArrayList<File> listFiles = fileUtils.listFilesFromPath(extractionFile.getPath());
        ArrayList<String> commands = new ArrayList<>();

        listFiles
            .parallelStream()
            .filter(e -> e.getName().contains(".jar"))
            .forEach(e -> {
                String 
                    jarFileName = e.getName(),
                    jarParent = e.getParent(),
                    extracJAR = "jar -xf " + jarFileName,
                    deleteJAR = "rm -r " + jarFileName + "\n";
                commands.add("cd " + jarParent + " && " + extracJAR + " && " + deleteJAR);
        });
        return commands;
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
    public boolean createAddJarFileCommand(String jarFilePath) throws Exception {
        System.out.println("adding jar dependency in process ...");
        String sourceFilePath = "";
        boolean isAdded = false;
        File jarFile = new File(jarFilePath);
        if(!jarFile.exists()) {
            throw new Exception("jar file not found");
        }
        if(jarFile.isFile()) {
            sourceFilePath = jarFile.getParent();
        } else {
            sourceFilePath = jarFile.getPath();
        }
        String externalJarName = new File(sourceFilePath).getName();
        File libFile = new File(localPath + "\\lib\\" + externalJarName);
        if(libFile.exists() == false) {
            fileOperation.copyFilesfromSourceToTarget(
                    sourceFilePath,
                    new File(localPath + "\\lib").getPath()
            );
            isAdded = true;
        } else {
            System.out.println("DEPENDENCY ALREADY INSIDE THE PROYECT");
        }
        return isAdded;
    }
    public String createRunCommand(ArrayList<String> libJars) {
        String 
            command  = "",
            mainName = FileUtils.getMainClass(localPath) + ".java";
        StringBuffer jarFiles = new StringBuffer();
        jarFiles.append("'.\\bin\\;");
        jarFiles.append(libJars
                .parallelStream()
                .filter(e -> !e.isEmpty())
                .map(e -> e + ";")
                .collect(Collectors.joining())
        );
        if(jarFiles.isEmpty()) {
            command = "java -XX:+ExtensiveErrorReports -d .\\bin\\" + " .\\src\\" + mainName;
        } else {
            String cleanLibs = jarFiles.substring(0, jarFiles.length()-1) + "'";
            command = "java -XX:+ExtensiveErrorReports -cp " + cleanLibs + " .\\src\\" + mainName;
        }
        return command;
    }
    public void createBuildCommand(boolean includeExtraction) {
        String mainName = FileUtils.getMainClass(localPath) + ".jar";
        fileOperation.createFiles("java-exe.ps1", mainName, includeExtraction);
        System.out.println("Adding build script ...");
    }
}
