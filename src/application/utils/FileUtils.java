package utils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.BufferedReader;

import java.nio.file.FileVisitOption;
import java.nio.file.Paths;
import java.nio.file.Files;

import java.util.ArrayList;
import java.util.List;

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
    public List<File> listFilesFromPath(String filePath) {
        List<File> names = new ArrayList<>();
        Thread t = new Thread(new Runnable() {
            public void run() {
                try {
                    names.addAll(Files.walk(Paths.get(filePath), FileVisitOption.FOLLOW_LINKS)
                        .map(p -> p.toFile())
                        .filter(f -> f.isFile())
                        .toList()
                    );
                } catch(IOException e) {
                    e.printStackTrace();
                }
            }
        });
        t.start();
        /**
         * TODO: start other threads or other operations.
         * - After t.start() you can execute or invoke other operations that runs in parallel with the thread operations.
         * - You need to return the thread to join it in the method that invokes this parallel method.
         */
        try {
            // TODO: join the threads or operations
            // this part should be declare in the method that invokes this parallel method. 
            t.join();
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
        return names;
    }
    public List<String> listDirectoriesFromPath(String dirPath) {
        List<String> names = new ArrayList<>();
        try {
            String c = getCleanPath(dirPath);
            File f = resolvePaths(localPath, c);
            if(f.listFiles() == null)  throw new IOException("Empty directory provided");
            names.addAll(Files.walk(Paths.get(dirPath), FileVisitOption.FOLLOW_LINKS)
                .map(p -> p.toFile())
                .filter(p -> p.isDirectory())
                .map(p -> p.getPath() + File.separator)
                .toList()
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
        return names;
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
}
