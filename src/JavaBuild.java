import Operations.Operation;
class JavaBuild {
    public static void main(String[] args) {
        Thread nThread = new Thread("build app");
        try {
            nThread.start();
            for(int i=0; i<args.length; ++i) {
                Operation miOperation = new Operation(".\\");
                switch(args[i]) {
                    case "-b":
                        miOperation.createProyectOperation();
                        miOperation.createFilesOperation();
                        break;
                    case "-cm":
                        miOperation.compileProyectOperation();
                        break;
                    case "-ex":
                        miOperation.extractJarDependencies();
                        break;
                    case "-cj":
                        miOperation.createJarOperation();
                        break;
                    case "--build":
                        miOperation.compileProyectOperation();
                        miOperation.extractJarDependencies();
                        miOperation.createJarOperation();
                        break;
                    case "-r":
                        miOperation.createRunOperation();
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
                        System.out.println("use --build to build the proyect");
                        System.out.println("use -r to run the proyect");
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
