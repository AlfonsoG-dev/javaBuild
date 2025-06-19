package utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import java.util.Optional;

import operations.FileOperation;

public class CommandUtils {

    private String localPath;
    private FileUtils fileUtils;
    private FileOperation fileOperation;

    public CommandUtils(String localPath) {
        this.localPath = localPath;
        fileOperation = new FileOperation(localPath);
        fileUtils = new FileUtils(localPath);
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

    public String compileFormatType(String libFiles, String target, String flags, int release) {
        StringBuffer compile = new StringBuffer();
        compile.append("javac --release " + release + " " + flags + " -d ." + File.separator);
        if(target.isEmpty()) {
            target = "bin";
        }
        compile.append(target + File.separator + " ");
        if(!libFiles.isEmpty()) {
            compile.append(" -cp ");
        }
        return compile.toString();
    }
    private String jarTypeFormat(String jarFileName) {
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
        String jarFormat     = jarTypeFormat(jarFileName);
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
    public String runClassOption(String className, String source) {
        String name = source + File.separator + fileOperation.getProjectName(source) + ".java";
        String mainName = Optional.of(className).orElse(name);

        return mainName;
    }
}
