import java.io.IOException;

import Operations.Operation;
import Utils.OperationUtils;
class JavaBuild {
    public static void main(String[] args) {
        OperationUtils mios = new OperationUtils(".\\");
        for(int i=0; i<args.length; ++i) {
            Operation mia = new Operation(".\\");
            switch(args[i]) {
                case "-b":
                    mia.CreateProyectOperation();
                    break;
                case "-cm":
                    mia.CompileProyectOperation();
                    break;
                case "-ex":
                    mia.ExtractJarDependencies();
                    break;
                case "-cj":
                    mia.CreateJarOperation();
                    break;
                case "--build":
                    mia.CompileProyectOperation();
                    mia.ExtractJarDependencies();
                    mia.CreateJarOperation();
                    break;
                case "--h":
                    System.out.println("use -cm to compile the proyect");
                    System.out.println("use -ex to extract the lib jar files");
                    System.out.println("use -cj to create the proyect jar file");
                    System.out.println("use -b to create the proyect folder structure");
                    System.out.println("use --build to build the proyect");
                    break;
                default: 
                    System.out.println("use --h for help");
                    break;
            }
        }
    }
}
