import Operations.Operation;
class JavaBuild {
    public static void main(String[] args) {
        try {
            Operation op = new Operation(".\\");
            boolean haveExtractions = op.haveIncludeExtraction();
            for(int i=0; i<args.length; ++i) {
                switch(args[i]) {
                    case "-ls":
                        if((i+1) < args.length) {
                            op.listProjectFiles(args[i+1]);
                        } else {
                            System.err.println(
                                    "[ ERROR ]: you must specify src or lib dir like: -ls .\\src"
                            );
                        }
                        break;
                    case "-b":
                        if((i+1) < args.length && args[i+1].contains("(-n")) {
                            op.createProyectOperation();
                            op.createFilesOperation(getCliValues(args, i, "-n"));
                        } else {
                            System.err.println("[ ERROR ]: no author provide");
                        }
                        break;
                    case "-cm":
                        op.compileProyectOperation(getCliValues(args, i, "-t"));
                        break;
                    case "-ex":
                        if(haveExtractions) {
                            op.extractJarDependencies();
                        } else {
                            System.out.println("[ INFO ]: Extraction files are not included");
                        }

                        break;
                    case "-cj":
                        if(haveExtractions) {
                            op.createJarOperation(true);
                        } else {
                            op.createJarOperation(false);
                        }
                        break;
                    case "--i":
                        if((i+1) < args.length && args[i+1].equals("n")) {
                            op.createIncludeExtractions(false);
                        } else {
                            op.createIncludeExtractions(true);
                        }
                        break;
                    case "-r":
                        op.buildScript(haveExtractions);
                        break;
                    case "--build":
                        String target = getCliValues(args, i, "-t");
                        if(haveExtractions) {
                            op.compileProyectOperation(target);
                            op.extractJarDependencies();
                            op.createJarOperation(true);
                        } else {
                            op.compileProyectOperation(target);
                            op.createJarOperation(false);
                        }
                        break;
                    case "--add":
                        if((i+1) < args.length) {
                            op.createAddJarFileOperation(args[i+1]);
                        } else {
                            System.out.println("[ INFO ]: the path to the jar file is needed");
                        }
                        break;
                    case "--run":
                        op.compileProyectOperation("");
                        if((i+1) < args.length) {
                            boolean 
                                conditionA = args[i+1].contains("-"),
                                conditionB = args[i+1].contains("--");
                            if(!(conditionA || conditionB)) {
                                op.runAppOperation(args[i+1]);
                            } else {
                                op.runAppOperation("");
                            }
                        } else {
                            op.runAppOperation("");
                        }
                        break;
                    case "--h":
                        System.out.println("use -ls to list java | jar | class files in the given path");
                        System.out.println("use -b to create the proyect folder structure");
                        System.out.println("use -cm to compile the proyect");
                        System.out.println("use -ex to extract the lib jar files");
                        System.out.println("use -cj to create the proyect jar file");
                        System.out.println("use -r to create the build script");
                        System.out.println("use --i ex to include the extraction files for building jar files");
                        System.out.println("\tuse --i nex to exclude the extraction files for build jar files");
                        System.out.println("use --build to build the proyect");
                        System.out.println("use --add to include an external jar as a dependency");
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
    private static String getCliValues(String[] args, int i, String option) {
        String b = "";
        for(int j=i; j<args.length; ++j) {
            if(args[j].equals(option) && (j+1) < args.length) {
                b = args[i+1];
            }
        }
        return b;
    }
}
