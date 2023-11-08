# Java build tool
>- basic build tool for simple java proyects
>- build the proyect with 1 or 3 simple commands

## Features 
- [x] compile the proyect to bin
- [x] extracts the jar files inside lib to create later the proyect jar file
- [x] create the proyect jar file
- [x] build the proyect, combine the 3 previusly mention
- [x] creae the folder structure of the proyect

## Usage
>- to compile the proyect
>>- `java -jar javaBuild.jar -c`
>>>- it compiles the .java clases into bin

>- to create a jar file is neccesary to include or verify dependencies or libs in the proyect
>>- in order to create the jar file we need to extract the content of all the jar files inside lib into: `extractionFiles`
>>- use the *extractionFiles* to include in the proyect jar file the depdendencies
>>>- `java -jar javaBuild.jar -ex`

>- now to create the proyect jar file
>>- `java -jar javaBuild.jar -cj`


>- all can be done with only 1 command
>>- `java -jar javaBuild --build`
>>>- it combines the 3 commands in one command

## TODO's
- [ ] make the Manifesto.txt and Main class of the proyect


## Disclaimer
>- this proyect is for educational purposes
>- it is not intended to create a full program
>- security issues are not taken into account
