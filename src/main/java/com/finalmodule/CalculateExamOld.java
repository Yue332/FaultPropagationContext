package com.finalmodule;

import com.finalmodule.base.FinalBean;
import com.finalmodule.base.IFinalProcessModule;
import com.utils.BuggyLine;
import com.utils.ConfigUtils;
import com.utils.SuspValueBean;
import com.utils.cal.IAnalysisFunc;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CalculateExamOld extends FinalBean implements IFinalProcessModule {
    private String[] funcArr;
    private String[] allBugIdArr;
    private String suspValueCsvPath;
    private String buggyLinePath;
    private String examPath;

    @Override
    public void process(Runtime runTime, StringBuilder processLog) throws Exception {
        String projectId = super.config.getConfig(ConfigUtils.PRO_PROJECT_ID_KEY);
        String projectPath = config.getConfig(ConfigUtils.PRO_PROJECT_PATH_KEY) + File.separator +
                projectId + "_@:BUG_ID@" + File.separator;
        this.funcArr = config.getConfig(ConfigUtils.PRO_FUNC_KEY).split(",");
        this.allBugIdArr = config.getBugIdArr();
        this.suspValueCsvPath = projectPath + "gzoltar_output" + File.separator +
            projectId + File.separator + "@:BUG_ID@" + File.separator +
                projectId + "-@:BUG_ID@-suspValue.csv";

        this.buggyLinePath = config.getConfig(ConfigUtils.PRO_PROJECT_PATH_KEY) + File.separator + "get_buggy_lines_" + projectId +
                File.separator + projectId + "-@:BUG_ID@.buggy.lines";

        this.examPath = System.getProperty("user.home") + File.separator + "exam" + File.separator;

        String title = "bugid," + config.getConfig(ConfigUtils.PRO_FUNC_KEY) + "\r\n";
        File examFile = new File(examPath);
        if(!examFile.exists()) {
        	examFile.mkdirs();
        }
        examFile = new File(examPath + projectId + "-Exam.csv");
        FileUtils.writeStringToFile(examFile, title, false);
        
        for(String bugId : this.allBugIdArr){
            File buggyLineFile = new File(this.buggyLinePath.replaceAll("@:BUG_ID@", bugId));
            if(!buggyLineFile.exists()){
                throw new Exception("[ERROR] 文件["+buggyLineFile.getAbsolutePath()+"]不存在！");
            }
            List<String> tmpList = org.apache.commons.io.FileUtils.readLines(buggyLineFile);
            List<BuggyLine> buggyLineBeanList = BuggyLine.getBuggyLineList(tmpList);
            List<String> buggyLineList = BuggyLine.getAllElements(buggyLineBeanList);


            File suspValueFile = new File(suspValueCsvPath.replaceAll("@:BUG_ID@", bugId));
            if(!suspValueFile.exists()){
                throw new Exception("[ERROR] 文件["+suspValueFile.getAbsolutePath()+"]不存在！");
            }
            List<String> suspValueList = FileUtils.readLines(suspValueFile, "utf-8");
            suspValueList.remove(0);
            int m = suspValueList.size();
            StringBuilder lineData = new StringBuilder(bugId).append(",");
            for (int i = 0; i < funcArr.length; i++) {
                List<SuspValueBean> suspValueBeanList = SuspValueBean.getBeanList(suspValueList, i);
                Collections.sort(suspValueBeanList);
                int n = 0;
//                System.out.println("[DEBUG] 按第["+(i+1)+"]个公式倒序排序结果(前10):");
//                for(int idx = 0, length = Math.min(10, suspValueBeanList.size()); idx < length; idx ++) {
//                	System.out.println("[DEBUG] " + suspValueBeanList.get(idx).getLine());
//                }
                for(SuspValueBean bean : suspValueBeanList) {
                	n ++;
                	if(buggyLineList.contains(bean.getElement())) {
                		break;
                	}
                }
                BigDecimal score = new BigDecimal(n + "").divide(new BigDecimal(m + ""), IAnalysisFunc.scale, IAnalysisFunc.roundingMode);
                lineData.append(score.toPlainString()).append(",");
            }
            FileUtils.writeStringToFile(examFile, lineData.substring(0, lineData.length() - 1) + "\r\n", true);
        }
        
        StringBuilder avager = new StringBuilder("avager,");
        List<String> examList = FileUtils.readLines(examFile);
        examList.remove(0);
        List<BigDecimal> list = new ArrayList<BigDecimal>();
        for(int n = 0, length = examList.size(); n < length; n ++) {
        	String exam = examList.get(n);
        	String[] tmp = exam.split(",");
        	String[] tmpScore = new String[tmp.length - 1];
        	for(int j = 1; j < tmp.length; j ++) {
        		tmpScore[j - 1] = tmp[j];
        	}
        	for(int i = 0, l = Math.min(tmpScore.length, this.funcArr.length); i < l; i ++) {
        		if(n == 0) {
        			list.add(i, new BigDecimal(tmpScore[i]));
        		}else {
        			list.set(i, list.get(i).add(new BigDecimal(tmpScore[i])));
        		}
        	}
        }
        for(BigDecimal sumScore : list) {
        	avager.append(sumScore.divide(new BigDecimal(examList.size() + ""), IAnalysisFunc.scale, IAnalysisFunc.roundingMode)).append(",");
        }
        FileUtils.writeStringToFile(examFile, avager.substring(0, avager.length() - 1), true);

    }
}
