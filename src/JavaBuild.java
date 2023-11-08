import Operations.Operation;
class JavaBuild {
    public static void main(String[] args){
        for(int i=0; i<args.length; ++i) {
            Operation mia = new Operation(".\\");
            switch(args[i]) {
                case "-c":
                    mia.CompileProyectOperation();
                    break;
                case "-cj":
                    mia.CreateJarOperation();
                    break;
                case "--h":
                    System.out.println("not implemented yet");
                    break;
                default: 
                    System.out.println("use --h for help");
                    break;
            }
        }
    }
}
