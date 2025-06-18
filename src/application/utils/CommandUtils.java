package utils;

import operations.FileOperation;
import operations.ExecutorOperation;

import java.io.File;
import java.io.IOException;

import java.nio.file.Path;

import java.util.Optional;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class CommandUtils {

    private String localPath;
    private FileUtils fileUtils;
    private FileOperation fileOperation;
    private ExecutorOperation executor;

    public CommandUtils(String localPath) {
        this.localPath = localPath;
        fileOperation = new FileOperation(localPath);
        fileUtils = new FileUtils(localPath);
        executor = new ExecutorOperation();
    }
    public Path parentFromNesting(Path source) {
        Path p = null;
        int n = source.getNameCount();
        for(int i=n; i > 0; --i) {
            p = source.getParent();
        }
        return p;
    }

    public boolean recompileFiles(Path filePath, Path source, Path target) {
        String relative = "";
        String classFilePath = "";
        source = source.normalize();
        if(source.getNameCount() > 2) {
            if(filePath.toString().contains(fileOperation.getMainClass(source.toString()) + ".java")) {
                relative = source.relativize(filePath).toString();
                classFilePath = target.resolve(relative.replace(".java", ".class")).toString();
            } else {
                String sourceParent = parentFromNesting(source).toString();
                relative = filePath.toString().replace(sourceParent, "");
                classFilePath = target.toString() + relative.replace(".java", ".class");
            }
        } else {
            relative = source.relativize(filePath).toString();
            classFilePath = target.resolve(relative.replace(".java", ".class")).toString();
        } 

        File javaFile = filePath.toFile();
        File classFile = new File(classFilePath);
        return !classFile.exists() || javaFile.lastModified() > classFile.lastModified();
    }
    public Optional<String> getSourceFiles(String source, String target) {
        String b = null;
        List<String> names = new ArrayList<>();
        File srcFile = fileUtils.resolvePaths(localPath, source);
        File binFile = fileUtils.resolvePaths(localPath, target);
        if(srcFile.listFiles() == null) {
            System.out.println("[Info] " + srcFile.getPath() + " is empty");
            return Optional.ofNullable(b);
        }
        if(binFile.exists() && binFile.listFiles() == null || !binFile.exists()) {
            names.addAll(fileOperation.listSourceDirs(source));
       } else if(binFile.exists() && binFile.listFiles() != null || binFile.listFiles().length == 0) {
            executor.executeConcurrentCallableList(fileUtils.listFilesFromPath(srcFile.toString()))
                .stream()
                .map(f -> f.toPath())
                .filter(p -> recompileFiles(p, srcFile.toPath(), binFile.toPath()))
                .forEach(e -> {
                    names.add(e.normalize() + " ");
                });
        } 
        if(names.size() > 0) {
            b = names
            .stream()
            .collect(Collectors.joining());
        }
        return Optional.ofNullable(b);
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
    public String compileFormatType(String target, int release) {
        StringBuffer compile = new StringBuffer();
        // TODO: get flags from config file
        compile.append("javac --release " + release + " -Werror -Xlint:all -Xdiags:verbose -d ." + File.separator);
        if(target.isEmpty()) {
            target = "bin";
        }
        compile.append(target + File.separator + " ");
        if(!getLibFiles().isEmpty()) {
            compile.append(" -cp ");
        }
        return compile.toString();
    }
    private String jarTypeFormat(String jarFileName, String directory) {
        StringBuffer jarFormat = new StringBuffer();
        jarFormat.append("jar -c");
        boolean presentManifesto = fileOperation.haveManifesto();
        if(presentManifesto) {
            jarFormat.append("fm ");
        }
        if(!presentManifesto && jarFileName.isEmpty()) {
            jarFormat.append("f ");
        }
        if(!presentManifesto && !jarFileName.isEmpty()) {
            jarFormat.append("fe ");
        }
        return jarFormat.toString();
    }
    /**
     * @param source: directory where .class files are
     * @param target: directory where .java files are, this will serve to find the mainClass
     */
    public String jarTypeUnion(String directory, String source, String target) throws IOException {
        StringBuffer build = new StringBuffer();

        String jarFileName      = fileOperation.getProjectName(target) + ".jar";
        String localParent   = new File(localPath).getCanonicalPath();
        String jarFormat     = jarTypeFormat(jarFileName, directory);
        String mainClassName = fileOperation.getProjectName(target);

        source = fileUtils.resolvePaths(localPath, source).getPath() + File.separator + " .";

        build.append(jarFormat);

        switch(jarFormat) {
            case "jar -cfm ":
                if(!directory.isEmpty()) {
                    build.append(jarFileName);
                    build.append(" Manifesto.txt -C ");
                    build.append(source);
                    build.append(directory);
                } else {
                    build.append(jarFileName);
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
                    build.append(jarFileName);
                    build.append(" ");
                    build.append(mainClassName);
                    build.append(" -C ");
                    build.append(source);
                    build.append(directory);
                } else {
                    build.append(jarFileName);
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
        String name = "";
        String lines = fileUtils.readFileLines(
                fileUtils.resolvePaths(localPath, "Manifesto.txt").getPath()
        );
        for(String l: lines.split("\n")) {
            if(l.contains("Main-Class: ")) {
                name = l.split(":")[1];
            }
        }
        return name;
    }
    public String runClassOption(String className, String source) {
        String name = source + File.separator + fileOperation.getProjectName(source) + ".java";
        String mainName = Optional.of(className).orElse(name);

        return mainName;
    }
}
