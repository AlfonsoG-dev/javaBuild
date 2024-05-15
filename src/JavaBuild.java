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
                        if((i+1) < args.length) {
                            op.createProyectOperation();
                            op.createFilesOperation(args[i+1]);
                        } else {
                            System.err.println("[ ERROR ]: no author provide");
                        }
                        break;
                    case "-cm":
                        if((i+1) < args.length) {
                            op.compileProyectOperation(args[i+1]);
                        } else {
                            op.compileProyectOperation("");
                        }
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
                        String
                            extract = getCliValues(args, i, "n"),
                            author = getCliValues(args, i, "-a");
                        if(!extract.isEmpty()) {
                            op.createIncludeExtractions(false, author);
                        } else {
                            op.createIncludeExtractions(true, author);
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
                        System.out.println("\t give the name of the Author of the project");
                        System.out.println("use -cm to compile the proyect");
                        System.out.println("\t you can give a path where you want to save the .classs files");
                        System.out.println("\t default value is .\\bin\\");
                        System.out.println("use -ex to extract the lib jar files");
                        System.out.println("use -cj to create the proyect jar file");
                        System.out.println("use -r to create the build script");
                        System.out.println("use --i n to not include lib dependency as part of the build");
                        System.out.println("\tuse --i to include lib dependency as part of the build");
                        System.out.println("use --build to build the proyect");
                        System.out.println("use --add to include an external jar as a dependency");
                        System.out.println("\t give a path to the .jar lib dependency or the path to the directory");
                        System.out.println("use --run to run the proyect without building it");
                        System.out.println("\t you can give the path to the class to run");
                        System.out.println("\t default value is the main class inside the src directory");
                        System.out.println("\t it can also execute commands, they must start with the prefix ('-' or '--')");
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
                b = args[j+1];
            }
        }
        return b;
    }
}
