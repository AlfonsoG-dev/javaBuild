package Operations;

import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.Path;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import Utils.CommandUtils;
import Utils.FileUtils;
public class FileOperation {

    private FileUtils fileUtils;
    private String localPath;
    public FileOperation(String nLocalPath) {
        localPath = nLocalPath;
        fileUtils = new FileUtils(localPath);
    }

    public List<String> listSRCDirectories(String path) throws IOException {
        List<String> names = new ArrayList<>();
        Thread t = new Thread(new Runnable() {
            public void run() {
                try {
                    String c = fileUtils.getCleanPath(path);
                    File lf = new File(localPath + File.separator + c);
                    if(lf.listFiles() != null) {
                        Files.newDirectoryStream(lf.toPath())
                            .forEach(e -> {
                                File f = e.toFile();
                                if(f.isDirectory()) {
                                    names.add(f.getPath() + File.separator);
                                    if(f.listFiles() != null) {
                                        try {
                                            names.addAll(
                                                    listSRCDirectories(f.getPath())
                                            );
                                        } catch(Exception err) {
                                            err.printStackTrace();
                                        }
                                    }
                                }
                            });
                    }
                } catch(IOException err) {
                    err.printStackTrace();
                }
            }
        });
        t.start();
        try {
            t.join();
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
        return names;
    }
    public List<String> listLibFiles() {
        List<String> names = new ArrayList<>();
        Thread t = new Thread(new Runnable() {
            public void run() {
                try {
                    File lf = new File(localPath + File.separator + "lib");
                    if(lf.listFiles() != null) {
                        Files.newDirectoryStream(lf.toPath())
                            .forEach(e -> {
                                File f = e.toFile();
                                for(File mf: f.listFiles()) {
                                    names.add(mf.getPath());
                                }
                            });
                    }
                } catch(IOException e) {
                    e.printStackTrace();
                }
            }
        });
        t.start();
        try {
            t.join();
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
        return names;
    }

    /**
     * main class of the proyect
     * @param localpath: local path
     * @return main class file name
     */
    public String getMainClass(String localpath) {
        File miFile = new File(localpath + File.separator + "src");
        BufferedReader miBufferedReader = null;
        String mainName = "";
        try {
            if(miFile.listFiles() != null) {
            outter: for(File f: miFile.listFiles()) {
                    if(f.isFile() && f.getName().contains(".java")) {
                        miBufferedReader = new BufferedReader(new FileReader(f));
                        while(miBufferedReader.read() != -1) {
                            if(miBufferedReader.readLine().contains("public static void main(String[] args)")) {
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

    public String getProjectName() {
        String name = getMainClass(localPath);
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

    public boolean haveManifesto() {
        return new File(localPath + File.separator + "Manifesto.txt").exists();
    }
    /**
     * creates the manifesto file for the jar file creation
     * @param fileName: path where the manifesto is created
     * @param includeExtraction: true if you want the lib files as part of the jar file, false otherwise
     * @param libFiles: the lib files to include in the build
     */
    public void writeManifesto(boolean includeExtraction, String libFiles, String authorName) {
        String
            author    = authorName.trim(),
            mainClass = getMainClass(localPath);

        StringBuffer m = new StringBuffer();

        m.append("Manifest-Version: 1.0");
        m.append("\n");

        if(!author.isEmpty()) {
            m.append("Created-By: ");
            m.append(author);
            m.append("\n");
        }
        if(!mainClass.isEmpty()) {
            m.append("Main-Class: ");
            m.append(mainClass);
            m.append("\n");
        }
        if(!libFiles.isEmpty() && !includeExtraction) {
            m.append("Class-Path: ");
            m.append(libFiles);
            m.append("\n");
        }

        // write lines to file
        fileUtils.writeToFile(m.toString(), "Manifesto.txt");
    }
    /**
     * create the sentences for the build script
     * @param fileName: path where the build script is created
     * @param mainClass: main class name
     * @param extract: true if you want to include the lib files as part of the jar file, false otherwise
     * @throws IOException: exception while trying to create the build script
     */
    private void writeBuildFile(String fileName, String mainClass, String source, String target, boolean extract) throws IOException {
        Command myCommand = new Command(localPath);
        List<String> dirNames = listSRCDirectories(source);
        List<String> libNames = listLibFiles();

        StringBuffer sourceFiles = new StringBuffer("." + File.separator + source + File.separator + "*.java ");
        String
            libFiles = "",
            compile = "javac -Werror -Xlint:all -d ." + File.separator + target + File.separator,
            runJar = "",
            runCommand = "";

        sourceFiles.append(
                dirNames
                .parallelStream()
                .filter(e -> fileUtils.countFilesInDirectory(new File(e)) > 0)
                .map(e -> e + "*.java ")
                .collect(Collectors.joining())
        );


        libFiles += libNames
            .stream()
            .map(e -> e + ";")
            .collect(Collectors.joining());

        if(!libFiles.isEmpty()) {
            compile += " -cp '$libFiles' $srcClases";
        } else {
            compile += " $srcClases";
        }
        if(!mainClass.isEmpty()) {
            runJar = FileUtils.getJavaScriptCommand(mainClass);
            runCommand = FileUtils.getRunScriptCommand();
        } else {
            runCommand = FileUtils.getBuildScriptCommand();
        }
        try(FileWriter w = new FileWriter(localPath + File.separator + fileName)){
            FileUtils.writeScript(
                    fileName,
                    w,
                    sourceFiles.toString(),
                    libFiles,
                    compile,
                    myCommand.getJarFileCommand(extract, ""),
                    runJar,
                    runCommand
            );
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    public void createFiles(String author, String fileName, String mainClass, boolean includeExtraction) {
        System.out.println("[ Info ]: created " + fileName);
        if(fileName.equals(".gitignore")) {
            String ignoreFiles = "";
            ignoreFiles = "**bin" + "\n" +
                "**lib" + "\n" +
                "**extractionFiles" + "\n" +
                "**Manifesto.txt" + "\n" +
                "**Session.vim" + "\n" +
                "**.jar" + "\n" +
                "**.exe";
            fileUtils.writeToFile(ignoreFiles, fileName);
        } else if(fileName.equals("Manifesto.txt")) {
            String libJars = "";
            List<String> jars = new CommandUtils(localPath).getLibFiles();
            libJars += jars
                .parallelStream()
                .filter(e -> !e.isEmpty())
                .map(e -> e + ";")
                .collect(Collectors.joining());
            writeManifesto(includeExtraction, libJars, author);
        } else if(fileName.equals(mainClass + ".java")) {
            String mainClassLines = "";
            mainClassLines = "class " + mainClass + " {\n" +
                "    public static void main(String[] args) {\n" + 
                "        System.out.println(\"Hello from " + mainClass + "\");" + "\n" +
                "    }\n" + 
                "}";
            fileUtils.writeToFile(mainClassLines, fileName);
        } else if(fileName.contains(".ps1") || fileName.contains(".sh")) {
            // write build script lines
            try {
                writeBuildFile(
                        fileName,
                        mainClass,
                        "src",
                        "target",
                        includeExtraction
                );
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
    }
    public boolean extractionDirContainsPath(String libJarPath) throws IOException {
        boolean containsPath = false;
        File extractionFile = new File(localPath + File.separator + "extractionFiles");
        File myFile = new File(libJarPath);
        if(myFile.getParent() != null && extractionFile.listFiles() != null) {
            String jarLibParent = myFile.getParent();
            String jarNameParent = new File(jarLibParent).getName();
            for(File f: extractionFile.listFiles()) {
                String extractionDirName = new File(f.getCanonicalPath()).getName();
                if(extractionDirName.equals(jarNameParent)) {
                    containsPath = true;
                }
            }
        }
        return containsPath;
    }
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
                fileUtils.listFilesFromPath(sourceFilePath)
                    .parallelStream()
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
