package Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.lang.Process;

import java.util.ArrayList;
import java.util.List;
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
    public void executeCommand(String command) {
        Process p = null;
        try {
            ProcessBuilder builder = new ProcessBuilder();
            String localFULL = new File(localPath).getCanonicalPath();
            File local = new File(localFULL);
            if(command.isEmpty()) {
                throw new Exception("[ ERROR ]: cannot execute an empty command: ");
            }
            builder.command("pwsh", "-NoProfile", "-Command", command);
            builder.directory(local);
            p = builder.start();
            if(p.getErrorStream() != null) {
                CMDOutputError(p.getErrorStream());
            }
            if(p.getInputStream() != null) {
                CMDOutput(p.getInputStream());
            }
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            if(p != null) {
                try {
                    p.waitFor();
                    p.destroy();;
                } catch(Exception e) {
                    e.printStackTrace();
                }
                p = null;
            }
        }
    }
    public void CMDOutputError(InputStream miCmdStream) {
        BufferedReader miReader = null;
        try {
            miReader = new BufferedReader(new InputStreamReader(miCmdStream));
            String line = "";
            while(true) {
                line = miReader.readLine();
                if(line == null) {
                    break;
                }
                System.out.println("[ ERROR ]: " + line);
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
                System.out.println("[ INFO ]: " + line);
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
    private String getMainClassName() {
        String name = FileUtils.getMainClass(localPath);
        if(name.isEmpty()) {
            try {
            String
                localParent = new File(localPath).getCanonicalPath(),
                localName = new File(localParent).getName();
            name = localName;
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
        return name;
    }
    public void createProyectFiles(String author)  {
        try {
            String 
                mainDirName = new File(localPath).getCanonicalPath(),
                mainClass = new File(mainDirName).getName();
            fileOperation.createFiles(author, ".gitignore", "", false);
            fileOperation.createFiles(author, "Manifesto.txt", mainClass, false);
            fileOperation.createFiles(author, mainClass + ".java", mainClass, false);
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
                fileOperation.listSRCDirectories("src")
                    .parallelStream()
                    .filter(e -> !e.isEmpty())
                    .forEach(e -> {
                        int countFiles = fileUtils.countFilesInDirectory(new File(e));
                        if(countFiles > 0) {
                            names.add(e + "*.java ");
                        }
                    });
            } else {
                System.out.println("[ INFO ]: " + localPath + "\\src\\ folder not found");
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
    private String compileFormatType(String target, String libFiles) {
        StringBuffer compile = new StringBuffer();
        if(target.isEmpty() && libFiles.isEmpty()) {
            compile.append("javac -Werror -Xlint:all -d .\\bin\\ ");
        } else if(target.isEmpty() && !libFiles.isEmpty()) {
            compile.append("javac -Werror -Xlint:all -d .\\bin\\ -cp ");
        } else if(!target.isEmpty() && libFiles.isEmpty()) {
            compile.append("javac -Werror -Xlint:all -d ");
            compile.append(new File(target).getPath());
            compile.append(" ");
        }
        return compile.toString();
    }
    public String createCompileClases(ArrayList<String> libJars, String srcClases, String target) {
        // create jar files command for compile operation
        StringBuffer 
            libFiles = new StringBuffer(),
            cLibFiles = new StringBuffer(),
            compile = new StringBuffer();
        // lib files
        libFiles.append(
                libJars
                .stream()
                .filter(e -> !e.isEmpty())
                .map(e -> e + ";")
                .collect(Collectors.joining())
        );
        String
            mainClass = FileUtils.getMainClass(localPath),
            format = compileFormatType(target, libFiles.toString());

        if(!mainClass.isEmpty() && libFiles.isEmpty()) {
            compile.append(format);
            compile.append(".\\src\\*.java -sourcepath .\\src\\");
        } else if(!mainClass.isEmpty() && !libFiles.isEmpty()) {
            String cb = libFiles.substring(0, libFiles.length()-1);
            cLibFiles.append("'" + cb + "' " + srcClases);
            compile.append(format);
            compile.append(cLibFiles);
            compile.append(" .\\src\\*.java -sourcepath .\\src\\");
        } else if(mainClass.isEmpty() && libFiles.isEmpty()) {
            compile.append(format);
             compile.append(srcClases);
        } else if(mainClass.isEmpty() && !libFiles.isEmpty()) {
            String cb = libFiles.substring(0, libFiles.length()-1);
            cLibFiles.append("'" + cb + "' " + srcClases);
            compile.append(format);
            compile.append(cLibFiles);
        }
        return compile.toString();
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
    public List<String> createExtractionCommand() throws IOException {
        File extractionFile = new File(localPath + "\\extractionFiles");
        List<String> commands = new ArrayList<>();

        fileUtils.listFilesFromPath(extractionFile.getPath())
            .parallelStream()
            .filter(e -> e.getName().contains(".jar"))
            .forEach(e -> {
                String 
                    jarFileName = e.getName(),
                    jarParent   = e.getParent(),
                    extracJAR   = "jar -xf " + jarFileName,
                    deleteJAR   = "rm -r " + jarFileName + "\n";
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
        if(!manifestoIsCreated() && mainName.isEmpty()) {
            jarFormat = "jar -cf ";
        }
        if(!manifestoIsCreated() && !mainName.isEmpty()) {
            jarFormat = "jar -cfe ";
        }
        return jarFormat;
    }
    private String jarTypeUnion(String mainName, String directory) throws IOException {
        StringBuffer build = new StringBuffer();
        String 
            localParent = new File(localPath).getCanonicalPath(),
            jarFormat = jarTypeFormat(mainName, directory),
            mainClassName = getMainClassName();

        switch(jarFormat) {
            case "jar -cfm ":
                if(mainName != "" && directory != "") {
                    build.append(jarFormat);
                    build.append(mainName);
                    build.append(" Manifesto.txt -C .\\bin\\ .");
                    build.append(directory);
                } else if(mainName != "" && directory == "") {
                    build.append(jarFormat);
                    build.append(mainName);
                    build.append(" Manifesto.txt -C .\\bin\\ .");
                }
                break;
            case "jar -cf ":
                String jarName = new File(localParent).getName() + ".jar";
                if(directory != "") {
                    build.append(jarFormat);
                    build.append(jarName);
                    build.append(" -C .\\bin\\ .");
                    build.append(directory);
                } else {
                    build.append(jarFormat);
                    build.append(jarName);
                    build.append(" -C .\\bin\\ .");
                }
                break;
            case "jar -cfe ":
                if(directory != "") {
                    build.append(jarFormat);
                    build.append(mainName);
                    build.append(" ");
                    build.append(mainClassName);
                    build.append(" -C .\\bin\\ .");
                    build.append(directory);
                } else {
                    build.append(jarFormat);
                    build.append(mainName);
                    build.append(" ");
                    build.append(mainClassName);
                    build.append(" -C .\\bin\\ .");
                }
                break;
        }
        return build.toString();
    }
    public String createJarFileCommand(boolean includeExtraction) throws IOException {
        String
            mainName = getMainClassName() + ".jar",
            command = "",
            directory = "";

        File extractionFile = new File(localPath + "\\extractionFiles");

        if(extractionFile.exists() && extractionFile.listFiles() != null) {
            for(File extractionDir: extractionFile.listFiles()) {
                directory += " -C " + extractionDir.getPath() + "\\ .";
            }
        } 
        if(includeExtraction) {
            command = jarTypeUnion(mainName, directory);
        } else {
            command = jarTypeUnion(mainName, "");
        }
        return command;
    }
    public boolean createAddJarFileCommand(String jarFilePath) throws Exception {
        System.out.println("[ INFO ]: adding jar dependency in process ...");
        String sourceFilePath = "";
        boolean isAdded = false;
        File jarFile = new File(jarFilePath);
        if(!jarFile.exists()) {
            throw new Exception("[ ERROR ]: jar file not found");
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
            System.out.println("[ INFO ]: DEPENDENCY ALREADY INSIDE THE PROYECT");
        }
        return isAdded;
    }
    private StringBuffer runClassOption(String className, String mainName) {
        StringBuffer runClass = new StringBuffer();
        if(className.isEmpty() || className == null) {
            runClass.append(" .\\src\\" + mainName);
        } else if(className.equals(mainName)) {
            runClass.append(" .\\src\\" + mainName);
        } else {
            runClass.append(" " + className);
        }
        return runClass;
    }
    public String createRunCommand(ArrayList<String> libJars, String className) {
        String 
            command  = "",
            mainName = getMainClassName() + ".java";
        StringBuffer 
            jarFiles = new StringBuffer(),
            runClass = runClassOption(className, mainName);
        jarFiles.append("'.\\bin\\;");
        jarFiles.append(libJars
                .parallelStream()
                .filter(e -> !e.isEmpty())
                .map(e -> e + ";")
                .collect(Collectors.joining())
        );
        if(jarFiles.isEmpty()) {
            command = "java -XX:+ExtensiveErrorReports -d .\\bin\\" + runClass;
        } else {
            String cleanLibs = jarFiles.substring(0, jarFiles.length()-1) + "'";
            command = "java -XX:+ExtensiveErrorReports -cp " + cleanLibs + runClass;
        }
        return command;
    }
    public void createBuildCommand(boolean includeExtraction) {
        String mainName = FileUtils.getMainClass(localPath);
        if(!mainName.isEmpty()) {
            mainName = mainName + ".jar";
        }
        fileOperation.createFiles("", "java-exe.ps1", mainName, includeExtraction);
        System.out.println("[ INFO ]: Adding build script ...");
    }
}
