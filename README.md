# Java build tool
>- basic build tool for simple java projects
>- build the project with 1 or 3 simple commands
>- It works on WINDOWS & LINUX

![expected output](./docs/build_command.png)

-----

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
- [ ] add to the *--add* CLI command the aggregation of modules like the ones of *JavaFX*
- [ ] implement the module aggregation for compile, run, create-jar, execution script
 
------

# How to

- use the `.ps1` script to build the project.
```shell
pwsh java-exe.ps1
```
>- it will build the project and create the jar file to use for the *.exe* file creation.
>- it will build the project and create the jar file to use for the *.exe* file creation.
>- now you have the jar file to test the project functionality.

# Usage

>- use the executable
```console
javabuild --h
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

>- to compile the project.
>>- `javaBuild -cm`.
>>>- it compiles the .java clases into bin.
>- you can give a folder path to indicate the directory where you want to place the compiled files.
>- you can give a folder path to indicate the directory where you want to place the compiled files.
```pwsh
javabuild -cm .\otro\target
```

## Create a jar file of the project

>- to create a jar file is necessary to include or verify dependencies or libraries in the project.
>- to create a jar file is necessary to include or verify dependencies or libraries in the project.
>>- in order to create the jar file we need to extract the content of all the jar files inside lib into: `extractionFiles`.
>>- use the extracted files to include the library files in the build process of the project *.jar* file.
>>>- `javaBuild -ex`.

>- now to create the project jar file.
>>- `javaBuild -cj`.
>>- it also can be created using source folder as parameter:
```pwsh
javabuild -cj .\testing\
```

## Create or modify the manifesto file
>- to create the manifesto file, is necessary to include or verify if you want to add to the build the extraction of the dependencies or not.
>>- in order to create the jar file you can include the extraction files of a dependency: `javabuild --i`
>>- in order to create the jar file and exclude the extraction files of a dependency: `javabuild --i n` 

## Build the project

>- all can be done with only 1 command
>>- `javaBuild --build`
>>>- it combines the commands: *compile* & *create jar* to build the project.
>>>- the extraction of the jar dependency need to execute manually with: `-ex` and only one time per jar dependency

## Create the project structure

>- creates the project structure
>>- `javaBuild -b author-name`
>>>- adds the manifesto file and the main class with the project folder name

## Run or execute the project
>- `javabuild --run`
>>- it compiles to *.\bin\* folder and executes the project using the .class files.
>>- you can specify the java class that you want to execute: 
```pwsh
javabuild --run .\src\App.java
```
>>- you can give also the source directory
```pwsh
javabuild --run .\src\App.java -s .\testing\
```
>>- if you don't give the class to execute the main class is selected
>>- additional you can execute CLI command with this method too
>>- additional you can execute CLI command with this method too
```pwsh
javabuild --run --h
```

## create build script

>>- `javaBuild -r`
>>>- creates the powershell script whit all the command to build the project, except the extraction operation
>>>- executes the project

## Add an external jar dependency

>- add an external dependency 
>>- `javabuild --add dependency.jar`
>>>- add an external jar file to the lib folder of the project
>>- or you can use the directory name
>>- `javabuild --add ./folderName`

------

# Additional info

This project use [javaBuild_tool](https://github.com/AlfonsoG-dev/javaBuild) to build itself.
>- this app uses powershell to execute the commands on WINDOWS.
>- this app uses bash to execute commands on LINUX.
>- if you want to use the CLI tool you have to create a `.exe` file and place it in system path

------

# Disclaimer
>- this project is for educational purposes
>- security issues are not taken into account
>- only works when the lib file don't contain modules
>- only works when the lib file don't contain modules
