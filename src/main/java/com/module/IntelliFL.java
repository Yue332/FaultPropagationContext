package com.module;

import com.utils.Bean;
import com.utils.Configer;
import com.utils.Utils;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.List;

public class IntelliFL extends Bean implements IProcessModule{
    public IntelliFL(Configer config) {
        super(config);
    }

    private String command1 = "java -cp @:BUILD_TEST@:@:BUILD@@:OTHERLIB@ -javaagent:intelliFL.jar=covLevel=meth-cov org.junit.runner.JUnitCore @:TEST_CLASS@ \r\n";

    private String command2 = "java -cp /home/yy/intelliFL/intelliFL.jar set.intelliFL.cg.CallGraphBuilder @:BUILD@ \r\n";

    private String command3 = "if [ ! -d /home/yy/MBFL/@:PROJECTID@/@:PROJECTID@-@:BUGID@/inteliFL ];then\n" +
            "        mkdir -p /home/yy/MBFL/@:PROJECTID@/@:PROJECTID@-@:BUGID@/inteliFL\n" +
            "fi\n" +
            "\n" +
            "mv intelliFL/intelliFL-meth-cov/* /home/yy/MBFL/@:PROJECTID@/@:PROJECTID@-@:BUGID@/inteliFL";
    @Override
    public void process(Runtime runTime) throws Exception {
        String buildPath = Utils.getSrcDir(runTime, projectPath);
        String buildTestPath = Utils.getBuildTest(runTime, projectPath);
        String[] allTestArray = Utils.getAllTestArray(runTime, projectPath);

        File methcovFile = new File(System.getProperty("user.home") + File.separator + "intelliFL" + File.separator + projectId + File.separator + projectId + "-" + bugId + "_methcov.sh");
        StringBuilder methcov = new StringBuilder();
        for (String test : allTestArray){
            System.out.println("testClass:" + test);
            methcov.append(command1.replace("@:BUILD_TEST@", buildTestPath).
                    replace("@:BUILD@", buildPath)
                    .replace("@:TEST_CLASS@", test)
                    .replace("@:OTHERLIB@", getOtherLib()));
        }
        methcov.append("\n").append(command3.replace("@:PROJECTID@", projectId).replace("@:BUGID@", bugId));
        FileUtils.writeStringToFile(methcovFile, methcov.toString(), false);

        File generalFile = new File(System.getProperty("user.home") + File.separator + "intelliFL" + File.separator + projectId + ".sh");
        if(!generalFile.exists()){
            FileUtils.writeStringToFile(generalFile, "#!/bin/bash\n", "utf-8", false);
        }
        File logPath = new File(methcovFile.getParentFile().getAbsolutePath() + File.separator + "log" + File.separator);
        if(!logPath.exists()){
            logPath.mkdirs();
        }

        StringBuilder shell = new StringBuilder();
        shell.append("chmod +x ").append(methcovFile.getAbsolutePath()).append("\n");
        shell.append("sh ").append(methcovFile.getAbsolutePath()).append(" > ").append(logPath.getAbsolutePath()).append(File.separator)
                .append("log-").append(methcovFile.getName()).append(".txt").append("\n");
        FileUtils.writeStringToFile(generalFile, shell.toString(), "utf-8", true);

//        File callGraphFile = new File(System.getProperty("user.home") + File.separator + "intelliFL" + File.separator + projectId + "_callgraph.sh");
//        FileUtils.writeStringToFile(callGraphFile, command2.replace("@:BUILD@", buildPath), false);
    }

    public String getOtherLib(){
        StringBuilder otherLib = new StringBuilder();
        String home = System.getProperty("user.home");
        if("Mockito".equals(projectId)) {
            // /home/yy/SBFL/Locate_buggylines/SBFL-Mockito/Mockito_1/lib/test/
            buildOtherLib(otherLib, projectPath + File.separator + "lib" + File.separator + "test" + File.separator);
            // /home/yy/libs/Mockito/
            buildOtherLib(otherLib, home + File.separator + "libs" + File.separator + projectId + File.separator);
            // /home/yy/SBFL/Locate_buggylines/SBFL-Mockito/Mockito_1/compileLib/
            buildOtherLib(otherLib, projectPath + File.separator + "compileLib" + File.separator);
            // /home/yy/SBFL/Locate_buggylines/SBFL-Mockito/Mockito_1/lib/build/
            buildOtherLib(otherLib, projectPath + File.separator + "lib" + File.separator + "build" + File.separator);
            return otherLib.toString();
        }else if ("Closure".equals(projectId)){
            // /home/yy/SBFL/Locate_buggylines/SBFL-Closure/Closure_1/build/lib/rhino.jar
            buildOtherLib(otherLib, projectPath + File.separator + "lib" + File.separator);

            File rhinoPath = new File(projectPath + File.separator + "build" + File.separator + "lib" + File.separator);
            if(rhinoPath.exists()){
                buildOtherLib(otherLib, rhinoPath.getAbsolutePath());
            }
            return otherLib.toString();
        }
        return "";
    }

    private void buildOtherLib(StringBuilder otherLib, String libPath){
        File libDir = new File(libPath);
        if(!libDir.exists()){
            throw new RuntimeException(libDir.getAbsolutePath() + "不存在！");
        }
        File[] libs = libDir.listFiles(file -> file.getName().endsWith(".jar") && !file.getName().contains("junit") &&
                !file.getName().contains("ant"));
        for (File lib : libs) {
            otherLib.append(":").append(lib.getAbsolutePath());
        }
    }

    @Override
    public void onPrepare() {

    }
}
