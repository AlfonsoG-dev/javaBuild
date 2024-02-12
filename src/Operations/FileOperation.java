package Operations;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.Path;

import Utils.FileUtils;
import Utils.OperationUtils;
public class FileOperation {

    private FileUtils fileUtils;
    private String localPath;
    public FileOperation(String nLocalPath) {
        fileUtils = new FileUtils();
        localPath = nLocalPath;
    } 
    public void createFiles(String fileName, String mainClass, boolean includeExtraction) {
        try {
            File localFile = new File(localPath);
            OperationUtils utils = new OperationUtils(localFile.getPath());
            File miFile = new File(localPath + "\\src");
            if(fileName.equals(".gitignore")) {
                FileWriter miFileWriter = new FileWriter(localFile.getPath() + "\\" + fileName);
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
                String libJars = utils.libJars();
                fileUtils.writeManifesto(localFile, "Manifesto.txt", includeExtraction, libJars);
            } else if(fileName.equals(mainClass + ".java")) {
                FileWriter writeMainClass = new FileWriter(miFile.getPath() + "\\" + fileName);
                writeMainClass.write(
                        "class " + mainClass + " {\n" +
                        "    public static void main(String[] args) {\n" + 
                        "        System.out.println(\"Hello from " + mainClass + "\");" + "\n" +
                        "    }\n" + 
                        "}"
                );
                writeMainClass.close();
            } else if(fileName.equals("java-exe.ps1")) {
                fileUtils.writeBuildFile(localFile, fileName, mainClass, includeExtraction);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    public String listLibFiles() {
        String names = "";
        try {
            File libFile = new File(localPath + "\\lib");
            if(libFile.exists() && libFile.listFiles() != null) {
                DirectoryStream<Path> libFiles = Files.newDirectoryStream(libFile.toPath());
                for(Path p: libFiles) {
                    File f = p.toFile();
                    for(File mf: f.listFiles()) {
                        names += mf.getPath() + "\n";
                    }
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        return names;
    }
    public String listSRCDirectories(String path) throws IOException {
        String cPath = fileUtils.getCleanPath(path);
        File localFile = new File(localPath + "\\" + cPath);
        String names = "";
        if(localFile.listFiles() != null) {
            DirectoryStream<Path> localFiles = Files.newDirectoryStream(localFile.toPath());
            for(Path p: localFiles) {
                File f = p.toFile();
                if(f.isDirectory()) {
                    names += f.getPath() + "\\" + "\n";
                    if(f.listFiles() != null) {
                        names += listSRCDirectories(f.getPath());
                    }
                }
            }
        }
        return names;
    }
    public boolean extractionDirContainsPath(String libJars) throws IOException {
        boolean containsPath = false;
        File extractionFile = new File(localPath + "\\extractionFiles");
        File miFile = new File(libJars);
        if(miFile.getParent() != null && extractionFile.listFiles() != null) {
            String jarLibParent = miFile.getParent();
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
            if(new File(sourceFilePath).isFile()) {
                String sourceFileName = new File(sourceFilePath).getName();
                String sourceParent = new File(sourceFilePath).getParent();
                String sourceParentName = new File(sourceParent).getName();
                File targetFile = new File(
                        targetFilePath + "\\" +
                        sourceParentName + "\\" +
                        sourceFileName
                );
                fileUtils.createParentFile(
                        targetFile.getPath(),
                        targetFile.getParent()
                );
                System.out.println(
                        Files.copy(
                            new File(sourceFilePath).toPath(),
                            targetFile.toPath(),
                            StandardCopyOption.COPY_ATTRIBUTES
                        )
                );
            } else if(new File(sourceFilePath).isDirectory()) {
                String[] fileNames = fileUtils.listFilesFromPath(sourceFilePath).split("\n");
                for(String fn: fileNames) {
                    File sourceFile = new File(fn);
                    String cTargetNames = fileUtils.createTargetFromParentPath(
                            sourceFilePath,
                            sourceFile.getCanonicalPath()) + ";";
                    String[] names = cTargetNames.split(";");
                    for(String n: names) {
                        if(n.contains("git") == false) {
                            File targetFile = new File(targetFilePath + "\\" + n);
                            fileUtils.createParentFile(targetFilePath, targetFile.getParent());
                            Path sourcePath = sourceFile.toPath();
                            Path targetPath = targetFile.toPath();
                            System.out.println(
                                    Files.copy(
                                        sourcePath,
                                        targetPath,
                                        StandardCopyOption.COPY_ATTRIBUTES
                                    )
                            );
                        }
                    }
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
