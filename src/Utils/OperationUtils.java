package Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.lang.Process;

import java.util.List;

import Operations.FileOperation;

public class OperationUtils {
    private FileOperation fileOperation;
    private String localPath;
    public OperationUtils(String nLocalPath) {
        fileOperation = new FileOperation(nLocalPath);
        localPath = nLocalPath;
    }
    public void executeCommand(String command) {
        Process p = null;
        try {
            ProcessBuilder builder = new ProcessBuilder();
            String localFULL = new File(localPath).getCanonicalPath();
            File local = new File(localFULL);
            if(command.isEmpty()) {
                throw new Exception("[ ERROR ]: cannot execute an empty command: ");
            }
            builder.command("bash", "-c", command);
            builder.directory(local);
            p = builder.start();
            if(p.getErrorStream() != null) {
                CMDOutputError(p.getErrorStream());
            }
            if(p.getInputStream() != null) {
                CMDOutput(p.getInputStream());
            }
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            if(p != null) {
                try {
                    p.waitFor();
                    p.destroy();;
                } catch(Exception e) {
                    e.printStackTrace();
                }
                p = null;
            }
        }
    }
    public void CMDOutputError(InputStream miCmdStream) {
        BufferedReader miReader = null;
        try {
            miReader = new BufferedReader(new InputStreamReader(miCmdStream));
            String line = "";
            while(true) {
                line = miReader.readLine();
                if(line == null) {
                    break;
                }
                System.out.println("[ ERROR ]: " + line);
            }
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            if(miReader != null) {
                try {
                    miReader.close();
                } catch(Exception e) {
                    e.printStackTrace();
                }
                miReader = null;
            }
        }
    }
    public void CMDOutput(InputStream miCmdStream) {
        BufferedReader miReader = null;
        try {
            miReader = new BufferedReader(new InputStreamReader(miCmdStream));
            String line = "";
            while(true) {
                line = miReader.readLine();
                if(line == null) {
                    break;
                }
                System.out.println("[ INFO ]: " + line);
            }
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            if(miReader != null) {
                try {
                    miReader.close();
                } catch(Exception e) {
                    e.printStackTrace();
                }
                miReader = null;
            }
        }
    }
    public void createProyectFiles(String author)  {
        try {
            String 
                mainDirName = new File(localPath).getCanonicalPath(),
                mainClass = new File(mainDirName).getName();
            fileOperation.createFiles(author, ".gitignore", "", false);
            fileOperation.createFiles(author, "Manifesto.txt", mainClass, false);
            fileOperation.createFiles(author, mainClass + ".java", mainClass, false);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    public void createExtractionFiles(List<String> jars) {
        File extraction = new File(localPath + File.separator + "extractionFiles");
        if(extraction.exists() == false) {
            extraction.mkdir();
        }
        jars
            .parallelStream()
            .forEach(e -> {
                fileOperation.copyFilesfromSourceToTarget(e, extraction.getPath());
            });
    }
    public boolean addJarDependency(String jarFilePath) throws Exception {
        System.out.println("[ INFO ]: adding jar dependency in process ...");
        String sourceFilePath = "";
        boolean isAdded = false;
        File jarFile = new File(jarFilePath);
        if(!jarFile.exists()) {
            throw new Exception("[ ERROR ]: jar file not found");
        }
        if(jarFile.isFile()) {
            sourceFilePath = jarFile.getParent();
        } else {
            sourceFilePath = jarFile.getPath();
        }
        String externalJarName = new File(sourceFilePath).getName();
        File libFile = new File(localPath + File.separator + "lib" + File.separator+ externalJarName);
        if(libFile.exists() == false) {
            fileOperation.copyFilesfromSourceToTarget(
                    sourceFilePath,
                    new File(localPath + File.separator + "lib").getPath()
            );
            isAdded = true;
        } else {
            System.out.println("[ INFO ]: DEPENDENCY ALREADY INSIDE THE PROYECT");
        }
        return isAdded;
    }
    // TODO: use linux sh build shell script language
    public void createBuildScript(boolean includeExtraction) {
        String mainName = FileUtils.getMainClass(localPath);
        if(!mainName.isEmpty()) {
            mainName = mainName + ".jar";
        }
        fileOperation.createFiles("", "java-exe.ps1", mainName, includeExtraction);
        System.out.println("[ INFO ]: Adding build script ...");
    }
}
