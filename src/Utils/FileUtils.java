package Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
public class FileUtils {
    public String GetCleanPath(String filePath) {
        String build = "";
        if(filePath.startsWith("\\.")) {
            build = filePath.replace(".", "").replace("/", "\\").replace("\\\\", "\\");
        } else {
            build = filePath.replace("/", "\\").replace("\\\\", "\\");
        }
        return build;
    }
    public String getDirectoryFiles(File[] miFiles) {
        String fileNames = "";
        try {
            for(File f: miFiles) {
                if(f.exists() && f.isFile()) {
                    fileNames += f.getCanonicalPath() + "\n";
                } else if(f.isDirectory()) {
                    fileNames += this.getDirectoryFiles(f.listFiles());
                }
            }
        } catch(Exception e) {
            System.err.println(e);
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
                fileNames += getDirectoryFiles(miFile.listFiles());
            }
        } catch(Exception e) {
            System.err.println(e);
        }
        return fileNames;
    }
    public String listFilesFromDirectory(File[] files) {
        String fileNames = "";
        try {
            for(File f: files) {
                if(f.isFile()) {
                    fileNames += f.getPath() + "\n";
                } else {
                    this.listFilesFromPath(f.getPath());
                }
            }
        } catch(Exception e) {
            System.err.println(e);
        }
        return fileNames;
    }
    public void CreateDirectory(String directory) {
        File miFile = new File(directory);
        if(miFile.exists() == false) {
            miFile.mkdir();
        }
    }
    public String CreateTargetFromParentPath(String parentFile, String dirs) {
        String parentName = new File(parentFile).getParent();
        String targetNames = dirs.replace(parentName, "");
        return targetNames;
    }
    public void CreateParentFile(String targetFilePath, String parentFileNames) {
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
            System.err.println(e);
        }
    }
    /**
     * main class of the proyect
     * @param localpath: local path
     * @return main class file name
     */
    public static String GetMainClass(String localpath) {
        File miFile = new File(localpath + "\\src");
        BufferedReader miBufferedReader = null;
        String mainName = "";
        try {
            if(miFile.listFiles() != null) {
            outter: for(File f: miFile.listFiles()) {
                    if(f.isFile() && f.getName().contains(".java")) {
                        miBufferedReader = new BufferedReader(new FileReader(f));
                        while(miBufferedReader.read() != -1) {
                            if(miBufferedReader.readLine().contains("static void main(String[] args)")) {
                                mainName = f.getName().split(".java")[0];
                                break outter;
                            }
                        }
                    } else if (f.isFile() == false && f.isDirectory() == true){
                        mainName = "";
                    }
                }
            }
        } catch(Exception e) {
            System.err.println(e);
        } finally {
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
