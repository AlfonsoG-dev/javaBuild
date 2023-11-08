package Operations;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.Path;

import Utils.FileUtils;
public class FileOperation {

    private FileUtils fileUtils;
    private String localPath;
    public FileOperation(String nLocalPath) {
        fileUtils = new FileUtils();
        localPath = nLocalPath;
    }
    public String listLibFiles() {
        String names = "";
        try {
            File localFile = new File(localPath);
            for(File f: localFile.listFiles()) {
                if(f.getName().equals("lib")) {
                    for(File mf: f.listFiles()) {
                        names += fileUtils.listFilesFromPath(mf.getPath());
                    }
                }
            }
        } catch(Exception e) {
            System.err.println(e);
        }
        return names;
    }
    public String listSRCDirectories(String path) throws IOException {
        String cPath = fileUtils.GetCleanPath(path);
        File localFile = new File(localPath + "\\" + cPath);
        String names = "";
        for(File f: localFile.listFiles()) {
            if(f.isDirectory()) {
                names += f.getPath() + "\\" + "\n";
            } 
            if(f.isDirectory() && f.listFiles().length > 0) {
                for(File ml: f.listFiles()) {
                    if(ml.isDirectory()) {
                        names += listSRCDirectories(ml.getPath()) + "\n";
                    }
                }
            }
        }
        String cNames = fileUtils.GetCleanPath(names);
        return cNames;
    }
    public void DeleteDirectories(String filePath) {
        try {
            File localFile = new File(localPath);
            String cFile = fileUtils.GetCleanPath(filePath);
            File miFile = new File(localFile.getCanonicalPath() + "\\" + cFile);
            if(miFile.isFile()) {
                if(miFile.delete() == true) {
                    System.out.println("se elimino el archivo: " + miFile.getName());
                }
            }
            if(miFile.isDirectory() && miFile.listFiles().length >0) {
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
    public void DeleteFiles(String filePath, String extension) {
        try {
            File miFile = new File(filePath);
            if(miFile.isFile() && miFile.getName().contains(extension)) {
                miFile.delete();
            } else {
                for(File f: miFile.listFiles()) {
                    if(f.isFile() && f.getName().contains(extension)) {
                        f.delete();
                    } else {
                        for(File fl: f.listFiles()) {
                            this.DeleteFiles(fl.getCanonicalPath(), extension);
                        }
                    }
                }
            }
        } catch(Exception e) {
            System.err.println(e);
        }
    }
    public void CopyFilesfromSourceToTarget(String sourceFilePath, String targetFilePath) {
        try {
            String[] fileNames = fileUtils.listFilesFromPath(sourceFilePath).split("\n");
            for(String fn: fileNames) {
                File sourceFile = new File(fn);
                String cTargetNames = fileUtils.CreateTargetFromParentPath(sourceFile.getCanonicalPath()) + ";";
                String[] names = cTargetNames.split(";");
                for(String n: names) {
                    File targetFile = new File(targetFilePath + "\\" + n);
                    if(targetFile.exists() == false) {
                        fileUtils.CreateParentFile(targetFilePath, targetFile.getParent());
                        Path sourcePath = sourceFile.toPath();
                        Path targetPath = targetFile.toPath();
                        System.out.println( Files.copy(sourcePath, targetPath, StandardCopyOption.COPY_ATTRIBUTES));
                    }
                }
            }
        } catch(Exception e) {
            System.err.println(e);
        }
    }
}
