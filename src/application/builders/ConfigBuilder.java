package builders;

import operations.FileOperation;
import utils.FileUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

public class ConfigBuilder {

    private String localPath;
    private FileUtils fUtils;
    private FileOperation fOperation;

    public ConfigBuilder(String localPath, FileUtils fUtils, FileOperation fOperation) {
        this.localPath = localPath;
        this.fUtils = fUtils;
        this.fOperation = fOperation;
    }
    private String buildConfigLines(String[][] configuration) {
        String lines="";
        for(int i=0; i<configuration.length; ++i) {
            for(int j=0; j<configuration[i].length; ++j) {
                lines += configuration[i][j];
            }
        }
        return lines;
    }
    /**
     * get the project configuration from a file.
     * <br> pre Source-Path, Class-Path, Main-Class, Libraries, Compile-Flags. 
     * <br> post if the config file doesn't exists you have some default values. Otherwise return the file values
     * @return a table with the key-value of the config file.
     */
    public HashMap<String, String> getConfigValues() {
        HashMap<String, String> config = new HashMap<>();

        File configFile = fUtils.resolvePaths(localPath, "config.txt"); 

        if(!configFile.exists()) {
            String mainClass = fOperation.getMainClass("src");
            String[][] headers = {
                {"Root-Path", "src"},
                {"Source-Path: ", "src"},
                {"Class-Path: ", "bin"},
                {"Main-Class: ", mainClass},
                {"Test-Path: ", "src" + File.separator + "Test"},
                {"Test-Class: ", "Test.TestLauncher"},
                {"Libraries: ", ""},
                {"Compile-Flags: ", "-Werror -Xlint:all -Xdiags:verbose"}
            };
            for(int i=0; i<headers.length; ++i) {
                for(int j=0; j<headers[i].length; ++j) {
                    config.put(headers[i][0].trim().replace(":", ""), headers[i][j]);
                }
            }
        } else {
            String[] lines = fUtils.readFileLines(configFile.getPath()).split("\n");
            for(String l : lines) {
                String[] nameValue = l.split(":", 2);
                if(nameValue.length < 2) continue;

                String name = nameValue[0].trim();
                String val = nameValue[1].trim();

                if(name.contains("Source-Path") || name.contains("Class-Path") || name.contains("Main-Class")) {
                    // for linux replace back-slash to slash
                    val = val.replace("\\", File.separator);
                }
                config.put(name, val);
            }
        }

        return config;
    }
    /**
     * If the source path doesn't contain Test folder in the first nest level return false.
     */
    private boolean existTest(String source) {
        boolean e = false;
        File f = new File(source);
        if(f.listFiles() != null && f.listFiles().length > 0) {
            for(File mf: f.listFiles()) {
                if(mf.isDirectory() && mf.getName().equals("Test")) {
                    e = true;
                    break;
                }
            }
        }
        return e;
    }
    /**
     * create the config
     * @param source where to search for .java files
     * @param target where to store the .class files
     * @throws IOException when something when the writer process went wrong
     */
    public void writeConfigFile(String root, String source, String target, String mainClassName) {
        File f = fUtils.resolvePaths(localPath, "config.txt");
        try (FileWriter w = new FileWriter(f)) {
            String mainClass =  mainClassName == null ? fOperation.getProjectName(source) : mainClassName;
            String testPath = existTest(root) ? root + File.separator + "Test" : " ";
            String testClass = existTest(root) ? fOperation.getTestClass(testPath, root) : " ";
            String[][] headers = {
                {"Root-Path: ", root},
                {"\nSource-Path: ", source},
                {"\nClass-Path: ", target},
                {"\nMain-Class: ", mainClass.trim()},
                {"\nTest-Path: ", testPath},
                {"\nTest-Class: ", testClass},
                {"\nLibraries: ", ""},
                {"\nCompile-Flags: ",  "-Werror -Xlint:all -Xdiags:verbose"}
            };
            String lines = buildConfigLines(headers);
            System.out.println(lines);
            w.write(lines);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
