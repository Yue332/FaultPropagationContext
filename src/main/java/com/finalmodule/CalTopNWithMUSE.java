package com.finalmodule;

import com.finalmodule.base.FinalBean;
import com.finalmodule.base.IFinalProcessModule;
import com.utils.BuggyLine;
import com.utils.ConfigUtils;
import com.utils.ElementMuse;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * @program: SB-Tandem
 * @author: zhangziyi
 * @date: 2021/10/28
 * @description:
 **/
public class CalTopNWithMUSE extends FinalBean implements IFinalProcessModule {
    @Override
    public void process(Runtime runTime, StringBuilder processLog) throws Exception {
        String projectPath = config.getConfig(ConfigUtils.PRO_PROJECT_PATH_KEY);
        String project = config.getConfig(ConfigUtils.PRO_PROJECT_ID_KEY);
        String[] bugArray = config.getBugIdArr();
        int top = Integer.parseInt(config.getConfig(ConfigUtils.TOP_N_KEY));
        File outputFile = new File(System.getProperty("user.home") + File.separator + "mutationReports" + File.separator + project + File.separator + project + "-Top" + top + "-ByMUSE.csv");
        FileUtils.writeStringToFile(outputFile, "bugId,function,Top-N,contained,total_lines\r\n", "utf-8", false);
        for(String bug : bugArray){
            processOneBug(outputFile, projectPath, project, bug, top);
        }
    }


    public void processOneBug(File outputFile, String projectPath, String project, String bug, int top)throws Exception{
        List<ElementMuse> museList = ElementMuse.buildList(project, bug);
        // 按muse的值倒序排
        museList.sort(Comparator.comparingDouble(ElementMuse::getMuse).reversed());
        int realTop = Math.min(museList.size(), top);
        // 截取前top行
        museList = museList.subList(0, realTop);
        List<String> museElementList = new ArrayList<>(museList.size());
        museList.forEach(muse -> museElementList.add(muse.getElement()));
        // buggyLine
        List<String> buggyLineAllElements = BuggyLine.getAllElements(projectPath, project, bug);
        Map<String, Integer> map = BuggyLine.getTotalAndContained(buggyLineAllElements, museElementList);
        int total = map.get("TOTAL");
        int contained = map.get("CONTAINED");

        String data = project + "-" + bug + ",MUSE," + top + "," + contained + "," + total + "\r\n";
        FileUtils.writeStringToFile(outputFile, data, "utf-8", true);
    }
}
