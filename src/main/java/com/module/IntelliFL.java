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

    private String command1 = "java -cp @:BUILD_TEST@;@:BUILD@ -javaagent:intelliFL.jar=covLevel=meth-cov org.junit.runner.JUnitCore org.junit.runner.JUnitCore @:TEST_CLASS@ \r\n";

    private String command2 = "java -cp /home/yy/intelliFL/intelliFL.jar set.intelliFL.cg.CallGraphBuilder @:BUILD@ \r\n";
    @Override
    public void process(Runtime runTime) throws Exception {
        String buildPath = projectPath + File.separator + Utils.getSrcDir(runTime, projectPath);
        String buildTestPath = projectPath + File.separator + Utils.getBuildTest(runTime, projectPath);
        String[] allTestArray = Utils.getAllTestArray(runTime, projectPath, projectId, bugId);

        File methcovFile = new File(System.getProperty("user.home") + File.separator + "intelliFL" + File.separator + projectId + "_methcov.sh");
        StringBuilder methcov = new StringBuilder();
        for (String test : allTestArray){
            methcov.append(command1.replace("@:BUILD_TEST@", buildTestPath).
                    replace("@:BUILD@", buildPath)
                    .replace("@:TEST_CLASS@", test));
        }
        FileUtils.writeStringToFile(methcovFile, methcov.toString(), true);

        File callGraphFile = new File(System.getProperty("user.home") + File.separator + "intelliFL" + File.separator + projectId + "_callgraph.sh");
        FileUtils.writeStringToFile(callGraphFile, command2.replace("@:BUILD@", buildPath));
    }

    @Override
    public void onPrepare() {

    }
}
