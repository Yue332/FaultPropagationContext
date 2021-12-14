package com.finalmodule;

import com.finalmodule.base.FinalBean;
import com.finalmodule.base.IFinalProcessModule;
import com.utils.BuggyLine;
import com.utils.ConfigUtils;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.List;

public class FormatBuggyLine extends FinalBean implements IFinalProcessModule {
    @Override
    public void process(Runtime runTime, StringBuilder processLog) throws Exception {
        String projectPath = config.getConfig(ConfigUtils.PRO_PROJECT_PATH_KEY);
        String project = config.getConfig(ConfigUtils.PRO_PROJECT_ID_KEY);
        String[] allBugIdArr = config.getBugIdArr();

        File outputFile = new File(System.getProperty("user.home") + File.separator + "buggyLines" + File.separator + project + "-buggyline.csv");
        FileUtils.writeStringToFile(outputFile, "", "utf-8", false);
        StringBuilder data = new StringBuilder();
        for (String bug : allBugIdArr) {
            data.append(bug).append("\r\n");
            List<BuggyLine> buggyLineList = BuggyLine.getBuggyLineList(projectPath, project, bug);
            buggyLineList.forEach(row -> {
                data.append(row.getElement()).append(",").append(row.getOther()).append("\r\n");
            });
        }
        FileUtils.writeStringToFile(outputFile, data.toString(), "utf-8", true);
    }
}
