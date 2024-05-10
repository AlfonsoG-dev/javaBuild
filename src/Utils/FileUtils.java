package Utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.File;

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import Operations.Command;

public class FileUtils {
    private File localFile;
    public FileUtils(String localPath) {
        try {
            localFile = new File(localPath);
        } catch(Exception e) {
            // e.printStackTrace();
        }
    }
    /**
     * creates the manifesto file for the jar file creation
     * @param fileName: path where the manifesto is created
     * @param includeExtraction: true if you want the lib files as part of the jar file, false otherwise
     * @param libFiles: the lib files to include in the build
     */
    public void writeManifesto(String fileName, boolean includeExtraction, String libFiles, String authorName) {
        try {
            String
                author    = authorName.trim().replace(" ", "-"),
                mainClass = FileUtils.getMainClass(localFile.getPath());
            FileWriter writeManifesto = new FileWriter(
                    localFile.getPath() +
                    File.separator +
                    fileName
            );
            if(!libFiles.isEmpty() && includeExtraction) {
                writeManifesto.write(
                        "Manifest-Version: 1.0" + "\n" + 
                        "Created-By: " + author + "\n" + 
                        "Main-Class: " + mainClass + "\n"
                );
            } else if(!libFiles.isEmpty() && !includeExtraction) {
                writeManifesto.write(
                        "Manifest-Version: 1.0" + "\n" + 
                        "Created-By: " + author + "\n" + 
                        "Main-Class: " + mainClass + "\n" + 
                        "Class-Path: " + libFiles + "\n"
                );
            } else {
                writeManifesto.write(
                        "Manifest-Version: 1.0" + "\n" + 
                        "Created-By: " + author + "\n" + 
                        "Main-Class: " + mainClass + "\n"
                );
            }
            writeManifesto.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * create the sentences for the build script
     * @param fileName: path where the build script is created
     * @param mainClass: main class name
     * @param extract: true if you want to include the lib files as part of the jar file, false otherwise
     * @throws IOException: exception while trying to create the build script
     */
    public void writeBuildFile(String fileName, String mainClass, boolean extract) throws IOException {
        FileWriter writeBuildScript = new FileWriter(localFile.getPath() + "\\" + fileName);
        Command myCommand = new Command(localFile.getPath());

        String 
            compileCommand   = myCommand.getCompileCommand(
                ""
            ),
            createJarCommand = myCommand.getJarFileCommand(extract),
            os               = System.getProperty("os.name").toLowerCase();
        if(os.contains("windows") && !mainClass.isEmpty()) {
            writeBuildScript.write(
                    "$compile = " + "\"" + compileCommand + "\"" + "\n" + 
                    "$createJar = " + "\"" + createJarCommand + "\"" + "\n" + 
                    "$javaCommand = \"java -jar " + mainClass + "\""  + "\n" +
                    "$runCommand = " + "\"$compile\" +" + " \" && \" +" +
                    " \"$createJar\" +" + " \" && \" +" +
                    "\"$javaCommand\"" + "\n" + 
                    "Invoke-Expression $runCommand \n"
            );
        } else if(os.contains("windows") && mainClass.isEmpty()) {
            writeBuildScript.write(
                    "$compile = " + "\"" + compileCommand + "\"" + "\n" + 
                    "$createJar = " + "\"" + createJarCommand + "\"" + "\n" + 
                    "$runCommand = " + "\"$compile\" +" + " \" && \" +" +
                    " \"$createJar\"" + "\n" +
                    "Invoke-Expression $runCommand \n"
            );
        } else {
            System.out.println("[ INFO ]: ! OS NOT SUPPORTED ¡");
        }
        writeBuildScript.close();
    }
    public String getCleanPath(String filePath) {
        return new File(filePath).toPath().normalize().toString();
    }
    public int countFilesInDirectory(File myDirectory) {
        int count = 0;
        if(myDirectory.listFiles() != null) {
            for(File f: myDirectory.listFiles()) {
                if(f.isFile()) {
                    ++count;
                }
            }
        }
        return count;
    }
    public List<File> getDirectoryFiles(DirectoryStream<Path> misFiles) {
        List<File> names = new ArrayList<>();
        Thread t = new Thread(new Runnable() {
            public void run() {
                for(Path p: misFiles) {
                    File f = p.toFile();
                    try {
                        if(f.exists() && f.isFile()) {
                            names.add(f);
                        } else if(f.isDirectory()) {
                            names.addAll(
                                    getDirectoryFiles(
                                        Files.newDirectoryStream(f.toPath())
                                    )
                            );
                        }
                    } catch(IOException err) {
                        err.printStackTrace();
                    }
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
    public List<File> listFilesFromPath(String filePath) {
        List<File> names = new ArrayList<>();
        try {
            File miFile = new File(filePath);
            if(miFile.exists() && miFile.isFile()) {
                names.add(miFile);
            } else if(miFile.listFiles() != null) {
                names.addAll(
                        getDirectoryFiles(
                            Files.newDirectoryStream(miFile.toPath())
                        )
                );
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
        return names;
    }
    public List<File> listFilesFromDirectory(DirectoryStream<Path> files) {
        List<File> names = new ArrayList<>();
        Thread t = new Thread(new Runnable() {
            public void run() {
                for(Path p: files) {
                    File f = p.toFile();
                    if(f.isFile()) {
                        names.add(f);
                    } else if (f.isDirectory()){
                        names.addAll(
                                listFilesFromPath(f.getPath())
                        );
                    }
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
        String[] parentNames = parentFileNames.split("\n");
        for(String pn: parentNames) {
            String nFileName = pn.replace(targetFilePath.replace("/", "\\"), "");
            File mio = new File(pn);
            int fileLenght = new File(nFileName).toPath().getNameCount();
            if(mio.exists() == false && fileLenght > 1) {
                mio.mkdirs();
            } else if(mio.exists() == false && fileLenght <= 1) {
                mio.mkdir();
            }
            System.out.println("[ INFO ]: created " + mio.getPath());
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
            if(miFile.listFiles() != null) {
            outter: for(File f: miFile.listFiles()) {
                    if(f.isFile() && f.getName().contains(".java")) {
                        miBufferedReader = new BufferedReader(new FileReader(f));
                        while(miBufferedReader.read() != -1) {
                            if(miBufferedReader.readLine().contains("static void main(String[] args)")) {
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
}
