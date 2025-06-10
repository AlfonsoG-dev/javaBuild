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
                String toDelete = getCliValues(args, i, "-d");
                switch(args[i]) {
                    case "-ls":
                        if((i+1) < args.length) {
                            op.listProjectFiles(args[i+1]);
                        } else {
                            op.listProjectFiles(null);
                        }
                        break;
                    case "-cb":
                        if((i+1) < args.length) {
                            op.createProjectOperation();
                            op.createFilesOperation(args[i+1], source, target);
                        } else {
                            op.createProjectOperation();
                            op.createFilesOperation(null, source, target);
                        }
                        break;
                    case "--compile":
                        op.compileProjectOperation(source, target, release);
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
                        if((i+1)<args.length && !args[i+1].contains("-")) {
                            op.buildScript(haveExtractions, args[i+1], source, target);
                        } else {
                            op.buildScript(haveExtractions, "build", source, target);
                        }
                        break;
                    case "--i":
                        String author = getCliValues(args, i, "-a");
                        String runClass = getCliValues(args, i, "--class");
                        if((i+1) < args.length && args[i+1].equals("n")) {
                            op.createIncludeExtractions(false, author, runClass, source, target);
                        } else {
                            op.createIncludeExtractions(true, author, runClass, source, target);
                        }
                        break;
                    case "--build":
                        if(haveExtractions) {
                            op.compileProjectOperation(source, target, release);
                            op.extractJarDependencies();
                            op.createJarOperation(true, target);
                        } else {
                            op.compileProjectOperation(source, target, release);
                            op.createJarOperation(false, target);
                        }
                        break;
                    case "--scratch":
                        op.deleteDirectory(toDelete);
                        if(haveExtractions) {
                            op.compileProjectOperation(source, target, release);
                            op.extractJarDependencies();
                            op.createJarOperation(true, target);
                        } else {
                            op.compileProjectOperation(source, target, release);
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
                        op.compileProjectOperation(source, target, release);
                        if((i+1) < args.length) {
                            boolean 
                                conditionA = args[i+1].contains("-"),
                                conditionB = args[i+1].contains("--");
                            if(!(conditionA || conditionB)) {
                                op.runAppOperation(args[i+1], target);
                            } else {
                                op.runAppOperation("", target);
                            }
                        } else {
                            op.runAppOperation("", target);
                        }
                        break;
                    case "--h":
                        System.out.println("use -ls to list java | jar | class files in the given path with the option -s ./source/");

                        System.out.println("use -cb to create the project folder structure");

                        System.out.println("\t when creating give the author name");
                        System.out.println("\t\t -ls author-name");
                        System.out.println("");

                        System.out.println("use --compile to compile the project");
                        System.out.println("\t use -s ./source/ to tell to the compiler where the .java files are");
                        System.out.println("\t use -t ./target/ to tell to the compiler for where the .class files will be store");
                        System.out.println("\t use -r 23 to tell to the compiler the version of java you want to use for the release");
                        System.out.println("");

                        System.out.println("use --extract to extract the lib jar files");
                        System.out.println("");

                        System.out.println("use --jar to create the project jar file");
                        System.out.println("\t use -s ./source/ to tell to the compiler where the .class files are");
                        System.out.println("");

                        System.out.println("use --script to create the build script");
                        System.out.println("\t give the script name --script java-build");
                        System.out.println("\t\t if leave empty it will set the name to build");
                        System.out.println("\t use -s to set the directory of `.java` files");
                        System.out.println("\t use -t to set the directory of `.class` files");
                        System.out.println("");

                        System.out.println("use --i to include lib jar files as part of the build");
                        System.out.println("\t if you don't want to include jar files use n");
                        System.out.println("\t\t --i n");
                        System.out.println("\t if you want to change the author use -a and give the author name");
                        System.out.println("\t\t --i -a author-name");
                        System.out.println("");

                        System.out.println("use --build to build the project");
                        System.out.println("\t use -s ./source/ to tell to the compiler where the .java files are");
                        System.out.println("\t use -t ./target/ to tell to the compiler where the .class files are");
                        System.out.println("\t use -r 23 to tell to the compiler the version of java you want to use for the release");
                        System.out.println("");

                        System.out.println("use --scratch to build the project from scratch");
                        System.out.println("\t use -d ./delete/ to delete where the `.class` are, and build from scratch");
                        System.out.println("\t\t don't use this option with the others unless you want to get an error");
                        System.out.println("\t use -s ./source/ to tell to the compiler where the .java files are");
                        System.out.println("\t use -t ./target/ to tell to the compiler where the .class files are");
                        System.out.println("\t use -r 23 to tell to the compiler the version of java you want to use for the release");
                        System.out.println("");

                        System.out.println("use --add to include an external jar as a dependency");
                        System.out.println("\t you need to give the external jar file/dir path");
                        System.out.println("\t\t --add ./external/file.jar");
                        System.out.println("");

                        System.out.println("use --run to run the project without building it");
                        System.out.println("\t use -s ./source/ to tell to the compiler where the .java files are");
                        System.out.println("\t use -t ./target/ to tell to the compiler where the .class files are");
                        System.out.println("\t use -r 23 to tell to the compiler the version of java you want to use for the release");
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
