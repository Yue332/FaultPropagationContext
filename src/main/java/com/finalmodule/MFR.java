package com.finalmodule;

import com.finalmodule.base.FinalBean;
import com.finalmodule.base.IFinalProcessModule;
import com.utils.ConfigUtils;
import com.utils.cal.IAnalysisFunc;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class MFR extends FinalBean implements IFinalProcessModule {
    @Override
    public void process(Runtime runTime) throws Exception {
        String[] projectArr = super.config.getConfig(ConfigUtils.PRO_PROJECT_ID_KEY).split(",");
        String[] funcArr = super.config.getConfig(ConfigUtils.PRO_FUNC_KEY).split(",");
        StringBuilder info = new StringBuilder();
        for(String projectId : projectArr){
            for(String func : funcArr){
                File outputFile = new File(System.getProperty("user.home") + File.separator +
                        "MFR" + File.separator + projectId + File.separator + "MFR-" + func + ".csv");
                FileUtils.writeStringToFile(outputFile, "bugid,AR\r\n", false);
                for(String bugId : super.config.getBugIdArr()){
                    File buggyMethod = new File(System.getProperty("user.home") + File.separator +
                            "BuggyMethod" + File.separator + projectId + "-" + bugId + ".buggy.methods");
                    if(!buggyMethod.exists()){
//                        throw new Exception("bugid["+bugId+"]文件["+buggyMethod.getAbsolutePath()+"]不存在！");
                    	System.out.println("[WARNING] project["+projectId+"]bugid["+bugId+"]func["+func+"] buggyMethod文件不存在！");
                    	info.append("project["+projectId+"]bugid["+bugId+"]func["+func+"]文件["+buggyMethod.getAbsolutePath()+"]不存在！").append("\r\n");
                    	continue;
                    }
                    List<String> buggyMethodList = FileUtils.readLines(buggyMethod, "utf-8");
                    buggyMethodList.remove(0);
                    File suspMethod = new File(System.getProperty("user.home") + File.separator + "methodSuspValue" + File.separator + projectId + "-" + bugId + "-" + func + "-method-suspValue.csv");
                    if(!suspMethod.exists()){
                        throw new Exception("methodSuspValue文件["+suspMethod.getAbsolutePath()+"]不存在！");
                    }
                    List<String> suspMethodList = FileUtils.readLines(suspMethod, "utf-8");
                    suspMethodList.remove(0);
                    List<BigDecimal> faultList = new ArrayList<>();
                    List<BigDecimal> nList = new ArrayList<>();
                    int fault = 0;
                    for(String line : suspMethodList){
                        String[] tmp = line.split(",");
                        String method = tmp[0] + "," + tmp[1];
                        if(buggyMethodList.contains(method)){
                            nList.add(new BigDecimal(String.valueOf(suspMethodList.indexOf(line) + 1)));
                            fault ++;
                            faultList.add(new BigDecimal(String.valueOf(fault)));
                        }
                    }
                    if(nList.size() == 0){
                    	info.append("project["+projectId+"]bugid["+bugId+"]func["+func+"]buggyMethod为空！").append("\r\n");
                    	continue;
//                        throw new Exception("计算出n=0，文件["+suspMethod.getAbsolutePath()+"]中未找到与buggymethod文件["+buggyMethod.getAbsolutePath()+"]对应的值");
                    }
                    if(nList.size() != faultList.size()){
                        throw new Exception("n的数量与fault的数量不一致！");
                    }
                    BigDecimal sum = new BigDecimal("0");
                    System.out.println("nlist:" + nList.toString() + "\r\n faultList:" + faultList.toString());
                    for(int i = 0; i < nList.size(); i ++){
                        sum = sum.add(faultList.get(i).divide(nList.get(i), IAnalysisFunc.scale, IAnalysisFunc.roundingMode));
                    }
                    BigDecimal ar = sum.divide(new BigDecimal(String.valueOf(nList.size())), IAnalysisFunc.scale, IAnalysisFunc.roundingMode);
                    FileUtils.writeStringToFile(outputFile, bugId + "," + ar.toPlainString() + "\r\n", true);
                }
            }
        }
        
        File infoFile = new File(System.getProperty("user.home") + File.separator +
        		"MFR" + File.separator + "MFR-log.txt");
        FileUtils.writeStringToFile(infoFile, info.toString(), false);
    }
}
