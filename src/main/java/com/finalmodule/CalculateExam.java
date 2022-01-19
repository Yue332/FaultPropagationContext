package com.finalmodule;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.utils.FileUtils;
import com.utils.cal.IAnalysisFunc;

import org.apache.commons.lang3.StringUtils;

import com.finalmodule.base.FinalBean;
import com.finalmodule.base.IFinalProcessModule;
import com.utils.ConfigUtils;

public class CalculateExam extends FinalBean implements IFinalProcessModule {
	private String outputFilePath;
	private String[] funcArr;
	private String[] allBugIdArr;
	private String suspValueCsvPath;
	private String labelCsvPath;
	private String projectId;

	@Override
	public void process(Runtime runTime, StringBuilder processLog) throws Exception {
		Map<String, String> map = new LinkedHashMap<String, String>(funcArr.length);
		StringBuilder avagScore = new StringBuilder("average score,");
		for(int iFuncIdx = 0; iFuncIdx < funcArr.length; iFuncIdx ++) {
			int count = 0;
			BigDecimal sum = new BigDecimal("0.0");
			List<String> cantUseId = new ArrayList<String>();
			for(String bugId : this.allBugIdArr) {
				if(super.failBugIdList.contains(bugId)) {
					System.out.println("[INFO] bugId【"+bugId+"】在之前的处理模块种存在异常，此处不计算该bugid");
					continue;
				}
				System.out.println("[INFO] 开始使用公式【"+funcArr[iFuncIdx]+"】计算bug【"+bugId+"】的得分");
				List<String> data = readAndLableData(bugId);
				BigDecimal score = readAndMetric(data, iFuncIdx, funcArr[iFuncIdx]);
	            if (score.doubleValue() != 1){
	                count ++;
	                //System.out.println(identifier+"项目"+i+"错误版本"+formName+"公式"+metricName+"得分：:"+score);
	                sum = sum.add(score);
//	                sum += score;
	            }else {
	                //暂时不可用错误版本
	                cantUseId.add(bugId);
	                System.out.print("[INFO] 暂时不可用的bugid：" + bugId);          	
	            }
	            System.out.println("[INFO] bug【"+bugId+"】得分为["+score.toPlainString()+"]");
	            if(map.containsKey(bugId)) {
	            	map.put(bugId, map.get(bugId) + "," + score.toPlainString());
	            }else {
	            	map.put(bugId, score.toPlainString());
	            }
			}
			
			BigDecimal avag = sum.divide(new BigDecimal(count), 7, RoundingMode.HALF_UP);
			avagScore.append(avag.toPlainString()).append(",");
	        System.out.println("项目【"+projectId+"】可用版本数（缺陷个数）：" + count);
	        System.out.println("公式【"+funcArr[iFuncIdx]+"】，度量模块【"+CalculateExam.class.getName()+"】，项目平均得分：" + avag.toPlainString());
	        System.out.println("---------------------");
		}
//		System.out.println("[INFO] " + outputMsg.toString());
		
		// title
		StringBuilder title = new StringBuilder("bug,");
		for(String func : funcArr) {
			title.append(func).append(",");
		}
		File outputFile = new File(this.outputFilePath);
		System.out.println("[INFO] 将模块得分输出至文件：" + outputFile.getAbsolutePath());
		FileUtils.writeStringToFile(outputFile, title.substring(0, title.length() - 1) + "\r\n", false);
		for(Map.Entry<String, String> entry : map.entrySet()) {
			FileUtils.writeStringToFile(outputFile, entry.getKey() + "," + entry.getValue() + "\r\n", true);
		}
		FileUtils.writeStringToFile(outputFile, avagScore.substring(0, avagScore.length() - 1) + "\r\n", true);
		
	}

	@Override
	public void onPrepare() {
		super.onPrepare();
		this.projectId = config.getConfig(ConfigUtils.PRO_PROJECT_ID_KEY);
		this.funcArr = config.getConfig(ConfigUtils.PRO_FUNC_KEY).split(",");
		this.outputFilePath = config.getConfig(ConfigUtils.PRO_PROJECT_PATH_KEY) + File.separator + "Exam.csv";
		this.allBugIdArr = config.getBugIdArr();
		this.suspValueCsvPath = config.getConfig(ConfigUtils.PRO_PROJECT_PATH_KEY) + File.separator + projectId + "_@:BUG_ID@" + File.separator + 
				"gzoltar_output" + File.separator + projectId + File.separator + "@:BUG_ID@" + File.separator + projectId + "-@:BUG_ID@-suspValue.csv";
		this.labelCsvPath = config.getConfig(ConfigUtils.PRO_PROJECT_PATH_KEY) + File.separator + projectId + "_@:BUG_ID@" + File.separator +
				"label" + File.separator + projectId + "-@:BUG_ID@-label.csv";
	}
	
    private List<String> readAndLableData(String bugId) throws Exception {
        File suspValueFile = new File(suspValueCsvPath.replaceAll("@:BUG_ID@", bugId));
        if(!suspValueFile.exists()) {
        	throw new Exception("[ERROR] 找不到文件【"+suspValueFile.getAbsolutePath()+"】，请使用gzoltar生成并将文件放置在目录【"+suspValueFile.getParent()+"】中!");
        }
        File labelFile = new File(labelCsvPath.replaceAll("@:BUG_ID@", bugId));
        if(!labelFile.exists()) {
        	throw new Exception("[ERROR] 找不到文件【"+labelFile.getAbsolutePath()+"】，请使用label生成并将文件放置在目录【"+labelFile.getParent()+"】中!");
        }
        List<String> data = FileUtils.readLines(suspValueFile,"UTF-8");
        data.remove(0); // remove title line
        List<String> label = FileUtils.readLines(labelFile,"UTF-8");
        //将标签放在数据最后(用,分隔)
        for (int i = 0, length = data.size(); i < length; i++) {
//        	System.out.println("[DEBUG] data.size() = " + length + " i = " + i);
            String temp = StringUtils.substringAfter(data.get(i),",") +","+ StringUtils.substringAfter(label.get(i),",");
            data.remove(i);
            data.add(i,temp);
        }
        return data;
    }
    
    private BigDecimal readAndMetric(List<String> data, int funcIndex, String funcName){
        File dataBeforeOrder = new File(config.getConfig(ConfigUtils.PRO_PROJECT_PATH_KEY) + File.separator +"data-" + funcName + "-before_order.csv");
        try {
        	FileUtils.writeStringToFile(dataBeforeOrder, "", false);
            for(String a : data) {
            	FileUtils.writeStringToFile(dataBeforeOrder, a + "\r\n", true);
            }
        }catch (Exception e) {
			System.out.println("[WARNING] 排序前文件输出异常！" + e.getMessage());
			e.printStackTrace();
		}

        for (int i = 0; i < data.size(); i++) {
            for (int j = i+1; j < data.size(); j++) {
                //使用正则表达式获取第i列的值,放进新数组吧
                //double valueA = Double.parseDouble(StringUtils.substringBefore(data.get(i),","));
                //double valueB = Double.parseDouble(StringUtils.substringBefore(data.get(j),","));
                String[] tempString = data.get(i).split("\\,");
                BigDecimal valueA = new BigDecimal(tempString[funcIndex]);
                tempString = data.get(j).split("\\,");
                BigDecimal valueB = new BigDecimal(tempString[funcIndex]);

                int labelA = Integer.parseInt(StringUtils.substringAfterLast(data.get(i),","));
                int labelB = Integer.parseInt(StringUtils.substringAfterLast(data.get(j),","));
                //double类型比较大小
                if(valueA.compareTo(valueB) < 0){
                    String temp = data.get(i);
                    data.remove(i);
                    data.add(i,data.get(j-1));//remove之后，整体size-1
                    data.remove(j);
                    data.add(j,temp);
                }
                //求最佳和最差需要
                /*
                if((valueA == valueB) && (labelA < labelB)){
                    String temp = data.get(i);
                    data.remove(i);
                    data.add(i,data.get(j-1));//remove之后，整体size-1
                    data.remove(j);
                    data.add(j,temp);
                }
                 */

            }
        }
//        System.out.println("[DEBUG] data.size = " + data.size());
        File dataAfterOrder = new File(config.getConfig(ConfigUtils.PRO_PROJECT_PATH_KEY) + File.separator +"data-" + funcName + "-after_order.csv");
        try {
        	FileUtils.writeStringToFile(dataAfterOrder, "", false);
            for(String a : data) {
            	FileUtils.writeStringToFile(dataAfterOrder, a + "\r\n", true);
            }
        }catch (Exception e) {
			System.out.println("[WARNING] 排序后文件输出异常！" + e.getMessage());
			e.printStackTrace();
		}

        return dataMetric(data, funcIndex);
    }
    
    private BigDecimal dataMetric(List<String> data,int funcIndex) {
    // static double[] DataMetric(List<String> data,int col,String metric) {
        // double[] score=new double[2];

        /*
        int count = 0;  //查看到达错误语句前,有多少语句
        int label = 0;  //判断是否是错误语句
        double fault_susValue = 0;  //如果是错误语句,获取其怀疑值
        int sameSusValue_count = 0; //相同怀疑值,有多少语句
        int sameSusValueAndLabel_count =0;  //相同怀疑值和标签,有多少语句
        double best = 0.0 ,worse = 0.0;     //最优和最差
        boolean flag = true;    //判断是否第一次遇到

        for (int i = 0; i < data.size(); i++) {
            label = Integer.parseInt(StringUtils.substringAfterLast(data.get(i),","));
            if(label != 1){ //错误语句
                count++;
            }
            if(label == 1 && flag == true){
                flag = false;
                fault_susValue = Double.parseDouble(data.get(i).split("\\,")[col]);
                //fault_susValue = Double.parseDouble(StringUtils.substringBefore(data.get(i),","));
                for (int j = i; j < data.size(); j++) {
                    double temp = Double.parseDouble(data.get(j).split("\\,")[col]);
                    //double temp = Double.parseDouble(StringUtils.substringBefore(data.get(j),","));
                    if(temp == fault_susValue){ //怀疑值相同
                        sameSusValue_count ++;
                        int tempLabel =  Integer.parseInt(StringUtils.substringAfterLast(data.get(j),","));
                        if(tempLabel == 1){
                            sameSusValueAndLabel_count++;
                        }
                    }
                }
                break; // 到达第一个错误语句位置,跳出
            }
        }
        best = (float)count / data.size();
        worse = (float)(count + sameSusValue_count - sameSusValueAndLabel_count)/data.size();
        score[0] = best;
        score[1] = worse;
        */
        int count = 0;  //查看到达错误语句前,有多少语句
        int label = 0;  //判断是否是错误语句
        for (int i = 0; i < data.size(); i++) {
            label = Integer.parseInt(StringUtils.substringAfterLast(data.get(i),","));
            if(label != 1) count++; //错误语句
            if(label == 1) break;
        }
        // 保留7位小数 四舍五入
        return new BigDecimal(String.valueOf(count)).divide(new BigDecimal(data.size()), IAnalysisFunc.scale, IAnalysisFunc.roundingMode);
//        return (double)count / data.size();
    }
}
