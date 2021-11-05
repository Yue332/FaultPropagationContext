package com.finalmodule;

import com.finalmodule.base.FinalBean;
import com.finalmodule.base.IFinalProcessModule;
import com.utils.*;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.List;

/**
 * @author zzy
 * @date 2021-04-07 20:05
 */
public class CalculateTopNSrc extends FinalBean implements IFinalProcessModule {
    private String projectId;
    private String projectPath;
    private String[] bugIdArr;
    private String[] funcArr;

    private String buggyLine;

    private String output;

    private String title = "projectid-bugid,function,Top-N,contained,total_lines\r\n";

    private String dataDep;

    @Override
    public void onPrepare() {
        super.onPrepare();
        projectId = super.config.getConfig(ConfigUtils.PRO_PROJECT_ID_KEY);
        projectPath = super.config.getConfig(ConfigUtils.PRO_PROJECT_PATH_KEY);
        bugIdArr = super.config.getBugIdArr();
        funcArr = super.config.getConfig(ConfigUtils.PRO_FUNC_KEY).split(",");
        output = projectPath + File.separator + "Top@:TOP@-@:FUNC@_Src.csv";

        buggyLine = projectPath + File.separator + "get_buggy_lines_" + projectId +
                File.separator + projectId + "-@:BUG_ID@.buggy.lines";

        dataDep = projectPath + File.separator + projectId + "_" + "@:BUG_ID@" + File.separator + "gzoltar_output" + File.separator + projectId + File.separator +
                "@:BUG_ID@" + File.separator + "dataDependence" + File.separator + "new" + File.separator +
                projectId + "-@:BUG_ID@-suspValue-src-@:FUNCID@.csv";
    }

    @Override
    public void process(Runtime runTime, StringBuilder processLog) throws Exception {
        int top = Integer.parseInt(config.getConfig(ConfigUtils.TOP_N_KEY));
        // 遍历每个公式
        for (String func : funcArr) {
            File outputFile = new File(output.replaceAll("@:PROJECT_PATH@", projectPath)
                    .replaceAll("@:TOP@", super.config.getConfig(ConfigUtils.TOP_N_KEY))
                    .replaceAll("@:FUNC@", func));
            org.apache.commons.io.FileUtils.writeStringToFile(outputFile, title, false);

            for (String bugId : bugIdArr) {
                File buggyLineFile = new File(buggyLine.replaceAll("@:PROJECT_PATH@", projectPath)
                        .replaceAll("@:PROJECT_ID@", projectId)
                        .replaceAll("@:BUG_ID@", bugId));
                if (!buggyLineFile.exists()) {
                    throw new Exception("[ERROR] 文件" + buggyLineFile.getAbsolutePath() + "不存在！");
                }
                // org/jfree/chart/imagemap/StandardToolTipTagFragmentGenerator.java#65#        return " title=\"" + toolTipText
                // -> org.jfreee.chart.XXX 
                // -> org.jfreee.chart.XXX#65
                List<String> tmpList = org.apache.commons.io.FileUtils.readLines(buggyLineFile);
                List<BuggyLine> buggyLineBeanList = BuggyLine.getBuggyLineList(tmpList);
                List<String> buggyLineList = BuggyLine.getAllElements(buggyLineBeanList);


                File dataDepNewFile = new File(dataDep.replaceAll("@:BUG_ID@", bugId)
                        .replaceAll("@:FUNCID@", func));
                System.out.println("[INFO] 开始读取文件：" + dataDepNewFile.getAbsolutePath());
                if (!dataDepNewFile.exists()) {
                    throw new Exception("[ERROR] 文件" + dataDepNewFile.getAbsolutePath() + "不存在！");
                }
                
                // org.jfree.chart.imagemap.StandardToolTipTagFragmentGenerator#65,-1/65,0
                // -> org.jfree.chart.imagemap.StandardToolTipTagFragmentGenerator#65
                // -> org.jfree.chart.imagemap.StandardToolTipTagFragmentGenerator#-1
                List<String> dataDepList = org.apache.commons.io.FileUtils.readLines(dataDepNewFile);
                List<DataDepNewBean> beanList = DataDepNewBean.getList(dataDepList);
                List<String> elements = DataDepNewBean.getAllElements(beanList);

                int totalLines = 0;
                boolean contained = false;
                for (String buggyLine : buggyLineList) {
                    for (String element : elements) {
                        if (buggyLine.equals(element)) {
                            contained = true;
                            totalLines++;
                        }
                    }
                }
                String data = projectId + "-" + bugId + "," + func + "," + top + "," + (contained ? "1" : "0") + "," + totalLines + "\r\n";
                FileUtils.writeStringToFile(outputFile, data, true);
            }
        }
    }
}
