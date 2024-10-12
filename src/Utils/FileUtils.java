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
        try {
            localFile = new File(localPath);
        } catch(Exception e) {
            e.printStackTrace();
        }
        return localFile;
    }
    /**
     * creates the manifesto file for the jar file creation
     * @param fileName: path where the manifesto is created
     * @param includeExtraction: true if you want the lib files as part of the jar file, false otherwise
     * @param libFiles: the lib files to include in the build
     */
    public void writeManifesto(boolean includeExtraction, String libFiles, String authorName) {
        FileWriter writer = null;
        try {
            String
                author    = authorName.trim(),
                mainClass = FileUtils.getMainClass(localPath);
            StringBuffer m = new StringBuffer();
            writer = new FileWriter(
                    getLocalFile().getPath() +
                    File.separator +
                    "Manifesto.txt"
            );
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
            writer.write(m.toString());
        } catch(IOException e) {
            e.printStackTrace();
        } finally {
            if(writer != null) {
                try {
                    writer.close();
                } catch(Exception e) {
                    e.printStackTrace();
                }
                writer = null;
            }
        }
    }
    private String getRunCommand() {
        String command = "";
        if(System.getProperty("os.name").toLowerCase().contains("windows")) {
            command = "$runCommand = " + "\"$compile\" +" + " \" && \" +" + " \"$createJar\" " +
                "+ \" && \" +" + "\"$javaCommand\"" + "\n";
        }
        return command;
    }
    private String getJavaCommand(String mainClass) {
        String command = "";
        if(System.getProperty("os.name").toLowerCase().contains("windows")) {
            command = "$javaCommand = \"java -jar " + mainClass + "\""  + "\n";
        } else if(System.getProperty("os.name").toLowerCase().contains("linux")) {
            command = "java -jar " + mainClass + "\n";
        }
        return command;
    }
    private String buildCommand() {
        String command = "";
        if(System.getProperty("os.name").toLowerCase().contains("windows")) {
            command = "$runCommand = " + "\"$compile\" +" + " \" && \" +" + " \"$createJar\" \n";
        }
        return command;
    }

    private void writeSentences(String filePath, FileWriter writeBuildScript, String srcClases, String libFiles,
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
                System.out.println("[ INFO ]: change file to executable " + local.getPath());
            }
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
        String name = getLocalFile().getPath() + File.separator + fileName;
        FileWriter writeBuildScript = new FileWriter(name);
        Command myCommand = new Command(getLocalFile().getPath());
        CommandUtils cUtils = new CommandUtils(getLocalFile().getPath());
        String
            srcClases = cUtils.getSrcClases(),
            libFiles = "",
            compile = "javac -Werror -Xlint:all -d ." + File.separator + "bin" + File.separator,
            runJar = "",
            runCommand = "";
        libFiles += cUtils.getLibFiles()
            .stream()
            .map(e -> e + ";")
            .collect(Collectors.joining());
        if(!libFiles.isEmpty()) {
            compile += " -cp '$libFiles' $srcClases";
        } else {
            compile += " $srcClases";
        }
        if(!mainClass.isEmpty()) {
            runJar = getJavaCommand(mainClass);
            runCommand = getRunCommand();
        } else {
            runCommand = buildCommand();
        }
        writeSentences(
                name,
                writeBuildScript,
                srcClases,
                libFiles,
                compile,
                myCommand.getJarFileCommand(extract, ""),
                runJar, 
                runCommand
        );
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
            String nFileName = new File(pn).toPath().normalize().toFile().getPath();
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
    public String readFileLines(String path) {
        StringBuffer lines = new StringBuffer();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(new File(path)));
            while(reader.ready()) {
                lines.append(reader.readLine());
                lines.append("\n");
            }
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            if(reader != null) {
                try {
                    reader.close();
                } catch(Exception e) {
                    e.printStackTrace();
                }
                reader = null;
            }
        }
        return lines.toString();
    }
    /**
     * main class of the proyect
     * @param localpath: local path
     * @return main class file name
     */
    public static String getMainClass(String localpath) {
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
}
