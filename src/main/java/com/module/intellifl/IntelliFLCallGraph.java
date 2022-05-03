package com.module.intellifl;

import com.module.IProcessModule;
import com.utils.Bean;
import com.utils.Configer;
import com.utils.Utils;
import org.apache.commons.io.FileUtils;

import java.io.File;

public class IntelliFLCallGraph extends Bean implements IProcessModule {

    private String call_graph_command = "java -cp /home/yy/intelliFL/intelliFL.jar set.intelliFL.cg.CallGraphBuilder @:BUILD@ \r\n";

    private String outputPath = System.getProperty("user.home") + "/MBFL/@:PROJECTID@/@:PROJECTID@-@:BUGID@/inteliFL/callGraph/@:BUGID@.sh";
    private String outputGeneralPath = System.getProperty("user.home") + "/MBFL/@:PROJECTID@.sh";

    public IntelliFLCallGraph(Configer config) {
        super(config);
    }

    @Override
    public void process(Runtime runTime) throws Exception {
        String buildPath = Utils.getSrcDir(runTime, projectPath);
        File outputFile = new File(outputPath.replace("@:PROJECTID@", projectId)
                .replace("@:BUGID@", bugId));
        FileUtils.writeStringToFile(outputFile, call_graph_command.replace("@:BUILD@", buildPath), "utf-8", false);
        File generalFile = new File(outputGeneralPath);
        if(!generalFile.exists()){
            FileUtils.writeStringToFile(generalFile, "#!/bin/bash\n", "utf-8", false);
        }
        File logPath = new File(outputFile.getParentFile().getAbsolutePath() + File.separator + "log" + File.separator);
        if(!logPath.exists()){
            logPath.mkdirs();
        }

        String shell = "chmod +x " + outputFile.getAbsolutePath() + "\n" +
                "sh " + outputFile.getAbsolutePath() + " > " + logPath.getAbsolutePath() + File.separator +
                "log-" + outputFile.getName() + ".txt" + "\n";
        FileUtils.writeStringToFile(generalFile, shell, "utf-8", true);
    }

    @Override
    public void onPrepare() {

    }
}
