import Operations.Operation;
class JavaBuild {
    public static void main(String[] args) {
        for(int i=0; i<args.length; ++i) {
            Operation miOperation = new Operation(".\\");
            switch(args[i]) {
                case "-b":
                    miOperation.CreateProyectOperation();
                    miOperation.CreateFilesOperation();
                    break;
                case "-cm":
                    miOperation.CompileProyectOperation();
                    break;
                case "-ex":
                    miOperation.ExtractJarDependencies();
                    break;
                case "-cj":
                    miOperation.CreateJarOperation();
                    break;
                case "--build":
                    miOperation.CompileProyectOperation();
                    miOperation.ExtractJarDependencies();
                    miOperation.CreateJarOperation();
                    break;
                case "-r":
                    miOperation.CreateRunOperation();
                    break;
                case "--h":
                    System.out.println("use -cm to compile the proyect");
                    System.out.println("use -ex to extract the lib jar files");
                    System.out.println("use -cj to create the proyect jar file");
                    System.out.println("use -b to create the proyect folder structure");
                    System.out.println("use --build to build the proyect");
                    System.out.println("use -r to run the proyect");
                    break;
                default: 
                    System.out.println("use --h for help");
                    break;
            }
        }
    }
}
