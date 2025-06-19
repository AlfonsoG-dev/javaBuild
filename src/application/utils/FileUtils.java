package utils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.BufferedReader;

import java.nio.file.FileVisitOption;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.nio.file.Files;

import java.util.ArrayList;
import java.util.List;

import java.util.concurrent.Callable;

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

    /**
     * helper function to verify if the file exists
     * @param filePath the path to evaluate
     * @return true if exists, false otherwise
     */
    public boolean fileExists(String filePath) {
        return new File(filePath).exists();
    }
    /**
     * helper function to write lines to a file, if the file doesn't exists it will be created
     * @param lines the lines to write
     * @param filePath the path of the file
     */
    public void writeToFile(String lines, String filePath) {
        System.out.println("[Info] Writing lines...\n" + lines);
        try(FileWriter w = new FileWriter(getLocalFile().toPath().resolve(filePath).toFile())) {
            w.write(lines);
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * helper function to normalize any path
     * @param filePath the path to normalize
     * @return the normalized path
     */
    public String getCleanPath(String filePath) {
        return new File(filePath).toPath().normalize().toString();
    }
    /**
     * it concatenates two paths into one
     * @param root the parent path
     * @param children the path to add to the parent
     * @return the unified path
     */
    public File resolvePaths(String root, String children) {
        return new File(root).toPath().resolve(children).toFile();
    }
    /**
     * helper function to count the files inside a directory
     * @param f the directory path
     * @return the number of files inside or 0.
     */
    public int countFiles(File f) {
        File[] files = f.listFiles();
        return (files != null) ? files.length : 0;
    }
    /**
     * validates if the directory has any .java file
     * @param f the directory path
     * @return true if it has .java files, false otherwise
     */
    public boolean validateContent(File f) {
        boolean isValid = false;
        if(f.isDirectory() && f.listFiles() != null) {
            for(File v: f.listFiles()) {
                if(v.isFile() && v.getName().contains(".java")) {
                    isValid = true;
                    break;
                }
            }
        }
        return isValid;
    }
    /**
     * helper function to list the files inside a directory
     * @param filePath the directory where the files are.
     * @return a list with the files inside the directory in a recursively manner
     */
    private List<Path> listFiles(String filePath) {
        List<Path> result = new ArrayList<>();
        try {
            result = Files.walk(Paths.get(filePath), FileVisitOption.FOLLOW_LINKS)
                .filter(Files::isRegularFile)
                .toList();

        } catch(Exception e) {
            e.printStackTrace();
        }
        return result;

    }
    /**
     * helper function to list files with Callable wrapping.
     * @param filePath the directory to list its files.
     * @return a Callable with the list of files.
     */
    public Callable<List<File>> listFilesFromPath(String filePath) {
        return new Callable<List<File>>() {
            @Override
            public List<File> call() {
                return listFiles(filePath).stream().map(p -> p.toFile()).toList();
            }
        };
    }
    /**
     * helper function to list the directories inside a directory. Each directory needs to have at least one file.  
     * @param dirPath the directory to list its directories
     * @return the list of directories.
     */
    private List<File> getDirectoryNames(String dirPath) {
        List<File> names = new ArrayList<>();
        try {
            names = Files.walk(Paths.get(dirPath), FileVisitOption.FOLLOW_LINKS)
                .map(p -> p.toFile())
                .filter(p -> p.isDirectory() && countFiles(p) > 0)
                .toList();
        } catch(Exception e) {
            e.printStackTrace();
        }
        return names;
    }
    /**
     * helper function to list directories with Callable wrapping.
     * @param filePath the directory to list its directories.
     * @return a Callable with the list of directories.
     */
    public Callable<List<String>> listDirectoryNames(String filePath) {
        return new Callable<List<String>>() {
            @Override
            public List<String> call() {
                return getDirectoryNames(filePath).stream().map(p -> p.getPath()).toList();
            }
        };
    }
    /**
     * helper function to resolve a path from parent and children
     * @param parentFile the parent path
     * @param dirs the path to resolve into
     * @return the unified paths, changes its parent
     */
    public String createTargetFromParentPath(String parentFile, String dirs) {
        String parentName = new File(parentFile).getParent();
        String targetNames = dirs.replace(parentName, "");
        return targetNames;
    }
    /**
     * helper function to create directories.
     * @param targetFilePath the root directory
     * @param parentFileNames the path to resolve into
     */
    public void createParentFile(String targetFilePath, String parentFileNames) {
        String[] parentNames = parentFileNames.split("\n");
        for(String pn: parentNames) {
            String nFileName = new File(pn).toPath().normalize().toFile().getPath();
            File mio = new File(pn);
            int fileLength = new File(nFileName).toPath().getNameCount();
            if(mio.exists() == false && fileLength > 1) {
                mio.mkdirs();
            } else if(mio.exists() == false && fileLength <= 1) {
                mio.mkdir();
            }
            System.out.println("[Info] created " + mio.getPath());
        }
    }
    /**
     * helper function to get lines from a file
     * @param path the file path
     * @return the file lines
     */
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
    /**
     * helper function to list file lines wrapped in a Callable expression
     * @param filePath the file to read lines
     * @return a Callable with the list of lines
     */
    public Callable<List<String>> listFileLines(String filePath) {
        return new Callable<List<String>>() {
            @Override
            public List<String> call() {
                List<String> lines = new ArrayList<>();
                try(BufferedReader reader = new BufferedReader(new FileReader(new File(filePath)))) {
                    String line;
                    while((line = reader.readLine()) != null) {
                        lines.add(line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return lines;
            }
        };
    }
}
