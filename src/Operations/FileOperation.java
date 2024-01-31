package Operations;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
    public void createFiles(String fileName, String mainClass) {
        try {
            File localFile = new File(localPath);
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
                FileWriter writeManifesto = new FileWriter(localFile.getPath() + "\\" + fileName);
                writeManifesto.write(
                        "Manifesto-Version: 1.0" + "\n" + 
                        "Created-By: Alfonso-Gomajoa" + "\n" + 
                        "Main-Class: " + mainClass
                );
                writeManifesto.close();
            } else if(fileName.equals(mainClass + ".java")) {
                FileWriter writeMainClass = new FileWriter(miFile.getPath() + "\\" + fileName);
                writeMainClass.write(
                        "class " + mainClass + " {\n" +
                        "    public static void main(String[] args) {\n" + 
                        "        System.out.println(Hello from " + mainClass + ");" + "\n" +
                        "    }\n" + 
                        "}"
                );
                writeMainClass.close();
            } else if(fileName.equals("java-exe.ps1")) {
                FileWriter writeRunScript = new FileWriter(localFile.getPath() + "\\" + fileName);
                OperationUtils utils = new OperationUtils(localPath);
                String srcClases = utils.srcClases();
                String libJars = utils.libJars();
                String compileCommand = utils.createCompileClases(libJars, srcClases);
                String createJarCommand = utils.createJarFileCommand();
                String os = System.getProperty("os.name").toLowerCase();
                if(os.contains("windows")) {
                    writeRunScript.write(
                            "$compile = " + "\"" + compileCommand + "\"" + "\n" + 
                            "$createJar = " + "\"" + createJarCommand + "\"" + "\n" + 
                            "$javaCommand = \"java -jar " + mainClass + "\""  + "\n" +
                            "$runCommand = " + "\"$compile\" +" + " \" && \" +" + " \"$createJar\" +" + " \" && \" +" +
                            "\"$javaCommand\"" + "\n" + 
                            "Invoke-Expression $runCommand"
                    );
                } else if(os.contains("linux")) {
                    // TODO: create support for linux
                    writeRunScript.close();
                    throw new Exception("Not implemented yet");
                } else {
                    System.out.println("! OS NOT SUPPORTED ¡");
                }
                writeRunScript.close();
            }
        } catch(Exception e) {
            System.err.println(e);
        }
    }
    public String listLibFiles() {
        String names = "";
        try {
            File libFile = new File(localPath + "\\lib");
            if(libFile.exists() && libFile.listFiles() != null) {
                for(File f: libFile.listFiles()) {
                    for(File mf: f.listFiles()) {
                        names += mf.getPath() + "\n";
                    }
                }
            }
        } catch(Exception e) {
            System.err.println(e);
        }
        return names;
    }
    public String listSRCDirectories(String path) throws IOException {
        String cPath = fileUtils.getCleanPath(path);
        File localFile = new File(localPath + "\\" + cPath);
        String names = "";
        if(localFile.listFiles() != null) {
            for(File f: localFile.listFiles()) {
                if(f.isDirectory()) {
                    names += f.getPath() + "\\" + "\n";
                }
                if(f.isDirectory() && f.listFiles() != null) {
                    for(File mf: f.listFiles()) {
                        if(mf.isDirectory()) {
                            names += mf.getPath() + "\\" + "\n";
                        }
                        if(mf.isDirectory() && mf.listFiles() != null) {
                            names += listSRCDirectories(mf.getCanonicalPath());
                        }
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
        if(miFile.getParent() != null) {
            String jarLibParent = miFile.getParent();
            String jarNameParent = new File(jarLibParent).getName();
            if(extractionFile.listFiles() != null) {
                for(File f: extractionFile.listFiles()) {
                    String extractionDirName = new File(f.getCanonicalPath()).getName();
                    if(extractionDirName.equals(jarNameParent)) {
                        containsPath = true;
                    }
                }
            }
        }
        return containsPath;
    }
    public void deleteDirectories(String filePath) {
        try {
            File localFile = new File(localPath);
            String cFile = filePath;
            File miFile = new File(localFile.getCanonicalPath() + "\\" + cFile);
            if(miFile.isFile()) {
                if(miFile.delete() == true) {
                    System.out.println("se elimino el archivo: " + miFile.getName());
                }
            } else if(miFile.isDirectory() && miFile.listFiles().length >0) {
                boolean b = false;
                File[] files = miFile.listFiles();
                for(File f: files) {
                    b = f.delete();
                    if(b == true) {
                        System.out.println("se elimino el elemento: " + f);
                    }
                }
                if(b == true && miFile.delete() == true) {
                    System.out.println("se elimino el directorio: " + miFile);
                }
            } else if(miFile.listFiles().length == 0) {
                if(miFile.delete() == true) {
                    System.out.println("se elimino el directorio: " + miFile);
                }
            }
        } catch(Exception e) {
            System.err.println(e);
        }
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
            System.err.println(e);
        }
    }
}
