package Utils;

import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

import java.io.File;
import java.io.IOException;

import Operations.FileOperation;

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
        return FileUtils.getMainClass(localPath);
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
    public String getSrcClases() {
        String b = "";
        List<String> names = new ArrayList<>();
        try {
            File srcFile = new File(localPath + "\\src");
            if(srcFile.listFiles() != null) {
                for(File f: srcFile.listFiles()) {
                    if(f.isFile() && f.getName().contains(".java")) {
                        names.add(".\\src\\*.java ");
                        break;
                    }
                }
                fileOperation.listSRCDirectories("src")
                    .parallelStream()
                    .filter(e -> !e.isEmpty())
                    .forEach(e -> {
                        int countFiles = fileUtils.countFilesInDirectory(new File(e));
                        if(countFiles > 0) {
                            names.add(e + "*.java ");
                        }
                    });
            } else {
                System.out.println("[ INFO ]: " + localPath + "\\src\\ folder not found");
            }
        } catch(Exception e) {
            e.printStackTrace();
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
            File miFile = new File(localPath + "\\Manifesto.txt");
            if(miFile.exists()) {
                exists = true;
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        return exists;
    }
    public String compileFormatType(String target) {
        StringBuffer compile = new StringBuffer();
        compile.append("javac -Werror -Xlint:all -d");
        if(target.isEmpty() && getLibFiles().isEmpty()) {
            compile.append(" .\\bin\\ ");
        } else if(target.isEmpty() && !getLibFiles().isEmpty()) {
            compile.append(" .\\bin\\ -cp ");
        } else if(!target.isEmpty() && getLibFiles().isEmpty()) {
            compile.append(target);
            compile.append(" ");
        } else if(!target.isEmpty() && !getLibFiles().isEmpty()) {
            compile.append(target);
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
            source = ".\\bin\\ .";
        } else {
            source = new File(source).getPath() + File.separator + " .";
        }

        switch(jarFormat) {
            case "jar -cfm ":
                if(!directory.isEmpty()) {
                    build.append(jarFormat);
                    build.append(mainName);
                    build.append(" Manifesto.txt -C ");
                    build.append(source);
                    build.append(directory);
                } else if(directory.isEmpty()) {
                    build.append(jarFormat);
                    build.append(mainName);
                    build.append(" Manifesto.txt -C ");
                    build.append(source);
                }
                break;
            case "jar -cf ":
                String jarName = new File(localParent).getName() + ".jar";
                if(!directory.isEmpty()) {
                    build.append(jarFormat);
                    build.append(jarName);
                    build.append(" -C ");
                    build.append(source);
                    build.append(directory);
                } else {
                    build.append(jarFormat);
                    build.append(jarName);
                    build.append(" -C ");
                    build.append(source);
                }
                break;
            case "jar -cfe ":
                if(!directory.isEmpty()) {
                    build.append(jarFormat);
                    build.append(mainName);
                    build.append(" ");
                    build.append(mainClassName);
                    build.append(" -C ");
                    build.append(source);
                    build.append(directory);
                } else {
                    build.append(jarFormat);
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
        String mainName = !manifestoClass().isEmpty() ?
            manifestoClass() : " .\\src\\" + getProjectName() + ".java";
        
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
