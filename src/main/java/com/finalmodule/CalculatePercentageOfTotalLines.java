package com.finalmodule;

import com.finalmodule.base.FinalBean;
import com.finalmodule.base.IFinalProcessModule;
import com.utils.ConfigUtils;
import com.utils.cal.IAnalysisFunc;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.math.BigDecimal;
import java.util.*;

public class CalculatePercentageOfTotalLines extends FinalBean implements IFinalProcessModule {
    private String func;

    @Override
    public void process(Runtime runTime) throws Exception {
        String[] funcArr = config.getConfig(ConfigUtils.PRO_FUNC_KEY).split(",");
        if(funcArr.length != 1){
            throw new Exception("[ERROR] " + ConfigUtils.PRO_FUNC_KEY + "��ֻ����һ����ʽ��");
        }
        this.func = funcArr[0];

        String[] projectIdArr = super.config.getConfig(ConfigUtils.PRO_PROJECT_ID_KEY).split(",");
        String title = "Percentage_of_Total_lines," + Arrays.toString(projectIdArr).replace("[", "").replace("]", "") + "\r\n";
        String outputPath = getOutputPath();
        File outputFile = new File(outputPath);
        if(!outputFile.exists()){
            outputFile.mkdirs();
        }
        outputFile = new File(outputPath + getOuputFileName(func));
        FileUtils.writeStringToFile(outputFile, title, "UTF-8", false);

        Map<String, Map<String, String>> map = new HashMap<>();
        List<String> topList = new ArrayList<>();
        for(String projectId : projectIdArr){

            String projectPath = super.config.getConfig(ConfigUtils.PRO_PROJECT_PATH_KEY) + File.separator + "SBFL-" + projectId;

            String buggyLinePath = projectPath + File.separator + "get_buggy_lines_" + projectId + File.separator;

            String topNSrcPath = getTopNCsvPath(projectPath);

            int m = getAllBuggyLineCount(buggyLinePath);
            if(m == 0){
                throw new Exception("[ERROR] ��ȡbuggyline�ļ��õ�m=0���޷����㣡");
            }
            // {top,score}
            Map<String, String> scoreMap = new HashMap<>();
            Map<String, Integer> totalLineMap = getTotalLineCountFromTopNSrcFile(topNSrcPath);
            System.out.println("projectid: "+ projectId +"  totalLineMap :" + totalLineMap.toString());
            for(Map.Entry<String, Integer> entry : totalLineMap.entrySet()){
                String top = entry.getKey();
                if(!topList.contains(top)){
                    topList.add(top);
                }
                int size = entry.getValue();
                BigDecimal score = new BigDecimal(size + "").divide(new BigDecimal(m + ""), IAnalysisFunc.roundingMode, 2);
                BigDecimal scorePercent = score.multiply(new BigDecimal("100"));
                scoreMap.put(top, scorePercent.toPlainString() + "%");
            }
            map.put(projectId, scoreMap);
        }
        for(String top : topList){
            StringBuilder data = new StringBuilder("Top-").append(top).append(",");
            String[] socreArr = new String[projectIdArr.length];
            for(Map.Entry<String, Map<String, String>> entry : map.entrySet()){
                String projectId = entry.getKey();
                String score = entry.getValue().get(top);

                int idx = getArrayIdxByElement(projectIdArr, projectId);
                socreArr[idx] = score;
            }
            data.append(Arrays.toString(socreArr).replace("[", "").replace("]", "")).append("\r\n");
            FileUtils.writeStringToFile(outputFile, data.toString(), true);
        }

    }

    private int getAllBuggyLineCount(String buggyLinePath)throws Exception{
        File buggyLineDir = new File(buggyLinePath);
        if(!buggyLineDir.exists()){
            throw new Exception("[ERROR] buggyLineĿ¼["+buggyLineDir.getAbsolutePath()+"]�����ڣ�");
        }
        File[] buggyLines = buggyLineDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".buggy.lines");
            }
        });
        if(buggyLines == null || buggyLines.length == 0){
            throw new Exception("[ERROR] buggyLineĿ¼��δ�ҵ���.buggy.lines��β���κ��ļ�����ȷ��buggyline�ļ��Ƿ���ڣ�");
        }
        int sumLine = 0;
        for(File buggyLine : buggyLines){
            List<String> list = FileUtils.readLines(buggyLine, "utf-8");
            sumLine += list.size();
        }
        return sumLine;
    }

    /**
     *
     * @param topNSrcPath t
     * @return {topN, size}
     * @throws Exception file not found exception
     */
    protected Map<String, Integer> getTotalLineCountFromTopNSrcFile(String topNSrcPath)throws Exception{
        Map<String, Integer> map = new HashMap<>();
        File topNSrcPathDir = new File(topNSrcPath);
        if(!topNSrcPathDir.exists()){
            throw new Exception("[ERROR] topNĿ¼["+topNSrcPathDir.getAbsolutePath()+"]�����ڣ�");
        }
        File[] topNSrcs = topNSrcPathDir.listFiles((dir, name) -> name.endsWith(getTopNCsvNameEnd(this.func)));
        if(topNSrcs == null || topNSrcs.length == 0){
            throw new Exception("[ERROR] topNĿ¼��δ�ҵ���"+getTopNCsvNameEnd(func)+"��β���κ��ļ�����ȷ��topN�ļ��Ƿ���ڣ�");
        }
        for(File topNSrcFile : topNSrcs){
            String fileName = topNSrcFile.getName();
            String topN = getNbyTopNFileName(fileName);
            List<String> list = FileUtils.readLines(topNSrcFile, "utf-8");
            list.remove(0);
            int sumTotalLine = 0;
            for(String line : list) {
            	sumTotalLine += getTotalLine(line);
            }
            map.put(topN, sumTotalLine);
        }
        return map;
    }

    private String getNbyTopNFileName(String fileName){
        String[] tmp = fileName.split("-");
        return tmp[0].replaceAll("Top", "");
    }

    public static int getArrayIdxByElement(String[] array, String obj){
        for(int i = 0, length = array.length; i < length; i ++){
            if(obj.equals(array[i])){
                return i;
            }
        }
        return -1;
    }
    
    protected int getTotalLine(String line) {
    	String[] tmp = line.split(",");
    	return Integer.parseInt(tmp[4]);
    }
    
    protected String getTopNCsvPath(String projectPath) {
    	return projectPath + File.separator + "TopN_CSFL" + File.separator;
    }
    
    protected String getTopNCsvNameEnd(String func) {
    	return func + "_Src.csv";
    }
    
    protected String getOutputPath() {
    	return System.getProperty("user.home") + File.separator + "CSFL" + File.separator;
    }
    
    protected String getOuputFileName(String func) {
    	return "CSFL_Percentage_of_Total_lines.csv";
    }
}
