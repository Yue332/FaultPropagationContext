package com.utils.cal.topn;

import com.utils.BuggyLine;
import com.utils.ConfigUtils;
import com.utils.Configer;
import com.utils.cal.IAnalysisFunc;
import com.utils.cal.TFuncRegister;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @program: SBTandem
 * @author: zhangziyi
 * @date: 2021/11/10
 * @description:
 **/
public class TopNCalculator {
    public static final String OUTPUTFILEPATH = System.getProperty("user.home") + File.separator + "TOPN" + File.separator;
    public static final String header = "bugId,function,Top-N,contained,total_lines\r\n";

    private String projectPathBase;
    private String project;
    private String[] bugArray;
    private List<String> sortFunc;
    private int top;

    public TopNCalculator(String projectPathBase, String project, String[] bugArray, List<String> sortFunc, int top) {
        this.projectPathBase = projectPathBase;
        this.project = project;
        this.bugArray = bugArray;
        this.sortFunc = sortFunc;
        this.top = top;
    }

    public TopNCalculator(Configer config){
        this.projectPathBase = config.getConfig(ConfigUtils.PRO_PROJECT_PATH_KEY);
        this.project = config.getConfig(ConfigUtils.PRO_PROJECT_ID_KEY);
        this.top = Integer.parseInt(config.getConfig(ConfigUtils.TOP_N_KEY));
        this.bugArray = config.getBugIdArr();
        this.sortFunc = setSortFunc(config);
    }

    public List<String> setSortFunc(Configer config){
        try {
            Map<String, IAnalysisFunc> funcMap = TFuncRegister.getRegistClass(config, ConfigUtils.SORT_FUNC);
            List<String> list = new ArrayList<>(funcMap.size());
            funcMap.forEach((key, value) -> list.add(value.getName()));
            return list;
        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    public void calculate(String suspValueFilePath, StringBuilder log)throws Exception{
        a:for(String func : this.sortFunc){
            System.out.println("[INFO] ????????????" + func + "????Top-N");

            File outputFile = new File(OUTPUTFILEPATH + project + File.separator + project + "-" + func + "-Top-" + top + ".csv");

            StringBuilder data = new StringBuilder();
            for(String bug : bugArray){
                File suspValueFile = new File(suspValueFilePath.replace("@PROJECT@", project).replace("@BUG@", bug));
                FuncSuspValue suspValue;
                try {
                    suspValue = new FuncSuspValue(suspValueFile);
                }catch (Exception e){
                    if(e instanceof SuspValueNotFoundException){
                        System.out.println("[WARNING] ????["+project+"]bug["+bug+"]????????????????????????????????????????????");
                        log.append("????[").append(project).append("]bug[").append(bug).append("]????????????????????????????????????????????");
                        continue;
                    }
                    throw e;
                }
                if(!Arrays.asList(suspValue.getFuncArray()).contains(func)){
                    System.out.println("[INFO] ??????????????????????" + func + "????????????");
                    continue a;
                }
                suspValue.setCurrentFunc(func);
                // ????????
                suspValue.sortReversed();
                List<FuncSuspValue.ElementFuncSuspValue> suspValueList = suspValue.getSuspValueList();
                int realTop = Math.min(this.top, suspValueList.size());
                // ??????top??
                List<FuncSuspValue.ElementFuncSuspValue> subList = suspValueList.subList(0, realTop);

                List<String> elementList = new ArrayList<>(realTop);
                subList.forEach(row -> elementList.add(row.getElement()));
                List<String> buggyLineAllElements = BuggyLine.getAllElements(this.projectPathBase, project, bug);
                Map<String, Integer> map = BuggyLine.getTotalAndContained(buggyLineAllElements, elementList);
                int total = map.get("TOTAL");
                int contained = map.get("CONTAINED");

                data.append(project).append("-").append(bug).append(",").append(func).append(",").append(top).append(",").append(contained).append(",").append(total).append("\r\n");
            }
            FileUtils.writeStringToFile(outputFile, header, "utf-8", false);
            FileUtils.writeStringToFile(outputFile, data.toString(), "utf-8", true);
            System.out.println("[INFO] ????" + func + "????????");
        }
    }
}
