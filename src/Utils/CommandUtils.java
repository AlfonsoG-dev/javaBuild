package Utils;

import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

import java.io.File;
import java.io.IOException;

import Operations.FileOperation;

public class CommandUtils {

    private String localPath;
    private FileOperation fileOperation;
    private FileUtils fileUtils;
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
            if(srcFile.exists() && srcFile.listFiles() != null) {
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
            .sorted()
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
        if(target.isEmpty() && getLibFiles().isEmpty()) {
            compile.append("javac -Werror -Xlint:all -d .\\bin\\ ");
        } else if(target.isEmpty() && !getLibFiles().isEmpty()) {
            compile.append("javac -Werror -Xlint:all -d .\\bin\\ -cp ");
        } else if(!target.isEmpty() && getLibFiles().isEmpty()) {
            compile.append("javac -Werror -Xlint:all -d ");
            compile.append(new File(target).getPath());
            compile.append(" ");
        }
        return compile.toString();
    }
    private String jarTypeFormat(String mainName, String directory) throws IOException {
        String jarFormat = "";
        if(haveManifesto()) {
            jarFormat = "jar -cfm ";
        }
        if(!haveManifesto() && mainName.isEmpty()) {
            jarFormat = "jar -cf ";
        }
        if(!haveManifesto() && !mainName.isEmpty()) {
            jarFormat = "jar -cfe ";
        }
        return jarFormat;
    }
    public String jarTypeUnion(String directory) throws IOException {
        StringBuffer build = new StringBuffer();
        String 
            mainName = getProjectName() + ".jar",
            localParent = new File(localPath).getCanonicalPath(),
            jarFormat = jarTypeFormat(mainName, directory),
            mainClassName = getProjectName();

        switch(jarFormat) {
            case "jar -cfm ":
                if(mainName != "" && directory != "") {
                    build.append(jarFormat);
                    build.append(mainName);
                    build.append(" Manifesto.txt -C .\\bin\\ .");
                    build.append(directory);
                } else if(mainName != "" && directory == "") {
                    build.append(jarFormat);
                    build.append(mainName);
                    build.append(" Manifesto.txt -C .\\bin\\ .");
                }
                break;
            case "jar -cf ":
                String jarName = new File(localParent).getName() + ".jar";
                if(directory != "") {
                    build.append(jarFormat);
                    build.append(jarName);
                    build.append(" -C .\\bin\\ .");
                    build.append(directory);
                } else {
                    build.append(jarFormat);
                    build.append(jarName);
                    build.append(" -C .\\bin\\ .");
                }
                break;
            case "jar -cfe ":
                if(directory != "") {
                    build.append(jarFormat);
                    build.append(mainName);
                    build.append(" ");
                    build.append(mainClassName);
                    build.append(" -C .\\bin\\ .");
                    build.append(directory);
                } else {
                    build.append(jarFormat);
                    build.append(mainName);
                    build.append(" ");
                    build.append(mainClassName);
                    build.append(" -C .\\bin\\ .");
                }
                break;
        }
        return build.toString();
    }
    public StringBuffer runClassOption(String className) {
        StringBuffer runClass = new StringBuffer();
        String mainName = getProjectName() + ".java";
        if(className.isEmpty() || className == null) {
            runClass.append(" .\\src\\" + mainName);
        } else if(className.equals(mainName)) {
            runClass.append(" .\\src\\" + mainName);
        } else {
            runClass.append(" " + className);
        }
        return runClass;
    }
}