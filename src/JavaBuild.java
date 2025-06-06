import java.io.File;

import Operations.Operation;
class JavaBuild {
    public static void main(String[] args) {
        try {
            Operation op = new Operation("." + File.separator);
            boolean haveExtractions = op.haveIncludeExtraction();
            outter: for(int i=0; i<args.length; ++i) {
                String source = getCliValues(args, i, "-s");
                String target = getCliValues(args, i, "-t");
                String release = getCliValues(args, i, "-r");
                switch(args[i]) {
                    case "-ls":
                        op.listProjectFiles(source);
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
                        op.compileProyectOperation(source, target, release);
                        break;
                    case "--extract":
                        if(haveExtractions) {
                            op.extractJarDependencies();
                        } else {
                            System.out.println("[Info] Extraction files are not included");
                        }

                        break;
                    case "--jar":
                        if(haveExtractions) {
                            op.createJarOperation(true, source);
                        } else {
                            op.createJarOperation(false, source);
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
                        if(haveExtractions) {
                            op.compileProyectOperation(source, target, release);
                            op.extractJarDependencies();
                            op.createJarOperation(true, target);
                        } else {
                            op.compileProyectOperation(source, target, release);
                            op.createJarOperation(false, target);
                        }
                        break;
                    case "--scratch":
                        break;
                    case "--add":
                        if((i+1) < args.length) {
                            op.createAddJarFileOperation(args[i+1]);
                        } else {
                            System.out.println("[Info] the path to the jar file is needed");
                        }
                        break;
                    case "--run":
                        op.compileProyectOperation(source, target, release);
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
                        System.out.println("use -ls to list java | jar | class files in the given path with the option -s ./source/");

                        System.out.println("use -cb to create the proyect folder structure");

                        System.out.println("\t when creating give the author name");
                        System.out.println("\t\t -ls author-name");
                        System.out.println("");

                        System.out.println("use --compile to compile the proyect");
                        System.out.println("\t use -s ./source/ to tell to the compiler where the .java files are");
                        System.out.println("\t use -t ./target/ to tell to the compiler for where the .class files will be store");
                        System.out.println("\t use -r 23 to tell to the compiler the version of java you want to use for the release");
                        System.out.println("");

                        System.out.println("use --extract to extract the lib jar files");
                        System.out.println("");

                        System.out.println("use --jar to create the proyect jar file");
                        System.out.println("\t use -s ./source/ to tell to the compiler where the .class files are");
                        System.out.println("");

                        System.out.println("use --script to create the build script");
                        System.out.println("\t give the script name --script java-build");
                        System.out.println("\t if leave empty it will set the name to build");
                        System.out.println("");

                        System.out.println("use --i to include lib jar files as part of the build");
                        System.out.println("\t if you don't want to include jar files use n");
                        System.out.println("\t\t --i n");
                        System.out.println("\t if you want to change the author use -a and give the author name");
                        System.out.println("\t\t --i -a author-name");
                        System.out.println("");

                        System.out.println("use --build to build the proyect");
                        System.out.println("\t use -s ./source/ to tell to the compiler where the .java files are");
                        System.out.println("\t use -t ./target/ to tell to the compiler where the .class files are");
                        System.out.println("\t use -r 23 to tell to the compiler the version of java you want to use for the release");
                        System.out.println("");

                        System.out.println("use --scratch to build the proyect from scratch");

                        System.out.println("use --add to include an external jar as a dependency");
                        System.out.println("\t you need to give the external jar file/dir path");
                        System.out.println("\t\t --add ./external/file.jar");
                        System.out.println("");

                        System.out.println("use --run to run the proyect without building it");
                        System.out.println("\t use -s ./source/ to tell to the compiler where the .java files are");
                        System.out.println("\t use -t ./target/ to tell to the compiler where the .class files are");
                        System.out.println("\t use -r 23 to tell to the compiler the version of hava you want to use for the release");
                        System.out.println("\t\t the run command also accepts the execution of internal commands");
                        System.out.println("\t\t --run -ls ./src/");
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
        String b = null;
        for(int j=i; j<args.length; ++j) {
            if(args[j].equals(option) && (j+1) < args.length) {
                b = args[j+1];
            }
        }
        return b;
    }
}
