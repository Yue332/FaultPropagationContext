package com.finalmodule;

import com.finalmodule.base.FinalBean;
import com.finalmodule.base.IFinalProcessModule;
import com.utils.AllMiddleParams;
import com.utils.ConfigUtils;
import com.utils.MiddleParams;
import com.utils.Utils;
import com.utils.cal.IAnalysisFunc;
import com.utils.cal.TFuncRegister;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @program: SBTandem
 * @author: zhangziyi
 * @date: 2021/11/4
 * @description:
 **/
public class MetallaxisSuspValue extends FinalBean implements IFinalProcessModule {

    @Override
    public void process(Runtime runTime) throws Exception {
        String projectPath = super.config.getConfig(ConfigUtils.PRO_PROJECT_PATH_KEY);
        String project = super.config.getConfig(ConfigUtils.PRO_PROJECT_ID_KEY);
        String[] bugArr = super.config.getBugIdArr();
        LinkedHashMap<String, IAnalysisFunc> funcMap = TFuncRegister.getRegistClass(config);
        List<String> funcList = new ArrayList<>(funcMap.size());
        funcMap.forEach((key, func) -> funcList.add(key));
        Map<String, Map<String, BigDecimal>> outputMap = new HashMap<>();
        for(String bug : bugArr){
            for (Map.Entry<String, IAnalysisFunc> entry : funcMap.entrySet()){
                String funcName = entry.getKey();
                IAnalysisFunc analysisFunc = entry.getValue();
                processOne(runTime, projectPath, project, bug, outputMap, analysisFunc, funcName);
            }
        }

        String header = "element," + config.getConfig(ConfigUtils.PRO_FUNC_KEY) + "\r\n";
        String outputFilePath = System.getProperty("user.home") + File.separator + "mutationReports" + File.separator +
                project + File.separator + project + "-MetallaxisSuspValue.csv";
        File outputFile = new File(outputFilePath);
        FileUtils.writeStringToFile(outputFile, header, "utf-8", false);
        StringBuilder finalResult = new StringBuilder();
        for (Map.Entry<String, Map<String, BigDecimal>> entry : outputMap.entrySet()){
            String element = entry.getKey();
            Map<String, BigDecimal> funcScore = entry.getValue();
            StringBuilder score = new StringBuilder(element).append(",");
            for(String func : funcList){
                score.append(funcScore.get(func).toPlainString()).append(",");
            }
            finalResult.append(score.substring(0, score.length() - 1)).append("\r\n");
        }
        FileUtils.writeStringToFile(outputFile, finalResult.toString(), "utf-8", true);
    }

    public void processOne(Runtime runTime, String projectPath, String project, String bug,
                           Map<String, Map<String, BigDecimal>> outputMap, IAnalysisFunc analysisFunc, String funcName) throws Exception {
        int passCount = Utils.getAllTestArray(runTime, projectPath, project, bug).length;
        int failCount = Utils.getFailingTestArray(runTime, projectPath, project, bug).length;

        processOne(project, bug, outputMap, analysisFunc, funcName, passCount, failCount);
    }

    public void processOne(String project, String bug, Map<String, Map<String, BigDecimal>> outputMap, IAnalysisFunc analysisFunc, String funcName,
                            int passCount, int failCount) throws Exception {
        String middleParamsFilePath = System.getProperty("user.home") + File.separator + "mutationReports" + File.separator +
                project + File.separator + bug + File.separator + "为了后续计算的中间变量的值.csv";
        List<MiddleParams> middleParamList = new AllMiddleParams(middleParamsFilePath).getMiddleParams();
        double[][] martix = getMartix(middleParamList, passCount, failCount);

        Map<String, BigDecimal> mutatorSuspValueMap = getMutatorSuspValue(middleParamList, analysisFunc, martix);

        String reportFilePath = System.getProperty("user.home") + File.separator + "mutationReports" + File.separator + project + File.separator + bug + File.separator + "result.csv";
        List<CalMUSE.MutatorReport> reportList = CalMUSE.MutatorReport.getMutatorReportList(reportFilePath);
        //按类+行号分组
        Map<String, List<CalMUSE.MutatorReport>> groupByMap = reportList.stream().collect(Collectors.groupingBy(line -> line.getMutatedClass() + "#" + line.getLineNumber()));
        for(Map.Entry<String, List<CalMUSE.MutatorReport>> entry : groupByMap.entrySet()){
            String line = entry.getKey();
            List<CalMUSE.MutatorReport> mutatorReportList = entry.getValue();
            BigDecimal score = BigDecimal.ZERO;
            Map<String, BigDecimal> funcScore = outputMap.get(line) == null ? new HashMap<>() : outputMap.get(line);
            for(CalMUSE.MutatorReport report : mutatorReportList){
                score = score.max(mutatorSuspValueMap.get(report.getMutator()));
            }
            funcScore.put(funcName, score);
            outputMap.put(line, funcScore);
        }
    }

    private double[][] getMartix(List<MiddleParams> list, int passCount, int failCount){
        double[][] ret = new double[list.size()][4];
        for(int i = 0, length = list.size(); i < length; i ++){
            MiddleParams middleParam = list.get(i);
            ret[i][0] = middleParam.getTotalKillingTests();
            ret[i][1] = middleParam.getTotalSucceedingTests();
            ret[i][2] = passCount - ret[i][0];
            ret[i][3] = failCount - ret[i][1];
        }
        return ret;
    }

    private Map<String, BigDecimal> getMutatorSuspValue(List<MiddleParams> middleParamsList, IAnalysisFunc func, double[][] martix){
        Map<String, BigDecimal> mutatorSuspValue = new LinkedHashMap<>(middleParamsList.size());
        List<BigDecimal> valueList = func.onProcess(martix);
        for(int i = 0, length = middleParamsList.size(); i < length; i ++){
            MiddleParams middleParams = middleParamsList.get(i);
            mutatorSuspValue.put(middleParams.getMutator(), valueList.get(i));
        }
        return mutatorSuspValue;
    }
}
