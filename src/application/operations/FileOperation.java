package operations;

import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import builders.ScriptBuilder;
import utils.FileUtils;
public class FileOperation {

    private FileUtils fileUtils;
    private String localPath;
    private ScriptBuilder scriptBuilder;
    private ExecutorOperation executor;

    public FileOperation(String nLocalPath) {
        localPath = nLocalPath;
        this.fileUtils = new FileUtils(nLocalPath);
        scriptBuilder = new ScriptBuilder(nLocalPath);
        executor = new ExecutorOperation();
    }

    public FileOperation(String nLocalPath, FileUtils fileUtils) {
        localPath = nLocalPath;
        this.fileUtils = fileUtils;
        scriptBuilder = new ScriptBuilder(nLocalPath);
        executor = new ExecutorOperation();
    }
    /**
     * list of the directories inside the source folder that at least have one .java file
     * @param source where the .java files are
     * @return the list of directories
     */
    public List<String> listSourceDirs(String source) {
        return executor.executeConcurrentCallableList(fileUtils.listDirectoryNames(source));
    }
    /**
     * list of the files inside the lib folder
     * @return the list of lib files
     */
    public List<String> listLibFiles() {
        List<String> names = new ArrayList<>();
        File lf = fileUtils.resolvePaths(localPath, "lib");
        if(lf.listFiles() != null) {
            try {
                names = Files.walk(lf.toPath(), 2, FileVisitOption.FOLLOW_LINKS)
                .filter(Files::isRegularFile)
                .map(p -> p.toFile().getPath())
                .toList();
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
        return names;
    }

    /**
     * main class of the project
     * @throws IOException if the file can't be read.
     * @return main class file name or empty
     */
    public String getMainClass(String source) {
        File miFile = fileUtils.resolvePaths(localPath, source);
        BufferedReader miBufferedReader = null;
        String mainName = "";
        try {
            if(miFile.listFiles() != null) {
            outter: for(File f: miFile.listFiles()) {
                    if(f.isFile() && f.getName().contains(".java")) {
                        miBufferedReader = new BufferedReader(new FileReader(f));
                        String line;
                        while((line = miBufferedReader.readLine()) != null) {
                            if(line.contains("public static void main(String[] args)")) {
                                mainName = f.getName().replace(".java", "");
                                break outter;
                            }
                        }
                    }
                }
            }
        } catch(IOException e) {
            e.printStackTrace();
        } finally {
            if(miBufferedReader != null) {
                try {
                    miBufferedReader.close();
                } catch(IOException e) {
                    e.printStackTrace();
                }
                miBufferedReader = null;
            }
        }
        return mainName;
    }
    /**
     * if the main class is empty use the project name as main class
     * @param source where the .java files are
     * @return the project name
     */
    public String getProjectName(String source) {
        String name = getMainClass(source);
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
    /**
     * to verify if the manifesto is present in the project
     * @return true if exists, false otherwise
     */
    public boolean haveManifesto() {
        return fileUtils.fileExists(fileUtils.resolvePaths(localPath, "Manifesto.txt").getPath());
    }
    public void createIgnoreFile(String fileName) {
        String ignoreFiles = "";
        ignoreFiles = "**bin" + "\n" +
            "**lib" + "\n" +
            "**extractionFiles" + "\n" +
            "**Manifesto.txt" + "\n" +
            "**Session.vim" + "\n" +
            "**.jar" + "\n" +
            "**.exe";
        fileUtils.writeToFile(ignoreFiles, fileName);
    }
    /**
     * create the manifesto file
     * @param source where the .java files are
     * @param author the author of the project
     * @param extract if to include or not the .jar dependencies of lib
     */
    public void createManifesto(String source, String author, boolean extract) {
        StringBuffer libJars = new StringBuffer();
        List<String> jars = listLibFiles()
            .stream()
            .filter(p -> p.contains(".jar"))
            .toList();
        libJars.append( jars
            .stream()
            .map(e -> e + ";")
            .collect(Collectors.joining())
        );
        scriptBuilder.writeManifesto(libJars.toString(), author, getProjectName(source), extract);
    }
    /**
     * create the main class inside the source directory
     * @param source where .jar files are
     * @param fileName the name of the main class
     */
    public void createMainClass(String source, String fileName) {
        String mainClassLines = "";
        String main = getProjectName(source);
        mainClassLines = "class " + main + " {\n" +
            "    public static void main(String[] args) {\n" + 
            "        System.out.println(\"Hello from " + main + "\");" + "\n" +
            "    }\n" + 
            "}";
        String targetSource = fileUtils.resolvePaths(localPath, source).getPath();
        fileUtils.writeToFile(mainClassLines, fileUtils.resolvePaths(targetSource, fileName).getPath());
    }
    /**
     * create the build script .ps1 or .sh
     * @param source where the .java files are
     * @param target where the .class files are
     * @param fileName the name of the script
     * @param extract if to include or not the .jar lib files
     */
    public void createScript(String source, String target, String fileName, boolean extract) {
        // write build script lines
        scriptBuilder.writeBuildFile(
            fileName,
            getProjectName(source),
            source,
            target,
            listSourceDirs(source)
                .stream()
                .map(n -> new File(n))
                .filter(n -> fileUtils.validateContent(n))
                .map(n -> n.getPath() + File.separator + "*.java ")
                .toList(),
            listLibFiles()
                .stream()
                .filter(p -> p.contains(".jar"))
                .toList(),
            extract
        );
    }
    /**
     * verify if a extractFiles directory exists
     * @return true if exists, false otherwise
     */
    public boolean extractionDirContainsPath(String libJarPath) {
        boolean containsPath = false;
        File extractionFile = fileUtils.resolvePaths(localPath, "extractionFiles");
        File myFile = new File(libJarPath);
        if(myFile.getParent() != null && extractionFile.listFiles() != null) {
            String jarLibParent = myFile.getParent();
            String jarNameParent = new File(jarLibParent).getName();
            for(File f: extractionFile.listFiles()) {
                String extractionDirName = new File(f.getPath()).getName();
                if(extractionDirName.equals(jarNameParent)) {
                    containsPath = true;
                }
            }
        }
        return containsPath;
    }
    /**
     * copy the content of one path to another
     * @param sourceFilePath where you store the content to copy
     * @param targetFilePath where to put the copied files
     */
    public void copyFilesfromSourceToTarget(String sourceFilePath, String targetFilePath) {
        try {
            File sf = new File(sourceFilePath);
            if(sf.isFile()) {
                String sourceFileName = sf.getName();
                String sourceParent = sf.getParent();
                String sourceParentName = new File(sourceParent).getName();
                File tf = new File(
                        targetFilePath + File.separator +
                        sourceParentName + File.separator +
                        sourceFileName
                );
                fileUtils.createParentFile(
                        tf.getPath(),
                        tf.getParent()
                );
                System.out.println(
                        Files.copy(
                            sf.toPath(),
                            tf.toPath(),
                            StandardCopyOption.COPY_ATTRIBUTES
                        )
                );
            } else if(sf.isDirectory()) {
                executor.executeConcurrentCallableList(fileUtils.listFilesFromPath(sourceFilePath))
                    .stream()
                    .forEach(e -> {
                        try {
                            String n = fileUtils.createTargetFromParentPath(
                                    sourceFilePath,
                                    e.getCanonicalPath()
                            );
                            if(n.contains("git") == false) {
                                File targetFile = new File(targetFilePath + File.separator + n);
                                fileUtils.createParentFile(targetFilePath, targetFile.getParent());
                                Path sourcePath = e.toPath();
                                Path targetPath = targetFile.toPath();
                                System.out.println(
                                        Files.copy(
                                            sourcePath,
                                            targetPath,
                                            StandardCopyOption.COPY_ATTRIBUTES
                                        )
                                );
                            }
                        } catch(IOException err) {
                            err.printStackTrace();
                        }
                    });
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
}
