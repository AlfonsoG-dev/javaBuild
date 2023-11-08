import Operations.Operation;
class JavaBuild {
    public static void main(String[] args) {
        //Operation mio = new Operation(".\\");
        //mio.CreateJarOperation();
        for(int i=0; i<args.length; ++i) {
            Operation mia = new Operation(".\\");
            switch(args[i]) {
                case "-c":
                    mia.CompileProyectOperation();
                    break;
                case "-ex":
                    mia.ExtractJarDependencies();
                    break;
                case "-cj":
                    mia.CreateJarOperation();
                    break;
                case "--h":
                    System.out.println("use -c to compile the proyect");
                    System.out.println("use -ex to extract the lib jar files");
                    System.out.println("use -cj to create the proyect jar file");
                    break;
                default: 
                    System.out.println("use --h for help");
                    break;
            }
        }
    }
}
