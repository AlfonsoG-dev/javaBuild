package Utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.File;

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileUtils {
    public void writeManifesto(File localFile, String fileName, boolean includeExtraction, String libFiles) {
        try {
            FileWriter writeManifesto = new FileWriter(localFile.getPath() + "\\" + fileName);
            if(includeExtraction == true) {
                writeManifesto.write(
                        "Manifest-Version: 1.0" + "\n" + 
                        "Created-By: Alfonso-Gomajoa" + "\n" + 
                        "Main-Class: " + FileUtils.getMainClass(localFile.getPath()) + "\n"
                );
            } else {
                writeManifesto.write(
                        "Manifest-Version: 1.0" + "\n" + 
                        "Created-By: Alfonso-Gomajoa" + "\n" + 
                        "Main-Class: " + FileUtils.getMainClass(localFile.getPath()) + "\n" + 
                        "Class-Path: " + libFiles + "\n"
                );
            }
            writeManifesto.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    public String getCleanPath(String filePath) {
        String build = "";
        if(filePath.contains(".\\")) {
            build = new File(filePath).toPath().normalize().toString();
        } else {
            build = filePath;
        }
        return build;
    }
    public int countFilesInDirectory(File myDirectory) {
        int count = -1;
        try {
            if(myDirectory.listFiles() != null) {
                for(File f: myDirectory.listFiles()) {
                    if(f.isFile()) {
                        ++count;
                    }
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        return count;
    }
    public String getDirectoryFiles(DirectoryStream<Path> misFiles) {
        String fileNames = "";
        try {
            for(Path p: misFiles) {
                File f = p.toFile();
                if(f.exists() && f.isFile()) {
                    fileNames += f.getCanonicalPath() + "\n";
                } else if(f.isDirectory()) {
                    fileNames += this.getDirectoryFiles( Files.newDirectoryStream(f.toPath()));
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        return fileNames;
    }
    public String listFilesFromPath(String filePath) {
        String fileNames = "";
        try {
            File miFile = new File(filePath);
            if(miFile.exists() && miFile.isFile()) {
                fileNames += miFile.getCanonicalPath() + "\n";
            } else if(miFile.listFiles() != null) {
                fileNames += getDirectoryFiles(Files.newDirectoryStream(miFile.toPath()));
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        return fileNames;
    }
    public String listFilesFromDirectory(DirectoryStream<Path> files) {
        String fileNames = "";
        try {
            for(Path p: files) {
                File f = p.toFile();
                if(f.isFile()) {
                    fileNames += f.getPath() + "\n";
                } else {
                    this.listFilesFromPath(f.getPath());
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        return fileNames;
    }
    public void createDirectory(String directory) {
        File miFile = new File(directory);
        if(miFile.exists() == false) {
            miFile.mkdir();
        }
    }
    public String createTargetFromParentPath(String parentFile, String dirs) {
        String parentName = new File(parentFile).getParent();
        String targetNames = dirs.replace(parentName, "");
        return targetNames;
    }
    public void createParentFile(String targetFilePath, String parentFileNames) {
        try {
            String[] parentNames = parentFileNames.split("\n");
            for(String pn: parentNames) {
                String nFileName = pn.replace(targetFilePath.replace("/", "\\"), "");
                File mio = new File(pn);
                int fileLenght = nFileName.split("\\\\").length;
                if(mio.exists() == false && fileLenght > 1) {
                    mio.mkdirs();
                } else if(mio.exists() == false && fileLenght <= 1) {
                    mio.mkdir();
                }
                System.out.println("directorio creado: " + mio.getName());
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * main class of the proyect
     * @param localpath: local path
     * @return main class file name
     */
    public static String getMainClass(String localpath) {
        File miFile = new File(localpath + "\\src");
        BufferedReader miBufferedReader = null;
        String mainName = "";
        try {
            String localName = new File(localpath).getCanonicalPath();
            String parentName = new File(localName).getName();
            if(miFile.listFiles() != null) {
            outter: for(File f: miFile.listFiles()) {
                    if(f.isFile() && f.getName().contains(".java") && f.getName().equals(parentName + ".java")) {
                        miBufferedReader = new BufferedReader(new FileReader(f));
                        while(miBufferedReader.read() != -1) {
                            if(miBufferedReader.readLine().contains("static void main(String[] args)")) {
                                mainName = f.getName().split(".java")[0];
                                break outter;
                            }
                        }
                    }
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            if(mainName == "") {
                try {
                    String parentName = new File(localpath).getCanonicalPath();
                    String localName = new File(parentName).getName();
                    mainName = localName;
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
            if(miBufferedReader != null) {
                try {
                    miBufferedReader.close();
                } catch(Exception e) {
                    System.err.println(e);
                }
                miBufferedReader = null;
            }
        }
        return mainName;
    }
}
