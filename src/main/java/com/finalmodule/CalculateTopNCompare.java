package com.finalmodule;

import com.finalmodule.base.FinalBean;
import com.finalmodule.base.IFinalProcessModule;
import com.utils.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zzy
 * @date 2021-04-07 14:09
 */
public class CalculateTopNCompare extends FinalBean implements IFinalProcessModule {
    private String projectId;
    private String projectPath;
    private String[] bugIdArr;
    private String[] funcArr;

    private String buggyLine;

    private String output;

    private String title = "projectid-bugid,function,Top-N,contained,total_lines\r\n";

    private String csvPath;

    @Override
    public void onPrepare() {
        super.onPrepare();
        projectId = super.config.getConfig(ConfigUtils.PRO_PROJECT_ID_KEY);
        projectPath = super.config.getConfig(ConfigUtils.PRO_PROJECT_PATH_KEY);
        bugIdArr = super.config.getBugIdArr();
        funcArr = super.config.getConfig(ConfigUtils.PRO_FUNC_KEY).split(",");
        output = projectPath + File.separator + "Top@:TOP@-@:FUNC@_Compare.csv";

        buggyLine = projectPath + File.separator + "get_buggy_lines_" + projectId +
                File.separator + projectId + "-@:BUG_ID@.buggy.lines";

        csvPath = projectPath + File.separator + projectId + "_@:BUG_ID@"
                + File.separator + "gzoltar_output" + File.separator + projectId
                + File.separator + "@:BUG_ID@" + File.separator + projectId + "-@:BUG_ID@-suspValue_new.csv";
    }

    @Override
    public void process(Runtime runTime) throws Exception {
        int top = Integer.parseInt(config.getConfig(ConfigUtils.TOP_N_KEY));
        for(int i = 0; i < funcArr.length; i ++) {
            File outputFile = new File(output.replaceAll("@:TOP@", String.valueOf(top))
                                        .replaceAll("@:FUNC@", funcArr[i]));
            com.utils.FileUtils.writeStringToFile(outputFile, title, false);
            int funcSum = 0;
            for(String bugId : bugIdArr) {
                int sum = 0;
                File buggyLineFile = new File(buggyLine.replaceAll("@:BUG_ID@", bugId));
                if(!buggyLineFile.exists()) {
                    throw new Exception("[ERROR] 文件【"+buggyLineFile.getName()+"】不存在，请将文件放入目录【"+buggyLineFile.getParent()+"】中后重试！");
                }
                List<SortBean> list = Utils.getSuspValueListAfterSort(csvPath.replaceAll("@:BUG_ID@", bugId), i);
                int size = list.size();
                int topNsize = Math.min(size, top);
                if(size < top) {
                    System.out.println("[INFO] " + buggyLineFile.getName() + "中语句的行数("+size+")小于topN("+top+")");
                }
                List<String> topNList = new ArrayList<String>(topNsize);
                List<String> topNOnlyElementList = new ArrayList<String>(topNsize);
                for(int j = 0; j < topNsize; j ++) {
                    topNList.add(list.get(j).toString());
                    topNOnlyElementList.add(list.get(j).toString().split(",")[0]);
                }
                System.out.println("[INFO] bug【" + bugId + "】公式【" + funcArr[i] + "】倒叙排序前【" + topNsize + "】包含的语句： " + topNList.toString());
                List<String> buggyLineList = com.utils.FileUtils.readLines(buggyLineFile);
                for(int j = 0, length = buggyLineList.size(); j < length; j ++) {
                    String[] buggyLine = buggyLineList.get(j).split("#");
                    String newBuggyLine = buggyLine[0].replace("/", ".").replace(".java", "") + "#" + buggyLine[1];
                    buggyLineList.remove(j);
                    buggyLineList.add(j, newBuggyLine);
                }
                System.out.println("[INFO] " + buggyLineFile.getName() + "包含的bugyyline为：" + buggyLineList.toString());
                for(String buggyLine : buggyLineList) {
                    if(topNOnlyElementList.contains(buggyLine)) {
                        System.out.println("[INFO] topN中包含buggyLine:" + buggyLine);
                        sum += 1;
                    }
                }
                funcSum += sum;
                FileUtils.writeStringToFile(outputFile, projectId+"-"+bugId + "," + funcArr[i] + "," + topNsize + "," + (sum > 0 ? "1" : "0") + "," + sum + "\r\n", true);
                System.out.println("--------------------");
            }
            System.out.println("[INFO] 公式【"+funcArr[i]+"】top【"+top+"】sum=【"+funcSum+"】");
        }


    }
}
