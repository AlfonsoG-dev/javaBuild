package main.application.operations;

import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.Path;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import main.application.utils.CommandUtils;
import main.application.utils.FileUtils;
import main.application.builders.ScriptBuilder;
public class FileOperation {

    private FileUtils fileUtils;
    private String localPath;
    private ScriptBuilder scriptBuilder;
    public FileOperation(String nLocalPath) {
        localPath = nLocalPath;
        fileUtils = new FileUtils(localPath);
        scriptBuilder = new ScriptBuilder(nLocalPath);
    }

    public List<String> listLibFiles() {
        List<String> names = new ArrayList<>();
        File lf = fileUtils.resolvePaths(localPath, "lib");
        if(lf.listFiles() == null) System.out.println("[Info] No dependencies found");;
        if(lf.listFiles() != null) {
            try {
                Files.newDirectoryStream(lf.toPath())
                    .forEach(e -> {
                        File f = e.toFile();
                        for(File mf: f.listFiles()) {
                            names.add(mf.getPath());
                        }
                });
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
        return names;
    }
    public List<String> listSourceDirs(String source) {
        List<String> filteredNames = new ArrayList<>();
        List<String> dirNames = fileUtils.listDirectoriesFromPath(source);
        dirNames.
            stream()
            .filter(n -> fileUtils.countFilesInDirectory(new File(n)) > 0)
            .forEach(System.out::println);

        return filteredNames;
    }

    /**
     * main class of the project
     * @return main class file name
     */
    public String getMainClass(String source) {
        File miFile = fileUtils.resolvePaths(localPath, source);
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

    public String getProjectName(String source) {
        String name = getMainClass(source);
        if(name.isEmpty()) {
            try {
            String
                localParent = new File(localPath).getCanonicalPath(),
                localName = new File(localParent).getName();
            name = localName;
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
        return name;
    }

    public boolean haveManifesto() {
        return fileUtils.fileExists(fileUtils.resolvePaths(localPath, "Manifesto.txt").getPath());
    }
    public void createFiles(String author, String fileName, String mainClass, String source, String target, boolean extract) {
        System.out.println("[ Info ]: created " + fileName);

        if(mainClass.isEmpty()) mainClass = getMainClass(source);

        if(fileName.equals(".gitignore")) {
            String ignoreFiles = "";
            ignoreFiles = "**bin" + "\n" +
                "**lib" + "\n" +
                "**extractionFiles" + "\n" +
                "**Manifesto.txt" + "\n" +
                "**Session.vim" + "\n" +
                "**.jar" + "\n" +
                "**.exe";
            fileUtils.writeToFile(ignoreFiles, fileName);
        } else if(fileName.equals("Manifesto.txt")) {
            String libJars = "";
            List<String> jars = new CommandUtils(localPath).getLibFiles();
            libJars += jars
                .parallelStream()
                .filter(e -> !e.isEmpty())
                .map(e -> e + ";")
                .collect(Collectors.joining());
            scriptBuilder.writeManifesto(libJars, author, mainClass, extract);

        } else if(fileName.equals(mainClass + ".java")) {
            String mainClassLines = "";
            mainClassLines = "class " + mainClass + " {\n" +
                "    public static void main(String[] args) {\n" + 
                "        System.out.println(\"Hello from " + mainClass + "\");" + "\n" +
                "    }\n" + 
                "}";
            String targetSource = fileUtils.resolvePaths(localPath, source).getPath();
            fileUtils.writeToFile(mainClassLines, fileUtils.resolvePaths(targetSource, fileName).getPath());
        } else if(fileName.contains(".ps1") || fileName.contains(".sh")) {
            // write build script lines
            scriptBuilder.writeBuildFile(
                fileName,
                mainClass,
                source,
                target,
                fileUtils.listDirectoriesFromPath(source),
                listLibFiles().stream().filter(p -> p.contains(".jar")).toList(),
                extract
            );
        }
    }
    public boolean extractionDirContainsPath(String libJarPath) throws IOException {
        boolean containsPath = false;
        File extractionFile = fileUtils.resolvePaths(localPath, "extractionFiles");
        File myFile = new File(libJarPath);
        if(myFile.getParent() != null && extractionFile.listFiles() != null) {
            String jarLibParent = myFile.getParent();
            String jarNameParent = new File(jarLibParent).getName();
            for(File f: extractionFile.listFiles()) {
                String extractionDirName = new File(f.getCanonicalPath()).getName();
                if(extractionDirName.equals(jarNameParent)) {
                    containsPath = true;
                }
            }
        }
        return containsPath;
    }
    public void copyFilesfromSourceToTarget(String sourceFilePath, String targetFilePath) {
        try {
            File sf = new File(sourceFilePath);
            if(sf.isFile()) {
                String sourceFileName = sf.getName();
                String sourceParent = sf.getParent();
                String sourceParentName = new File(sourceParent).getName();
                File tf = new File(
                        targetFilePath + File.separator +
                        sourceParentName + File.separator +
                        sourceFileName
                );
                fileUtils.createParentFile(
                        tf.getPath(),
                        tf.getParent()
                );
                System.out.println(
                        Files.copy(
                            sf.toPath(),
                            tf.toPath(),
                            StandardCopyOption.COPY_ATTRIBUTES
                        )
                );
            } else if(sf.isDirectory()) {
                fileUtils.listFilesFromPath(sourceFilePath)
                    .parallelStream()
                    .forEach(e -> {
                        try {
                            String n = fileUtils.createTargetFromParentPath(
                                    sourceFilePath,
                                    e.getCanonicalPath()
                            );
                            if(n.contains("git") == false) {
                                File targetFile = new File(targetFilePath + File.separator + n);
                                fileUtils.createParentFile(targetFilePath, targetFile.getParent());
                                Path sourcePath = e.toPath();
                                Path targetPath = targetFile.toPath();
                                System.out.println(
                                        Files.copy(
                                            sourcePath,
                                            targetPath,
                                            StandardCopyOption.COPY_ATTRIBUTES
                                        )
                                );
                            }
                        } catch(IOException err) {
                            err.printStackTrace();
                        }
                    });
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
}
