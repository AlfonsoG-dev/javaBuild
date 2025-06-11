package main.application.builders;

import main.application.utils.FileUtils;
import main.application.operations.FileOperation;

import java.io.File;

import java.util.Optional;
import java.util.HashMap;

public class ConfigBuilder {

    private String localPath;
    private FileUtils fUtils;
    private FileOperation fOperation;

    public ConfigBuilder(String localPath, FileUtils fUtils, FileOperation fOperation) {
        this.localPath = localPath;
        this.fUtils = fUtils;
        fOperation = fOperation;
    }
    public HashMap<String, String> getConfigValues() {
        HashMap<String, String> config = new HashMap<>();

        File configFile = fUtils.resolvePaths(localPath, "config.txt"); 

        if(!configFile.exists()) {
            String mainClass = fOperation.getProjectName("src");
            String[] defaul = {
                "Source-Path: src", "Class-Path: bin", "Main-Class: " + mainClass, "Libraries: "
            };
            for(String d: defaul) {
                String[] nameValue = d.split(":");
                config.put(nameValue[0].trim(), nameValue[1].trim());
            }
        } else {
            String[] lines = fUtils.readFileLines(configFile.getPath()).split("\n");
            for(String l: lines) {
                String[] nameValue = l.split(":");
                config.put(nameValue[0].trim(), nameValue[1].trim());
            }
        }

        return config;
    }
}
