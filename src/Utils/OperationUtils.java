package Utils;

import java.io.File;

import Operations.FileOperation;

public class OperationUtils {
    private FileUtils fileUtils;
    private FileOperation fileOperation;
    private String localPath;
    public OperationUtils(String nLocalPath) {
        fileUtils = new FileUtils();
        fileOperation = new FileOperation(nLocalPath);
        localPath = nLocalPath;
    }
    public String srcClases() {
        String names = "";
        try {
            String[] srcdirs = fileOperation.listSRCDirectories("src").split("\n");
            for(String s: srcdirs) {
                if(s.isEmpty() == false) {
                    names += s + "* ";
                }
            }
        } catch(Exception e) {
            System.err.println(e);
        }
        String cNames = "\\src\\* " + names.substring(0, names.length()-1);
        System.out.println(cNames);
        return cNames;
    }

    public String libJars() {
        String names = "";
        String[] libfiles = {""};
        for(String l: libfiles) {
            if(l.contains(".jar")) {
                names += l + "\n";
            }
        }
        return names;
    }
    public String CreateCompileClases(String libJars, String srcClases) {
        String forCommand = "\"";
        String[] libs = libJars.split("\n");
        String b = "";
        for(String l: libs) {
            if(l.isEmpty() == false) {
                b += l + ";";
            }
        }
        String compileCommand = "";
        if(b.isEmpty() == false) {
             compileCommand = "javac -d .\\bin\\ " + srcClases;
        } else {
            String cb = b.substring(0, b.length()-1);
            forCommand += cb + "\" " + srcClases;
            compileCommand = "javac -d .\\bin\\ -cp " + forCommand;
        }
        return compileCommand;
    }
}
