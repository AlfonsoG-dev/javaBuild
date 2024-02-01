# Java build tool
>- basic build tool for simple java projects
>- build the project with 1 or 3 simple commands

# Depencies
- [compile_references](https://docs.oracle.com/en/java/javase/17/docs/specs/man/javac.html)
- [create_jar_references](https://docs.oracle.com/javase/tutorial/deployment/jar/index.html)
- [run_java_references](https://docs.oracle.com/en/java/javase/21/docs/specs/man/java.html)

------

# Features 
- [x] compile the project to bin
- [x] extracts the jar files inside lib to create later the project jar file
- [x] create the project jar file
- [x] build the project, combine the 3 previously mention
- [x] create the folder structure of the project
- [x] create the run powershell script and executes the project

# TODO's 
- [ ] add to the *--add* CLI command the aggregation of modules like the ones of `javaFX`
- [ ] implement the module aggregation for compile, run, create-jar, execution script
 
------

# Usage
>- use the executable
```console
javabuild.exe --h
```
>- use the jar file
```console
java -jar JavaBuild.jar --h
```

## Compile

>- to compile the project
>>- `javaBuild.exe -cm`
>>>- it compiles the .java clases into bin

## Create a jar file of the project

>- to create a jar file is necessary to include or verify dependencies or libs in the project
>>- in order to create the jar file we need to extract the content of all the jar files inside lib into: `extractionFiles`
>>- use the *extractionFiles* to include in the project jar file the dependencies
>>>- `javaBuild.exe -ex`

>- now to create the project jar file
>>- `javaBuild.exe -cj`

## Create or modify the manifesto file
>- to create the manifesto file, is necessary to include or verify if you want to add to the build the extraction of the dependencies or not.
>>- in order to create the jar file you can include the extraction files of a dependency: `javabuild.exe --i ex`
>>- in order to create the jar file and exclude the extraction files of a dependency: `javabuild.exe --i nn` 

## Build the project

>- all can be done with only 1 command
>>- `javaBuild.exe --build`
>>>- it combines the commands: *"compile and createJar"* to build
>>>- the extraction of the jar dependency need to be execute manually with: `-ex` and only one time per jar dependency

## Create the project structure

>- creates the project structure
>>- `javaBuild.exe -b`
>>>- adds the manifesto file and the main class with the project folder name

## Run or excute the project

>>- `javaBuild.exe -r`
>>>- creates the powershell script whit all the command to build the project, except the extraction operation
>>>- executes the project

## Add an external jar dependency

>- add an external dependency 
>>- `javabuild.exe --add dependency.jar`
>>>- add an external jar file to the lib folder of the project
>>>- this command depends on [filesManager](https://github.com/AlfonsoG-dev/filesManager)

------

# Additional info
>- this app uses powershell to execute the commands
>- if you want to use the CLI tool you have to create a `.exe` file and place it in system path

------

# Disclaimer
>- this project is for educational purposes
>- it is not intended to create a full program
>- security issues are not taken into account
>- only works when the lib file do not contain modules
