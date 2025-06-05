package Utils;

import Utils.FileUtils;
import Operations.FileOperation;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CommandUtils {

    private String localPath;
    private FileUtils fileUtils;
    private FileOperation fileOperation;
    public CommandUtils(String localPath) {
        this.localPath = localPath;
        fileOperation = new FileOperation(localPath);
        fileUtils = new FileUtils(localPath);
    }

    public String getMainClass() {
        return fileOperation.getMainClass(localPath);
    }
    public String getProjectName() {
        String name = getMainClass();
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
    public boolean recompile(Path filePath, Path source, Path target) {
        boolean shouldRecompile = true;
        try {
            Path relative = source.relativize(filePath);
            Path classFilePath = target.resolve(relative.toString().replace(".java", ".class"));

            File javaFile = filePath.toFile();
            File classFile = classFilePath.toFile();
            shouldRecompile = !classFile.exists() || javaFile.lastModified() > classFile.lastModified();
        } catch(Exception e) {
            e.printStackTrace();
        }
        return shouldRecompile;
    }
    public String getSrcClases(String source, String target) {
        String b = "";
        List<String> names = new ArrayList<>();
        try {
            File srcFile = new File(localPath + File.separator + source);
            File binFile = new File(localPath + File.separator + target);
            List<File> targetFiles = fileUtils.listFilesFromPath(binFile.getPath());
            if(srcFile.listFiles() == null) {
                System.out.println("[Info] " + srcFile.getPath() + " is empty");
            }
            if(binFile.exists() && targetFiles.size() > 0) {
                fileUtils.listFilesFromPath(srcFile.toString())
                    .stream()
                    .map(f -> f.toPath())
                    .filter(p -> recompile(p, srcFile.toPath(), binFile.toPath()))
                    .forEach(e -> {
                        names.add(e + " ");
                    });
            } else if(binFile.exists() && targetFiles.size() == 0 || !binFile.exists()) {
                for(File f: srcFile.listFiles()) {
                    if(f.isFile() && f.getName().contains(".java")) {
                        names.add(".");
                        names.add(File.separator);
                        names.add("src");
                        names.add(File.separator);
                        names.add("*.java ");
                        break;
                    }
                }
                fileOperation.listSRCDirectories(source)
                    .parallelStream()
                    .filter(e -> !e.isEmpty())
                    .forEach(e -> {
                        int countFiles = fileUtils.countFilesInDirectory(new File(e));
                        if(countFiles > 0) {
                            names.add(e + "*.java ");
                        }
                });
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        if(names.isEmpty()) {
            System.out.println("[ERROR] empty src list of files");
        }
        b += names
            .parallelStream()
            .collect(Collectors.joining());
        return b;
    }

    public List<String> getLibFiles() {
        List<String> 
            names = new ArrayList<>(),
            libfiles = fileOperation.listLibFiles();

        libfiles
            .stream()
            .map(e -> new File(e))
            .filter(e -> e.exists() && e.isFile() && e.getName().contains(".jar"))
            .forEach(e -> {
                names.add(e.getPath());
            });
        return names;
    }
    private boolean haveManifesto() {
        boolean exists = false;
        try {
            File miFile = new File(localPath + File.separator + "Manifesto.txt");
            if(miFile.exists()) {
                exists = true;
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        return exists;
    }
    public String compileFormatType(String target, int release) {
        StringBuffer compile = new StringBuffer();
        compile.append("javac --release " + release + " -Werror -Xlint:all -d ." + File.separator);
        if(target.isEmpty()) {
            target = "bin";
        }
        compile.append(target + File.separator + " ");
        if(!getLibFiles().isEmpty()) {
            compile.append(" -cp ");
        }
        return compile.toString();
    }
    private String jarTypeFormat(String mainName, String directory) throws IOException {
        StringBuffer jarFormat = new StringBuffer();
        jarFormat.append("jar");
        if(haveManifesto()) {
            jarFormat.append(" -cfm ");
        }
        if(!haveManifesto() && mainName.isEmpty()) {
            jarFormat.append(" -cf ");
        }
        if(!haveManifesto() && !mainName.isEmpty()) {
            jarFormat.append(" -cfe ");
        }
        return jarFormat.toString();
    }
    public String jarTypeUnion(String directory, String source) throws IOException {
        StringBuffer build = new StringBuffer();
        String 
            mainName = getProjectName() + ".jar",
            localParent = new File(localPath).getCanonicalPath(),
            jarFormat = jarTypeFormat(mainName, directory),
            mainClassName = getProjectName();
        if(source.isEmpty()) {
            source = "." + File.separator + "bin" + File.separator + " .";
        } else {
            source = new File(source).getPath() + File.separator + " .";
        }

        build.append(jarFormat);

        switch(jarFormat) {
            case "jar -cfm ":
                if(!directory.isEmpty()) {
                    build.append(mainName);
                    build.append(" Manifesto.txt -C ");
                    build.append(source);
                    build.append(directory);
                } else if(directory.isEmpty()) {
                    build.append(mainName);
                    build.append(" Manifesto.txt -C ");
                    build.append(source);
                }
                break;
            case "jar -cf ":
                String jarName = new File(localParent).getName() + ".jar";
                if(!directory.isEmpty()) {
                    build.append(jarName);
                    build.append(" -C ");
                    build.append(source);
                    build.append(directory);
                } else {
                    build.append(jarName);
                    build.append(" -C ");
                    build.append(source);
                }
                break;
            case "jar -cfe ":
                if(!directory.isEmpty()) {
                    build.append(mainName);
                    build.append(" ");
                    build.append(mainClassName);
                    build.append(" -C ");
                    build.append(source);
                    build.append(directory);
                } else {
                    build.append(mainName);
                    build.append(" ");
                    build.append(mainClassName);
                    build.append(" -C ");
                    build.append(source);
                }
                break;
        }
        return build.toString();
    }
    protected String manifestoClass() {
        String
            name = "",
            lines = fileUtils.readFileLines(localPath + File.separator + "Manifesto.txt");
        for(String l: lines.split("\n")) {
            if(l.contains("Main-Class: ")) {
                name = l.split(":")[1];
            }
        }
        return name;
    }
    public StringBuffer runClassOption(String className) {
        StringBuffer runClass = new StringBuffer();
        String
            name = " ." + File.separator + "src" + File.separator + getProjectName() + ".java",
            mainName = !manifestoClass().isEmpty() ?
            manifestoClass() : name;
        
        if(className.isEmpty()) {
            runClass.append(mainName);
        } else if(className.equals(mainName)) {
            runClass.append(mainName);
        } else {
            runClass.append(" " + className);
        }
        return runClass;
    }
}
