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
import java.io.IOException;
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
    public void process(Runtime runTime, StringBuilder processLog) throws Exception {
        String projectPath = super.config.getConfig(ConfigUtils.PRO_PROJECT_PATH_KEY);
        String project = super.config.getConfig(ConfigUtils.PRO_PROJECT_ID_KEY);
        String[] bugArr = super.config.getBugIdArr();
        LinkedHashMap<String, IAnalysisFunc> funcMap = TFuncRegister.getRegistClass(config);
        List<String> funcList = new ArrayList<>(funcMap.size());
        funcMap.forEach((key, func) -> funcList.add(key));
        String header = "element," + config.getConfig(ConfigUtils.PRO_FUNC_KEY) + "\r\n";
        for(String bug : bugArr){
            try {
                Map<String, Map<String, BigDecimal>> outputMap = new HashMap<>();
                int passCount = Utils.getAllTestArray(runTime, projectPath, project, bug).length;
                int failCount = Utils.getFailingTestArray(runTime, projectPath, project, bug).length;

                String middleParamsFilePath = System.getProperty("user.home") + File.separator + "mutationReports" + File.separator +
                        project + File.separator + bug + File.separator + "Ϊ�˺���������м������ֵ.csv";
                List<MiddleParams> middleParamList = new AllMiddleParams(middleParamsFilePath).getMiddleParams();
                double[][] martix = getMartix(middleParamList, passCount, failCount, project, bug);

                for (Map.Entry<String, IAnalysisFunc> entry : funcMap.entrySet()) {
                    String funcName = entry.getKey();
                    IAnalysisFunc analysisFunc = entry.getValue();
                    processOne(project, bug, outputMap, analysisFunc, funcName, middleParamList, martix);
                }
                StringBuilder finalResult = new StringBuilder();
                for (Map.Entry<String, Map<String, BigDecimal>> entry : outputMap.entrySet()) {
                    String element = entry.getKey();
                    Map<String, BigDecimal> funcScore = entry.getValue();
                    StringBuilder score = new StringBuilder(element).append(",");
                    for (String func : funcList) {
                        score.append(funcScore.get(func).toPlainString()).append(",");
                    }
                    finalResult.append(score.substring(0, score.length() - 1)).append("\r\n");
                }
                String outputFilePath = System.getProperty("user.home") + File.separator + "mutationReports" + File.separator +
                        project + File.separator + project + "-" + bug + "-MetallaxisSuspValue.csv";
                File outputFile = new File(outputFilePath);
                FileUtils.writeStringToFile(outputFile, header, "utf-8", false);
                FileUtils.writeStringToFile(outputFile, finalResult.toString(), "utf-8", true);
            }catch (Exception e){
                e.printStackTrace();
                processLog.append("��Ŀ:").append(project).append("��bug:").append(bug).append("�����쳣��ԭ��").append(Utils.getExceptionString(e)).append("\r\n");
            }
        }
    }

    public void processOne(String project, String bug, Map<String, Map<String, BigDecimal>> outputMap, IAnalysisFunc analysisFunc, String funcName,
                           List<MiddleParams> middleParamList, double[][] martix) throws Exception {

        Map<String, BigDecimal> mutatorSuspValueMap = getMutatorSuspValue(middleParamList, analysisFunc, martix);

        String reportFilePath = System.getProperty("user.home") + File.separator + "mutationReports" + File.separator + project + File.separator + bug + File.separator + "result.csv";
        List<CalMUSE.MutatorReport> reportList = CalMUSE.MutatorReport.getMutatorReportList(reportFilePath);
        //����+�кŷ���
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

    public double[][] getMartix(List<MiddleParams> list, int passCount, int failCount, String project, String bug) throws IOException {
        double[][] ret = new double[list.size()][4];
        StringBuilder martixStr = new StringBuilder();
        for(int i = 0, length = list.size(); i < length; i ++){
            MiddleParams middleParam = list.get(i);
            ret[i][0] = middleParam.getTotalKillingTests();
            ret[i][1] = middleParam.getTotalSucceedingTests();
            ret[i][2] = passCount - ret[i][0];
            ret[i][3] = failCount - ret[i][1];

            martixStr.append(ret[i][0]).append(",").append(ret[i][1]).append(",").append(ret[i][2])
                    .append(",").append(ret[i][3]).append("\r\n");
        }
        File martixCsv = new File(System.getProperty("user.home") + File.separator + "mutationReports" + File.separator +
                project + File.separator + bug + File.separator + "martix.csv");
        FileUtils.writeStringToFile(martixCsv, martixStr.toString(), "utf-8", false);

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
