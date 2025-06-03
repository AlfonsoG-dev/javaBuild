import java.io.File;

import Operations.Operation;
class JavaBuild {
    public static void main(String[] args) {
        try {
            Operation op = new Operation("." + File.separator);
            boolean haveExtractions = op.haveIncludeExtraction();
            outter: for(int i=0; i<args.length; ++i) {
                switch(args[i]) {
                    case "-ls":
                        if((i+1) < args.length) {
                            op.listProjectFiles(args[i+1]);
                        } else {
                            System.err.println(
                                    "[Error] you must specify src or lib dir like: -ls .\\src"
                            );
                        }
                        break;
                    case "-cb":
                        if((i+1) < args.length) {
                            op.createProyectOperation();
                            op.createFilesOperation(args[i+1]);
                        } else {
                            System.err.println("[Error] no author provide");
                        }
                        break;
                    case "--compile":
                        if((i+1) < args.length && (i+2) < args.length) {
                            op.compileProyectOperation(args[i+1], args[i+2]);
                        } else {
                            op.compileProyectOperation("", "");
                        }
                        break;
                    case "--extract":
                        if(haveExtractions) {
                            op.extractJarDependencies();
                        } else {
                            System.out.println("[Info] Extraction files are not included");
                        }

                        break;
                    case "--jar":
                        String sourceDir = (i+1) < args.length ? args[i+1] : "";
                        if(haveExtractions) {
                            op.createJarOperation(true, sourceDir);
                        } else {
                            op.createJarOperation(false, sourceDir);
                        }
                        break;
                    case "--script":
                        if((i+1)<args.length) {
                            op.buildScript(haveExtractions, args[i+1]);
                        } else {
                            op.buildScript(haveExtractions, "build");
                        }
                        break;
                    case "--i":
                        String author = getCliValues(args, i, "-a");
                        if((i+1) < args.length && args[i+1].equals("n")) {
                            op.createIncludeExtractions(false, author);
                        } else {
                            op.createIncludeExtractions(true, author);
                        }
                        break;
                    case "--build":
                        String sourceFile = getCliValues(args, i, "-s");
                        String target = getCliValues(args, i, "-t");
                        if(haveExtractions) {
                            op.compileProyectOperation(sourceFile, target);
                            op.extractJarDependencies();
                            op.createJarOperation(true, target);
                        } else {
                            op.compileProyectOperation(sourceFile, target);
                            op.createJarOperation(false, target);
                        }
                        break;
                    case "--add":
                        if((i+1) < args.length) {
                            op.createAddJarFileOperation(args[i+1]);
                        } else {
                            System.out.println("[Info] the path to the jar file is needed");
                        }
                        break;
                    case "--run":
                        String source = getCliValues(args, i, "-s");
                        String targetFile = getCliValues(args, i, "-t");
                        op.compileProyectOperation(source, targetFile);
                        if((i+1) < args.length) {
                            boolean 
                                conditionA = args[i+1].contains("-"),
                                conditionB = args[i+1].contains("--");
                            if(!(conditionA || conditionB)) {
                                op.runAppOperation(args[i+1], source);
                            } else {
                                op.runAppOperation("", source);
                            }
                        } else {
                            op.runAppOperation("", source);
                        }
                        break;
                    case "--h":
                        System.out.println("use -ls to list java | jar | class files in the given path");
                        System.out.println("use -cb to create the proyect folder structure");
                        System.out.println("\t when creating give the author name");
                        System.out.println("\t\t -ls author-name");
                        System.out.println("");
                        System.out.println("use --compile to compile the proyect");
                        System.out.println("\t when compiling give the directory target");
                        System.out.println("\t\t --compile .\\source\\ .\\target\\");
                        System.out.println("\t if you don't provide the path for default its set to .\\bin\\");
                        System.out.println("");
                        System.out.println("use --extract to extract the lib jar files");
                        System.out.println("");
                        System.out.println("use --jar to create the proyect jar file");
                        System.out.println("\t when creating the jar file give the source directory");
                        System.out.println("\t\t --jar .\\testing\\");
                        System.out.println("\t if you don't provide the path for default its set to .\\bin\\");
                        System.out.println("");
                        System.out.println("use --script to create the build script");
                        System.out.println("\t if you want to name the script provide it following the -cr cli comand");
                        System.out.println("\t --script build or --script test");
                        System.out.println("");
                        System.out.println("use --i to include lib jar files as part of the build");
                        System.out.println("\t if you don't want to include jar files use n");
                        System.out.println("\t\t --i n");
                        System.out.println("\t if you want to change the author use -a and give the author name");
                        System.out.println("\t\t --i -a author-name");
                        System.out.println("");
                        System.out.println("use --build to build the proyect");
                        System.out.println("\t when building the proyect you can give the source directory");
                        System.out.println("\t\t --build -s .\\testing\\");
                        System.out.println("");
                        System.out.println("use --add to include an external jar as a dependency");
                        System.out.println("\t you need to give the external jar file/dir path");
                        System.out.println("\t\t --add .\\external\\file.jar");
                        System.out.println("");
                        System.out.println("use --run to run the proyect without building it");
                        System.out.println("\t when running the project you can give the source directory");
                        System.out.println("\t\t --run -s .\\testing\\");
                        System.out.println("\t when running the project you can give also the main class to run");
                        System.out.println("\t\t --run .\\main.java\\ -s .\\testing\\");
                        break;
                    default: 
                        System.out.println("use --h for help");
                        break outter;
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
