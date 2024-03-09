import Operations.Operation;
class JavaBuild {
    public static void main(String[] args) {
        try {
            Operation miOperation = new Operation(".\\");
            boolean haveExtractions = miOperation.haveIncludeExtraction();
            for(int i=0; i<args.length; ++i) {
                switch(args[i]) {
                    case "-b":
                        miOperation.createProyectOperation();
                        miOperation.createFilesOperation();
                        break;
                    case "-cm":
                        miOperation.compileProyectOperation();
                        break;
                    case "-ex":
                        if(haveExtractions) {
                            miOperation.extractJarDependencies();
                        } else {
                            System.out.println("Extraction files are not included");
                        }

                        break;
                    case "-cj":
                        if(haveExtractions) {
                            miOperation.createJarOperation(true);
                        } else {
                            miOperation.createJarOperation(false);
                        }
                        break;
                    case "--i":
                        if((i+1) < args.length && args[i+1].equals("ex")) {
                            miOperation.createIncludeExtractions(true);
                        } else if((i+1) < args.length && args[i+1].equals("nex")) {
                            miOperation.createIncludeExtractions(false);
                        }
                        break;
                    case "--build":
                        if(haveExtractions) {
                            miOperation.compileProyectOperation();
                            miOperation.extractJarDependencies();
                            miOperation.createJarOperation(true);
                        } else {
                            miOperation.compileProyectOperation();
                            miOperation.createJarOperation(false);
                        }
                        break;
                    case "-r":
                        miOperation.createBuildScript(haveExtractions);
                        break;
                    case "--run":
                        miOperation.compileProyectOperation();
                        boolean 
                            conditionA = args[i+1].contains("-"),
                            conditionB = args[i+1].contains("--");
                        if((i+1) < args.length && !(conditionA || conditionB)) {
                            miOperation.runAppOperation(args[i+1]);
                        } else {
                            miOperation.runAppOperation("");
                        }
                        break;
                    case "--add":
                        if((i+1) < args.length) {
                            miOperation.createAddJarFileOperation(args[i+1]);
                        } else {
                            System.out.println("the path to the jar file is needed");
                        }
                        break;
                    case "--h":
                        System.out.println("use -b to create the proyect folder structure");
                        System.out.println("use --add to include an external jar as a dependency");
                        System.out.println("use -cm to compile the proyect");
                        System.out.println("use -ex to extract the lib jar files");
                        System.out.println("use -cj to create the proyect jar file");
                        System.out.println("use --i ex to include the extraction files for building jar files");
                        System.out.println("\tuse --i nex to exclude the extraction files for build jar files");
                        System.out.println("use --build to build the proyect");
                        System.out.println("use -r to create the build script");
                        System.out.println("use --run to run the proyect without building it");
                        break;
                    default: 
                        System.out.println("use --h for help");
                        break;
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
