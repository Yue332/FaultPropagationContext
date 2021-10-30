package com.run;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.utils.cal.IAnalysisFunc;

public class Test {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		String projectId = "Chart";
		String[] bugIdArr = new String[]{"1", "2"};
		String func = "com.utils.cal.func.Ochiai";

        File outputFile = new File("C:\\Users\\44789\\Desktop\\" +
                "MFR" + File.separator + projectId + File.separator + "MFR-" + func + ".csv");
        FileUtils.writeStringToFile(outputFile, "bugid,AR\r\n", false);
        for(String bugId : bugIdArr){
            File buggyMethod = new File("C:\\Users\\44789\\Desktop" + File.separator +
                    "BuggyMethod" + File.separator + projectId + "-" + bugId + ".buggy.methods");
            if(!buggyMethod.exists()){
                throw new Exception("bugid["+bugId+"]文件["+buggyMethod.getAbsolutePath()+"]不存在！");
            }
            List<String> buggyMethodList = FileUtils.readLines(buggyMethod, "utf-8");
            buggyMethodList.remove(0);
            File suspMethod = new File("C:\\Users\\44789\\Desktop" + File.separator + "methodSuspValue" + File.separator + projectId + "-" + bugId + "-" + func + "-method-suspValue.csv");
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
                throw new Exception("计算出n=0，文件["+suspMethod.getAbsolutePath()+"]中未找到与buggymethod文件["+buggyMethod.getAbsolutePath()+"]对应的值");
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
