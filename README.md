# Java build tool
>- basic build tool for simple java projects
>- build the project with 1 or 3 simple commands

# Dependencies
- [java_jdk_17.0.8](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
- [powershell](https://www.microsoft.com/store/productId/9MZ1SNWT0N5D?ocid=pdpshare)
- [javaBuild_tool](https://github.com/AlfonsoG-dev/javaBuild)

# References
- [compile_references](https://docs.oracle.com/en/java/javase/17/docs/specs/man/javac.html)
- [create_jar_references](https://docs.oracle.com/javase/tutorial/deployment/jar/index.html)
- [run_java_references](https://docs.oracle.com/en/java/javase/21/docs/specs/man/java.html)

------

# Features 
- [x] compile the project to bin.
- [x] extracts the jar files inside lib to create later the project jar file.
- [x] create the project jar file.
- [x] run the project without using jar files.
- [x] build the project, combine the 3 previously mention.
- [x] create the folder structure of the project.
- [x] create the run powershell script and executes the project.

# TODO's 
- [ ] add to the *--add* CLI command the aggregation of modules like the ones of `javaFX`
- [ ] implement the module aggregation for compile, run, create-jar, execution script
 
------

# How to

- use the ps1 script to build the project.
```shell
pwsh java-exe.ps1
```
>- it will build the project and create the jar file to use for the exe file creation.
>- now you have the jar file to test the project functionality.

# Usage

>- use the executable
```console
javabuild.exe --h
```
>- use the jar file
```console
java -jar JavaBuild.jar --h
```

## Manifesto usage

- when the app have dependencies in *lib* folder you need to specify in the *Manifesto* file if you want to 
include the extraction files of the lib dependency or you need to declara in the *Manifesto* the class path 
of the lib dependencies

```text
Main-Class: App
Class-Path: .\lib\dependencyFolder\dependency.jar
```
- when you declare the Class-Path the build operation when trying to create the project *.jar* file, it exclude
the extraction files of the lib dependency.

>- if you don't declare the Class-Path the build operation when trying to create the project *.jar* file, now 
includes the extraction files of the lib dependency as part of the project *.jar* creation.

## Compile

>- to compile the project
>>- `javaBuild.exe -cm`
>>>- it compiles the .java clases into bin
>- you can use: "-t directoryPath" to indicate the directory where you want to place the compiled files
```pwsh
javabuild.exe -cm -t .\otro\target
```

## Create a jar file of the project

>- to create a jar file is necessary to include or verify dependencies or libs in the project
>>- in order to create the jar file we need to extract the content of all the jar files inside lib into: `extractionFiles`
>>- use the *extractionFiles* to include in the project jar file the dependencies
>>>- `javaBuild.exe -ex`

>- now to create the project jar file
>>- `javaBuild.exe -cj`

## Create or modify the manifesto file
>- to create the manifesto file, is necessary to include or verify if you want to add to the build the extraction of the dependencies or not.
>>- in order to create the jar file you can include the extraction files of a dependency: `javabuild.exe --i`
>>- in order to create the jar file and exclude the extraction files of a dependency: `javabuild.exe --i n` 

## Build the project

>- all can be done with only 1 command
>>- `javaBuild.exe --build`
>>>- it combines the commands: *"compile and createJar"* to build
>>>- the extraction of the jar dependency need to be execute manually with: `-ex` and only one time per jar dependency

## Create the project structure

>- creates the project structure
>>- `javaBuild.exe -b author-name`
>>>- adds the manifesto file and the main class with the project folder name

## Run or excute the project
>- `javabuild.exe --run`
>>- it compiles to *.\bin\* folder and executes the project using the .class files.
>>- you can specify the java class that you want to execute: 
```pwsh
javabuild.exe --run .\src\App.java
```
>>- if you don't give the class to execute the main class is selected
>>- additional you can execute cli command with this method too
```pwsh
javabuild.exe --run -cm
```

## create build script

>>- `javaBuild.exe -r`
>>>- creates the powershell script whit all the command to build the project, except the extraction operation
>>>- executes the project

## Add an external jar dependency

>- add an external dependency 
>>- `javabuild.exe --add dependency.jar`
>>>- add an external jar file to the lib folder of the project
>>- or you can use the directory name
>>- `javabuild.exe --add ./folderName`

------

# Additional info
This project use [javaBuild_tool](https://github.com/AlfonsoG-dev/javaBuild) to build itself.
>- this app uses powershell to execute the commands
>- if you want to use the CLI tool you have to create a `.exe` file and place it in system path

In the manifesto file you can set the following
>- Main-Class: `Main-Class: Main-Class-Name`
```txt
Main-Class: JavaBuild
```

>- Class-Path: `Class-Path: .\lib\dependency\dependency.jar`
```txt
Class-Path: .\lib\mysql\mysql.jar
```

## Conditions

- if there is no main class the project name will be use instead.
- if there is class path in the manifesto the lib dependency will be ignore when creating the project jar file.

------

# Disclaimer
>- this project is for educational purposes
>- it is not intended to create a full program
>- security issues are not taken into account
>- only works when the lib file do not contain modules
