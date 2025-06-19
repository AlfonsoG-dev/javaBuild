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
    /**
     * extract parent from a nested path
     * @param source the path to extract the parent
     * @return the parent of the path
     */
    public Path parentFromNesting(Path source) {
        Path p = source;
        int n = source.getNameCount();
        for(int i=n-2; i > 0; --i) {
            p = p.getParent();
        }
        return p;
    }

    /**
     * validate if the file should be re-compile or not
     * @param filePath the file to evaluate
     * @param source the source path where the .java files are
     * @param target the path where the .class files are stored
     * @return true if the file should be re-compile, false otherwise
     */
    public boolean recompileFiles(Path filePath, Path source, Path target) {
        String relative = "";
        String classFilePath = "";
        source = source.normalize();
        if(source.getNameCount() > 2) {
            // only when the source folder structure is: `src\name\name\name\App.java`
            String sourceParent = parentFromNesting(source).toString();
            System.out.println(source + " Here strip path till parent " + sourceParent);
            relative = filePath.toString().replace(sourceParent, "");
            classFilePath = target.toString() + relative.replace(".java", ".class");
        } else {
            System.out.println("No");
            relative = source.relativize(filePath).toString();
            classFilePath = target.resolve(relative.replace(".java", ".class")).toString();
        } 

        File javaFile = filePath.toFile();
        File classFile = new File(classFilePath);
        return !classFile.exists() || javaFile.lastModified() > classFile.lastModified();
    }

    /**
     * evaluate the type of format to set the compile, -d if you don't have lib files, -cp if you have lib files
     * @param libFiles the project .jar dependencies
     * @param target where the .class files are stored
     * @param flags the compile flags
     * @param release the java jdk version
     * @return the format -d or -cp in the javac command
     */
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
    /**
     * evaluate the type of format to set the jar file creation, -c with -fm, -f, -fe,
     * @param jarFileName the project jar file name
     * @return the jar file command format
     */
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
    /**
     * the run option if jar file or .java
     * @param className
     * @param source
     * @return
     */
    public String runClassOption(String className, String source) {
        String name = source + File.separator + fileOperation.getProjectName(source) + ".java";
        String mainName = Optional.of(className).orElse(name);

        return mainName;
    }
}
