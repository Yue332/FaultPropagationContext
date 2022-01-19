package com.finalmodule;

import com.finalmodule.base.FinalBean;
import com.finalmodule.base.IFinalProcessModule;
import com.utils.BuggyLine;
import com.utils.ConfigUtils;
import com.utils.DataDepNewBean;
import com.utils.SuspValueBean;
import com.utils.cal.IAnalysisFunc;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.math.BigDecimal;
import java.util.*;

/**
 * @author zzy
 * @date 2021-04-03 12:42
 */
public class CalculateExamNew extends FinalBean implements IFinalProcessModule {
    private String projectId;
    private String projectPath;
    private String[] bugIdArr;
    private String[] funcArr;
    
    private String suspValue;


    private String buggyLine;

    private String dataDepen;

    private String outputPath;

    @Override
    public void onPrepare() {
        super.onPrepare();
        projectId = super.config.getConfig(ConfigUtils.PRO_PROJECT_ID_KEY);
        projectPath = super.config.getConfig(ConfigUtils.PRO_PROJECT_PATH_KEY);
        bugIdArr = super.config.getBugIdArr();
        funcArr = super.config.getConfig(ConfigUtils.PRO_FUNC_KEY).split(",");
        buggyLine = projectPath + File.separator + "get_buggy_lines_" + projectId +
                File.separator + projectId + "-@:BUG_ID@.buggy.lines";
        
        suspValue = projectPath + File.separator + projectId + "_" + "@:BUG_ID@" + File.separator + "gzoltar_output" + File.separator + projectId + File.separator +
                "@:BUG_ID@" + File.separator + projectId + "-@:BUG_ID@-suspValue.csv";
        
        dataDepen = projectPath + File.separator + projectId + "_" + "@:BUG_ID@" + File.separator + "gzoltar_output" + File.separator + projectId + File.separator +
                "@:BUG_ID@" + File.separator + "dataDependence" + File.separator + "new" + File.separator +
                projectId + "-@:BUG_ID@-suspValue-src-@:FUNCID@.csv";
        outputPath = System.getProperty("user.home") + File.separator + "exam_new" + File.separator;
    }

    @Override
    public void process(Runtime runTime, StringBuilder processLog) throws Exception {
        String title = "bugid," + Arrays.toString(funcArr).replace("[", "").replace("]", "") + "\r\n";
        File outputFile = new File(outputPath);
        if(!outputFile.exists()) {
        	outputFile.mkdirs();
        }
        outputFile = new File(outputPath + projectId + "-Exam_new.csv");
        FileUtils.writeStringToFile(outputFile, title, "UTF-8", false);

        for(String bugId : bugIdArr){
            File buggyLineFile = new File(buggyLine.replaceAll("@:BUG_ID@", bugId));
            List<String> tmpList = FileUtils.readLines(buggyLineFile);
            List<BuggyLine> buggyLineBeanList = BuggyLine.getBuggyLineList(tmpList);
            List<String> buggyLineList = BuggyLine.getAllElements(buggyLineBeanList);

            if(!buggyLineFile.exists()){
                throw new Exception("[ERROR] 未找到buggyLine文件：" + buggyLineFile.getAbsolutePath());
            }
            
            File suspValueFile = new File(this.suspValue.replaceAll("@:BUG_ID@", bugId));
            if(!suspValueFile.exists()) {
            	throw new Exception("[ERROR] 未找到怀疑度文件：" + suspValueFile.getAbsolutePath());
            }
            List<String> suspValueList = FileUtils.readLines(suspValueFile);
            suspValueList.remove(0);

            StringBuilder lineData = new StringBuilder(bugId).append(",");
            for(int funcIdx = 0; funcIdx < this.funcArr.length; funcIdx ++) {
            	String func = funcArr[funcIdx];
                File dataDepNewFile = new File(dataDepen.replaceAll("@:BUG_ID@", bugId)
                        .replaceAll("@:FUNCID@", func));
                if (!dataDepNewFile.exists()) {
                    throw new Exception("[ERROR] 未找到数据依赖文件：" + dataDepNewFile.getAbsolutePath());
                }

                List<String> dataDepList = FileUtils.readLines(dataDepNewFile);
                List<DataDepNewBean> beanList = DataDepNewBean.getList(dataDepList);
                List<String> elements = DataDepNewBean.getAllElements(beanList);

                List<SuspValueBean> suspBeanList = SuspValueBean.getBeanList(suspValueList, funcIdx);
                // 倒序排序
                Collections.sort(suspBeanList);
                
                // 将怀疑度表中含有数据依赖语句的行剔除
                for(String element : elements) {
                	SuspValueBean.removeElement(suspBeanList, element);
                }
                // 将数据依赖得到的语句放入怀疑度表中
                for(int idx = 0, length = elements.size(); idx < length; idx ++) {
                	String line = elements.get(idx) + "," + "0";
                	SuspValueBean createBean = new SuspValueBean(line, 0);
                	suspBeanList.add(idx, createBean);
                }
                // 输出到临时文件，以便调试
//                File tmpFile = new File(suspValueFile.getParent() + File.separator + "tmp" + File.separator + "tmp-" + bugId + "-" + func + ".csv");
//                FileUtils.writeStringToFile(tmpFile, "", false);
//                for(SuspValueBean bean : suspBeanList) {
//                	FileUtils.writeStringToFile(tmpFile, bean.toString() + "\r\n", true);
//                }
                
                // 计算exam
                int n = 0;
                int m = suspBeanList.size();
                for(SuspValueBean bean : suspBeanList) {
                	n ++;
                	if(buggyLineList.contains(bean.getElement())) {
                		break;
                	}
                }
                BigDecimal score = new BigDecimal(n + "").divide(new BigDecimal(m + ""), IAnalysisFunc.scale, IAnalysisFunc.roundingMode);
                lineData.append(score.toPlainString()).append(",");

            
            }

            FileUtils.writeStringToFile(outputFile, lineData.toString() + "\r\n", "UTF-8", true);


        }

        StringBuilder avager = new StringBuilder("avager,");
        List<String> examList = FileUtils.readLines(outputFile);
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
        FileUtils.writeStringToFile(outputFile, avager.substring(0, avager.length() - 1), true);
    }
}
