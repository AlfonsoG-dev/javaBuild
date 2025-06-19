package models;

import java.io.File;
import java.util.Optional;

import utils.CommandUtils;
import utils.ModelUtils;

public class CompileModel {

    public final static String LOCAL_PATH = "." + File.separator;

    private String classPath;
    private String flags;

    private CommandUtils cUtils;
    private ModelUtils mUtils;


    public CompileModel(String sourcePath, String classPath, String flags) {
        this.classPath = classPath;
        if(flags.isEmpty()) {
            this.flags = "-Werror -Xlint:all -Xdiags:verbose";
        }
        this.flags = flags;
        cUtils = new CommandUtils(LOCAL_PATH);
        mUtils = new ModelUtils(sourcePath, classPath, LOCAL_PATH);

        // invariant
        CompileModel.verify(sourcePath, classPath);
    }

    /**
     * Create the compile command, it has two version.
     * </br> When you create from `--scratch` you don't need individual files; you need each directory in the project that has at least one .java file. 
     * </br> when you create from `--compile` or `--build` you only want the files that were modified later in the build process; for that you need each individual file to re-compile it.
     * @param release the java jdk version
     * @return the compile command
     */
   public String getCompileCommand(int release) {
        // create jar files command for compile operation
        StringBuffer 
            libFiles = new StringBuffer(),
            cLibFiles = new StringBuffer(),
            compile = new StringBuffer();

        // lib files
        libFiles.append(mUtils.getJarDependencies());

        String format = cUtils.compileFormatType(libFiles.toString(), classPath, flags, release);
        String srcClases = "";

        Optional<String> oSource = mUtils.getSourceFiles();
        if(oSource.isEmpty()) {
            System.out.println("[Info] No modified files to compile");
            return null;
        } else {
            srcClases = oSource.get();
        }

        if(!srcClases.contains("*.java")) {
            compile = new StringBuffer();
            compile.append(format);
            if(!libFiles.isEmpty()) {
                compile.append(" '");
                compile.append(classPath);
                compile.append(";");
                compile.append(libFiles);
                compile.append("' ");
            } else {
                compile.append(" -cp '");
                compile.append(classPath);
                compile.append("' ");
            }
            compile.append(srcClases);
        } else {
            compile.append(format);
            if(!libFiles.isEmpty()) {
                String cb = libFiles.substring(0, libFiles.length()-1);

                cLibFiles.append("'");
                cLibFiles.append(cb);
                cLibFiles.append("' ");

                compile.append(cLibFiles);
            }
            compile.append(srcClases);
        }
        return compile.toString();
    }



    // ----------------------------\\
    //   Verify non-null values    \\

    /**
     * verify that for this class you don't provide empty paths.
     * @param sourcePath where the .java files are.
     * @param classPath where to store .class files
     */
    public static void verify(String sourcePath, String classPath) {
        try {
            if(sourcePath.isEmpty() || classPath.isEmpty()) {
                throw new Exception("[Error] No empty paths allowed");
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
