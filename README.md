# Java build tool
- basic build tool for simple java projects
- build the project with 1 or 3 simple commands
- It works on WINDOWS & LINUX

![expected output](./docs/build_command.png)

-----

# Dependencies
- [java_jdk_17.0.8](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
- [powershell](https://www.microsoft.com/store/productId/9MZ1SNWT0N5D?ocid=pdpshare)

# References
- [compile_references](https://docs.oracle.com/en/java/javase/17/docs/specs/man/javac.html)
- [create_jar_references](https://docs.oracle.com/javase/tutorial/deployment/jar/index.html)
- [run_java_references](https://docs.oracle.com/en/java/javase/21/docs/specs/man/java.html)
- [executables_from_jar_linux](https://stackoverflow.com/questions/44427355/how-to-convert-jar-to-linux-executable-file)

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
>- now you have the jar file to test the project functionality.

# Usage

- use the executable
>- This will print the **cli** commands that you can used.
```console
javabuild --h
```
>- use the jar file to execute the program for the same purpose.
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

## list project files

- Use this to list the java *class* files inside the given folder.
```shell
javaBuild -ls .\src\
```

## Crete a project

- Use this to create the structure for the project.
>- You need to provide the author
```shell
javaBuild -cb -ls Author-Name
```

## Compile

- to compile the project.
>- `javaBuild -cm`.
>>- it compiles the .java clases into bin.
>>- you can give a folder path to indicate the directory where you want to place the compiled files.
>>- The default folder for the *class* files is **.\bin\**
```pwsh
javabuild -cm .\otro\target
```

## Create a jar file of the project

- to create a jar file is necessary to include or verify dependencies or libraries in the project.
>- in order to create the jar file we need to extract the content of all the jar files inside lib into: `extractionFiles`.
>- use the extracted files to include the library files in the build process of the project *.jar* file.
>>- `javaBuild -ex`.

- now to create the project jar file.
>- `javaBuild -cx`.
>- it also can be created using source folder as parameter:
```pwsh
javabuild -cx .\testing\
```
## Create the build script

- Use this to create the build script for **powershell** in windows and **bash** for linux.
>- Creates the script whit all the command to build the project, except the extraction operation.
>- And at last it executes the project.
```shell
javaBuild -cr
```

## Create or modify the manifesto file
- The manifesto file, is necessary to include or verify if you want to add to the build the extraction of the dependencies or not.
>- To include the extraction files of a dependency: `javabuild --i` otherwise `javabuild --i n` 

## Build the project

- All can be done with only 1 command
>- It combines the commands: *compile* & *create jar* to build the project.
>- The extraction of the jar dependency need to execute manually with: `-ex` and only one time per jar dependency.
>- If you have configured the manifesto the dependency extraction will behave as describe previously.
```shell
javaBuild --build
```
>>- Also you can give the source folder of the **class** files.
>>- If not *.\bin\* will be use
```shell
javaBuild --build -s .\testing\
```

## Run or execute the project

- Uses the class files and the main class to execute and run the application.
>- it compiles to *.\bin\* folder and executes the project using the .class files.
>- you can specify the java class that you want to execute: 
```pwsh
javabuild --run .\src\App.java
```
>- you can give also the source directory
```pwsh
javabuild --run .\src\App.java -s .\testing\
```
>>- if you don't give the class to execute the main class is selected
>>- additional you can execute CLI command with this method too
```pwsh
javabuild --run --h
```

## Add an external jar dependency

- Used to add a framework or library file type *.jar*.
>- `javabuild --add dependency.jar`
>>- add an external jar file to the lib folder of the project
>- or you can use the directory name
>- `javabuild --add ./folderName`
>>- Only works when the lib file don't contain modules.

------

# Additional info

This project use [javaBuild_tool](https://github.com/AlfonsoG-dev/javaBuild) to build itself.
>- this app uses **powershell** to execute the commands on *WINDOWS*.
>- this app uses **bash** to execute commands on *LINUX*.
>- if you want to use the *CLI* tool you have to create an `.exe` file and place it in system path
>- for **LINUX**
```bash
$ echo '#!/usr/bin/java -jar' > myBin
$ cat my.jar >> myBin
$ chmod +x myBin
$ ./myBin
```
- with that you can create an **environment variable** and use it from there.

------

# Disclaimer
- this project is for educational purposes.
- security issues are not taken into account.
- Use it at your own risk.
