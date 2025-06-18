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

    public boolean fileExists(String filePath) {
        return new File(filePath).exists();
    }
    public void writeToFile(String lines, String filePath) {
        System.out.println("[Info] Writing lines...\n" + lines);
        try(FileWriter w = new FileWriter(getLocalFile().toPath().resolve(filePath).toFile())) {
            w.write(lines);
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
    public String getCleanPath(String filePath) {
        return new File(filePath).toPath().normalize().toString();
    }
    public File resolvePaths(String root, String children) {
        return new File(root).toPath().resolve(children).toFile();
    }
    public int countFiles(File f) {
        File[] files = f.listFiles();
        return (files != null) ? files.length : 0;
    }

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
    public Callable<List<File>> listFilesFromPath(String filePath) {
        return new Callable<List<File>>() {
            @Override
            public List<File> call() {
                return listFiles(filePath).stream().map(p -> p.toFile()).toList();
            }
        };
    }

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
    public Callable<List<String>> listDirectoryNames(String filePath) {
        return new Callable<List<String>>() {
            @Override
            public List<String> call() {
                return getDirectoryNames(filePath).stream().map(p -> p.getPath()).toList();
            }
        };
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
            int fileLength = new File(nFileName).toPath().getNameCount();
            if(mio.exists() == false && fileLength > 1) {
                mio.mkdirs();
            } else if(mio.exists() == false && fileLength <= 1) {
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
