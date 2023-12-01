# Java build tool
>- basic build tool for simple java proyects
>- build the proyect with 1 or 3 simple commands

------

## Features 
- [x] compile the proyect to bin
- [x] extracts the jar files inside lib to create later the proyect jar file
- [x] create the proyect jar file
- [x] build the proyect, combine the 3 previusly mention
- [x] creae the folder structure of the proyect

## Issues to fix
- [ ] now the creation of the jar file includes the parent file of the extraction file of the lib dependency


------

## Usage
>- use the executable
```console
javabuild.exe --h
```
>- use the jar file
```console
java -jar JavaBuild.jar --h
```

### Compile

>- to compile the proyect
>>- `javaBuild.exe -cm`
>>>- it compiles the .java clases into bin

### Create a jar file of the proyect

>- to create a jar file is neccesary to include or verify dependencies or libs in the proyect
>>- in order to create the jar file we need to extract the content of all the jar files inside lib into: `extractionFiles`
>>- use the *extractionFiles* to include in the proyect jar file the depdendencies
>>>- `javaBuild.exe -ex`

>- now to create the proyect jar file
>>- `javaBuild.exe -cj`

### Build the proyect

>- all can be done with only 1 command
>>- `javaBuild.exe --build`
>>>- it combines the the commands: *"compile and createJar"* to build
>>>- the extraction of the jar dependency need to be execute manually with: `-ex` and only one time per jar dependency

### Create the proyect structure

>- creates the proyect structure
>>- `javaBuild.exe -b`
>>>- adds the manifesto file and the main class with the proyect folde name

### Add an external jar dependency

>- add an external dependency 
>>- `javabuild.exe --add dependency.jar`
>>>- add an external jar file to the lib folder of the proyect

------

## Aditional info
>- this app uses powershell to execute the commands
>- if you want to use the CLI tool you have to create a `.exe` file and place it in system path

------

## Disclaimer
>- this proyect is for educational purposes
>- it is not intended to create a full program
>- security issues are not taken into account
