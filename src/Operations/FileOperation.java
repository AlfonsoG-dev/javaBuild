package Operations;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.Path;

import java.util.ArrayList;
import java.util.stream.Collectors;

import Utils.FileUtils;
import Utils.OperationUtils;
public class FileOperation {

    private FileUtils fileUtils;
    private String localPath;
    public FileOperation(String nLocalPath) {
        localPath = nLocalPath;
        fileUtils = new FileUtils(localPath);
    } 
    public void createFiles(String fileName, String mainClass, boolean includeExtraction) {
        System.out.println("[ INFO ] created: " + fileName);
        try {
            File
                localFile = new File(localPath),
                miFile    = new File(localPath + "\\src");
            FileWriter 
                miFileWriter = null,
                writeMainClass = null;
            if(fileName.equals(".gitignore")) {
                miFileWriter = new FileWriter(localFile.getPath() + "\\" + fileName);
                miFileWriter.write(
                        "**bin" + "\n" +
                        "**lib" + "\n" +
                        "**extractionFiles" + "\n" +
                        "**Manifesto.txt" + "\n" +
                        "**Session.vim" + "\n" +
                        "**.jar" + "\n" +
                        "**.exe"
                );
                miFileWriter.close();
            } else if(fileName.equals("Manifesto.txt")) {
                String libJars = "";
                ArrayList<String> jars = new OperationUtils(localPath).libJars();
                libJars += jars
                    .parallelStream()
                    .filter(e -> !e.isEmpty())
                    .map(e -> e + ";")
                    .collect(Collectors.joining());
                fileUtils.writeManifesto("Manifesto.txt", includeExtraction, libJars);
            } else if(fileName.equals(mainClass + ".java")) {
                writeMainClass = new FileWriter(miFile.getPath() + "\\" + fileName);
                writeMainClass.write(
                        "class " + mainClass + " {\n" +
                        "    public static void main(String[] args) {\n" + 
                        "        System.out.println(\"Hello from " + mainClass + "\");" + "\n" +
                        "    }\n" + 
                        "}"
                );
                writeMainClass.close();
            } else if(fileName.equals("java-exe.ps1")) {
                fileUtils.writeBuildFile(
                        fileName,
                        mainClass,
                        includeExtraction
                );
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    public ArrayList<String> listLibFiles() {
        ArrayList<String> names = new ArrayList<>();
        Thread t = new Thread(new Runnable() {
            public void run() {
                try {
                    File lf = new File(localPath + "\\lib");
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
    public ArrayList<String> listSRCDirectories(String path) throws IOException {
        ArrayList<String> names = new ArrayList<>();
        Thread t = new Thread(new Runnable() {
            public void run() {
                try {
                    String c = fileUtils.getCleanPath(path);
                    File lf = new File(localPath + "\\" + c);
                    if(lf.listFiles() != null) {
                        Files.newDirectoryStream(lf.toPath())
                            .forEach(e -> {
                                File f = e.toFile();
                                if(f.isDirectory()) {
                                    names.add(f.getPath() + "\\");
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
    public boolean extractionDirContainsPath(String libJarPath) throws IOException {
        boolean containsPath = false;
        File extractionFile = new File(localPath + "\\extractionFiles");
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
                        targetFilePath + "\\" +
                        sourceParentName + "\\" +
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
                ArrayList<File> fileNames = fileUtils.listFilesFromPath(sourceFilePath);
                fileNames
                    .parallelStream()
                    .forEach(e -> {
                        try {
                            String n = fileUtils.createTargetFromParentPath(
                                    sourceFilePath,
                                    e.getCanonicalPath()
                            );
                            if(n.contains("git") == false) {
                                File targetFile = new File(targetFilePath + "\\" + n);
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
