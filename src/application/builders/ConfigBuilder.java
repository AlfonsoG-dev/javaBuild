package builders;

import operations.FileOperation;
import utils.FileUtils;

import java.io.File;
import java.io.FileWriter;


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
    /**
     * get from config file the project values.
     * <br> pre: Source-Path, Class-Path, Main-Class, Libraries. 
     * @return a table with the key-value of the config file
     */
    public HashMap<String, String> getConfigValues() {
        HashMap<String, String> config = new HashMap<>();

        File configFile = fUtils.resolvePaths(localPath, "config.txt"); 

        if(!configFile.exists()) {
            String mainClass = fOperation.getProjectName("src");
            String[] dConfig = {
                "Source-Path: src", "Class-Path: bin", "Main-Class: " + mainClass, "Libraries: "
            };
            for(String d: dConfig) {
                String[] nameValue = d.split(":");
                config.put(nameValue[0].trim(), nameValue[1].trim());
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
    public void writeConfigFile(String source, String target) {
        File f = fUtils.resolvePaths(localPath, "config.txt");
        try (FileWriter w = new FileWriter(f)) {
            String mainClass = fOperation.getProjectName(source);
            String lines = "Source-Path: " + source + "\nClass-Path: " + target + "\nMain-Class: " + mainClass.trim() + "\nLibraries: ";
            System.out.println(lines);
            w.write(lines);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
