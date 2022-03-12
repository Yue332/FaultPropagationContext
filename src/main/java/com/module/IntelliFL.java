package com.module;

import com.utils.Bean;
import com.utils.Configer;
import com.utils.Utils;
import org.apache.commons.io.FileUtils;

import java.io.File;

public class IntelliFL extends Bean implements IProcessModule{
    public IntelliFL(Configer config) {
        super(config);
    }

    private String command1 = "java -cp @:BUILD_TEST@:@:BUILD@ -javaagent:intelliFL.jar=covLevel=meth-cov org.junit.runner.JUnitCore @:TEST_CLASS@ \r\n";

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

        File methcovFile = new File(System.getProperty("user.home") + File.separator + "intelliFL" + File.separator + projectId + "_methcov.sh");
        StringBuilder methcov = new StringBuilder();
        for (String test : allTestArray){
            System.out.println("testClass:" + test);
            methcov.append(command1.replace("@:BUILD_TEST@", buildTestPath).
                    replace("@:BUILD@", buildPath)
                    .replace("@:TEST_CLASS@", test));
        }
        methcov.append("\n").append(command3.replace("@:PROJECTID@", projectId).replace("@:BUGID@", bugId));
        FileUtils.writeStringToFile(methcovFile, methcov.toString(), false);

        File callGraphFile = new File(System.getProperty("user.home") + File.separator + "intelliFL" + File.separator + projectId + "_callgraph.sh");
        FileUtils.writeStringToFile(callGraphFile, command2.replace("@:BUILD@", buildPath), false);
    }

    @Override
    public void onPrepare() {

    }
}
