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
import java.util.stream.Collectors;

import Operations.Command;

public class FileUtils {
    private File localFile;
    private String localPath;

    public FileUtils(String localPath) {
        this.localPath = localPath;
    }
    public File getLocalFile() {
        localFile = new File(localPath);
        return localFile;
    }
    public void writeToFile(String lines, String filePath) {
        try(FileWriter w = new FileWriter(getLocalFile().toPath().resolve(filePath).toFile())) {
            w.write(lines);
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
    public static String getRunScriptCommand() {
        String command = "";
        if(System.getProperty("os.name").toLowerCase().contains("windows")) {
            command = "$runCommand = " + "\"$compile\" +" + " \" && \" +" + " \"$createJar\" " +
                "+ \" && \" +" + "\"$javaCommand\"" + "\n";
        }
        return command;
    }
    public static String getJavaScriptCommand(String mainClass) {
        String command = "";
        if(System.getProperty("os.name").toLowerCase().contains("windows")) {
            command = "$javaCommand = \"java -jar " + mainClass + "\""  + "\n";
        } else if(System.getProperty("os.name").toLowerCase().contains("linux")) {
            command = "java -jar " + mainClass + "\n";
        }
        return command;
    }
    public static String getBuildScriptCommand() {
        String command = "";
        if(System.getProperty("os.name").toLowerCase().contains("windows")) {
            command = "$runCommand = " + "\"$compile\" +" + " \" && \" +" + " \"$createJar\" \n";
        }
        return command;
    }

    public static void writeScript(String filePath, FileWriter writeBuildScript, String srcClases, String libFiles,
            String compile, String extractJar, String runJar, String runCommand) throws IOException {
        if(System.getProperty("os.name").toLowerCase().contains("windows")) {
            writeBuildScript.write(
                    "$srcClases = \"" + srcClases + "\"\n" +
                    "$libFiles = \"" + libFiles + "\"\n" +
                    "$compile = \"" + compile + "\"\n" + 
                    "$createJar = " + "\"" + extractJar + "\"" + "\n" + 
                    runJar + 
                    runCommand +
                    "Invoke-Expression $runCommand \n"
            );
        } else if(System.getProperty("os.name").toLowerCase().contains("linux")) {
            writeBuildScript.write(
                    "srcClases=" + "\"" + srcClases + "\"\n" + 
                    "libFiles=" + "\"" + libFiles + "\"\n" + 
                    compile + "\n" + 
                    extractJar + "\n" + 
                    runJar
            );
            File local = new File(filePath);
            if(local.setExecutable(true)) {
                System.out.println("[Info] change file to executable " + local.getPath());
            }
        }
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
            String nFileName = new File(pn).toPath().normalize().toFile().getPath();
            File mio = new File(pn);
            int fileLenght = new File(nFileName).toPath().getNameCount();
            if(mio.exists() == false && fileLenght > 1) {
                mio.mkdirs();
            } else if(mio.exists() == false && fileLenght <= 1) {
                mio.mkdir();
            }
            System.out.println("[Info] created " + mio.getPath());
        }
    }
    public String readFileLines(String path) {
        StringBuffer lines = new StringBuffer();
        try (BufferedReader reader = new BufferedReader(new FileReader(new File(path)))) {
            while(reader.ready()) {
                lines.append(reader.readLine());
                lines.append("\n");
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        return lines.toString();
    }
}
