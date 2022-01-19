package com.finalmodule;

import com.finalmodule.base.FinalBean;
import com.finalmodule.base.IFinalProcessModule;
import com.utils.BuggyLine;
import com.utils.ConfigUtils;
import com.utils.Utils;
import com.utils.cal.IAnalysisFunc;
import com.utils.cal.topn.FuncSuspValue;
import com.utils.cal.topn.SuspValueNotFoundException;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.math.BigDecimal;
import java.util.*;

/**
 * @program: SBTandem
 * @author: zhangziyi
 * @date: 2021/11/13
 * @description:
 **/
public class CalExamUseMetallaxis extends FinalBean implements IFinalProcessModule {
    @Override
    public void process(Runtime runTime, StringBuilder processLog) throws Exception {
        String projectPath = config.getConfig(ConfigUtils.PRO_PROJECT_PATH_KEY);
        String project = config.getConfig(ConfigUtils.PRO_PROJECT_ID_KEY);
        String[] bugIdArr = config.getBugIdArr();
        String[] sortFuncArr = config.getConfig(ConfigUtils.SORT_FUNC).split(",");

        process(projectPath, project, bugIdArr, sortFuncArr, processLog);
    }

    public void process(String projectPath, String project, String[] bugIdArr, String[] sortFuncCustom, StringBuilder log) throws Exception {
        List<FuncSuspValue> funcSuspValueList = findUseableSuspValue(project, bugIdArr, log);
        String[] sortFuncArr = sortFuncCustom;
        if (sortFuncArr == null || sortFuncArr.length == 0 || (sortFuncArr.length == 1 && "all".equals(sortFuncArr[0]))) {
            System.out.println("[INFO] 未配置排序公式或配置为all，使用怀疑度文件中公式交集进行排序");
            sortFuncArr = getSortFuncs(funcSuspValueList);
            System.out.println("[INFO] 排序公式为：" + Arrays.toString(sortFuncArr));
        }
        Map<String, Map<String, BigDecimal>> finalMap = new HashMap<>(bugIdArr.length);
        for (FuncSuspValue suspValue: funcSuspValueList) {
            String bug = suspValue.getBug();
            List<BuggyLine> buggyLine = BuggyLine.getBuggyLineList(projectPath, project, bug);
            List<String> buggyLineElements = BuggyLine.getAllElements(buggyLine);

            Map<String, BigDecimal> funcExamMap = new HashMap<>(sortFuncArr.length);
            for (String sortFunc : sortFuncArr) {
                suspValue.sortReversed(sortFunc);
                List<String> suspValueElementList = suspValue.getAllElements();
                int index = suspValueElementList.size();
                for (int i = 0, length = suspValueElementList.size(); i < length; i++) {
                    String suspValueElement = suspValueElementList.get(i);
                    if (buggyLineElements.contains(suspValueElement)) {
                        index = i;
                        break;
                    }
                }
                BigDecimal exam = new BigDecimal(index).divide(new BigDecimal(suspValueElementList.size()), IAnalysisFunc.scale, IAnalysisFunc.roundingMode);
                funcExamMap.put(sortFunc, exam);
            }
            finalMap.put(bug, funcExamMap);
        }

        File outputFile = new File(System.getProperty("user.home") + File.separator +
                "EXAM" + File.separator + project + File.separator + project + "-ExamMetallaxis.csv");
        StringBuilder result = new StringBuilder(getHeader(sortFuncArr));
        String[] finalSortFuncArr = sortFuncArr;
        Map<String, BigDecimal> avageMap = new HashMap<>(finalSortFuncArr.length);
        finalMap.forEach((bugId, scoreMap) -> {
            result.append(bugId);
            for (String sortFunc : finalSortFuncArr) {
                avageMap.put(sortFunc, avageMap.containsKey(sortFunc) ? avageMap.get(sortFunc).add(scoreMap.get(sortFunc)) : scoreMap.get(sortFunc));
                result.append(",").append(scoreMap.get(sortFunc).toPlainString());
            }
            result.append("\r\n");
        });
        result.append("avage");
        for(String sortFunc : finalSortFuncArr){
            String funcAvageScore = avageMap.get(sortFunc).divide(new BigDecimal(funcSuspValueList.size()), IAnalysisFunc.scale, IAnalysisFunc.roundingMode).toPlainString();
            result.append(",").append(funcAvageScore);
        }
        FileUtils.writeStringToFile(outputFile, result.toString(), "utf-8", false);
    }

    public String getHeader(String[] funcArr) {
        StringBuilder header = new StringBuilder("bugid");
        for (String func : funcArr) {
            header.append(",").append(func);
        }
        return header.append("\r\n").toString();
    }

    public List<FuncSuspValue> findUseableSuspValue(String project, String[] bugArr, StringBuilder log) throws Exception {
        List<FuncSuspValue> list = new ArrayList<>();
        for (String bug : bugArr) {
            try {
                list.add(new FuncSuspValue(project, bug));
            } catch (Exception e) {
                if (e instanceof SuspValueNotFoundException) {
                    log.append("项目[").append(project).append("]bug[").append(bug).append("]未找到怀疑度文件，跳过！");
                    continue;
                }
                throw e;
            }
        }

        return list;
    }

    private String[] getSortFuncs(List<FuncSuspValue> funcSuspValueList){
        String[] sortFuncs = funcSuspValueList.get(0).getFuncArray();
        for(FuncSuspValue suspValue : funcSuspValueList){
            sortFuncs = Utils.intersect(sortFuncs, suspValue.getFuncArray());
        }
        return sortFuncs;
    }
}
