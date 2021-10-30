package com.finalmodule;

import org.apache.commons.io.FileUtils;

import com.finalmodule.base.FinalBean;
import com.finalmodule.base.IFinalProcessModule;
import com.utils.Bean;
import com.utils.ConfigUtils;
import com.utils.Configer;
import com.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @program: Test
 * @author: zhangziyi
 * @date: 2021/10/21
 * @description:
 **/
public class CalMUSE extends FinalBean implements IFinalProcessModule{
	private String[] projects;
	private String[] bugIds;
	
	@Override
	public void process(Runtime runTime) throws Exception {
		for(String project : projects) {
			for(String bugId : bugIds) {
				File resultCsvFile = new File(System.getProperty("user.home") + File.separator + "mutationReports" + File.separator + project + File.separator + bugId + File.separator + "result.csv");
				if(!resultCsvFile.exists()) {
					throw new Exception("[ERROR] 文件"+resultCsvFile.getAbsolutePath()+"不存在！");
				}
				String[] failingTests = getFailingTestArray(runTime, project, bugId);
				System.out.println("[DEBUGGER] failingTest : " + Arrays.toString(failingTests));
				cal(resultCsvFile, failingTests, project, bugId);
			}
		}
	}

	@Override
	public void onPrepare() {
		this.projects = super.config.getConfig(ConfigUtils.PRO_PROJECT_ID_KEY).split(",");
		this.bugIds = super.config.getBugIdArr();
		
	}
	
	public String COMMAND = "cd @PROJECT_PATH@,defects4j test";
	public String[] getFailingTestArray(Runtime runTime, String project, String bugId) throws Exception {
		String[] command = COMMAND.replace("@PROJECT_PATH@", super.config.getConfig(ConfigUtils.PRO_PROJECT_PATH_KEY) + File.separator + project + "_" + bugId + File.separator).split(",");
		String[] ret = Utils.executeCommandLine(runTime, command);
		String[] tmpArr = ret[1].split("-");
		List<String> list = new ArrayList<String>(tmpArr.length - 1);
		for(String str : tmpArr) {
			if(str.contains("Failing tests")) {
				continue;
			}
			list.add(str.replace("::", ".").trim());
		}
		return list.toArray(new String[] {});
	}

	public void cal(File resultCsvFile, String[] failingTests, String project, String bugId){
        int failingTestsNum = failingTests.length;
        try {
            List<String> csvList = FileUtils.readLines(resultCsvFile, "UTF-8");
            // 去掉表头
            csvList.remove(0);
            //转换为对象的方式
            List<MutatorReport> reportList = MutatorReport.getMutatorReportList(csvList);
            //根据变异体mutator列分组
            Map<String, List<MutatorReport>> groupByMap = reportList.stream().collect(Collectors.groupingBy(MutatorReport::getMutator));
            //每个mutator对应的totalKillingTests
            Map<String, BigDecimal> totalKillingTestsMap = new HashMap<>(groupByMap.size());
            Map<String, BigDecimal> totalSucceedingTestsMap = new HashMap<>(groupByMap.size());
            int totalPasstoFailTestsNum = 0;
            int totalFailtoPassTestsNum = 0;
            for(Map.Entry<String, List<MutatorReport>> entry : groupByMap.entrySet()){
                String mutator = entry.getKey();
                List<MutatorReport> valList = entry.getValue();
//                int allKillingTestsNum = 0;
                int killingTestsNum;
                int totalSucceedingTests = 0;
                int totalKillingTests = 0;
                int succeedingTestsNum;
                int killedCount;
                int row_totalKillingTests;
                for(MutatorReport report : valList){
                    if("KILLED".equals(report.status)){
                        killingTestsNum = report.killingTests.length;
//                        allKillingTestsNum += report.killingTests.length;
                        killedCount = getKilledCount(report.killingTests, failingTests);
                        row_totalKillingTests = killingTestsNum - killedCount;
                        totalKillingTests += row_totalKillingTests;
                    }else if("SURVIVED".equals(report.status)){
                        succeedingTestsNum = getSucceedingTestsNum(report.succeedingTests, failingTests);
                        totalSucceedingTests += succeedingTestsNum;
                    }
                }
//                totalKillingTests = allKillingTestsNum - failingTestsNum * killedCount;
                totalKillingTestsMap.put(mutator, new BigDecimal(totalKillingTests));
                totalPasstoFailTestsNum += totalKillingTests;

                totalSucceedingTestsMap.put(mutator, new BigDecimal(totalSucceedingTests));
                totalFailtoPassTestsNum += totalSucceedingTests;
            }
            Map<String, BigDecimal> museMap = getMUSE(totalSucceedingTestsMap, totalKillingTestsMap, new BigDecimal(failingTestsNum + ""), new BigDecimal(totalFailtoPassTestsNum + ""), new BigDecimal(totalPasstoFailTestsNum + ""));
            Map<String, List<MutatorReport>> lineNumberGroupByMap = reportList.stream().collect(Collectors.groupingBy((MutatorReport a) -> a.mutatedClass + "#" + a.lineNumber));
            Map<String, BigDecimal> suspLineNumberMap = getSuspLineNumber(museMap, lineNumberGroupByMap);

            outElementMuse(suspLineNumberMap, project, bugId);
            outTmpParams(totalKillingTestsMap, totalSucceedingTestsMap, totalPasstoFailTestsNum, totalFailtoPassTestsNum, project, bugId);
            outMutator(museMap, project, bugId);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private int getKilledCount(String[] killingTestsArray, String[] failingTestArray){
        int killedCount = 0;
        for(String killingTest : killingTestsArray){
            for(String failingTest : failingTestArray){
                if(failingTest.equals(killingTest)){
                    killedCount ++;
                }
            }
        }
        return killedCount;
    }

    private void outElementMuse(Map<String, BigDecimal> suspLineNumberMap, String project, String bugId) throws IOException {
        File file = new File(System.getProperty("user.home") + File.separator + "mutationReports" + File.separator + project + File.separator + bugId + File.separator + "MUSE计算的语句的可疑值.csv");
        FileUtils.writeStringToFile(file, "element,MUSE\r\n", "utf-8", false);
        StringBuilder result = new StringBuilder();
        for (Map.Entry<String, BigDecimal> entry : suspLineNumberMap.entrySet()){
            String lineNumber = entry.getKey();
            BigDecimal muse = entry.getValue();
            result.append(lineNumber).append(",").append(muse.toPlainString()).append("\r\n");
        }
        FileUtils.writeStringToFile(file, result.toString(), "utf-8", true);
    }

    private void outTmpParams(Map<String, BigDecimal> totalKillingTestsMap, Map<String, BigDecimal> totalSucceedingTestsMap,
                                     int totalPasstoFailTestsNum, int totalFailtoPassTestsNum,
                                     String project, String bugId) throws IOException {
        File file = new File(System.getProperty("user.home") + File.separator + "mutationReports" + File.separator + project + File.separator + bugId + File.separator + "为了后续计算的中间变量的值.csv");
        FileUtils.writeStringToFile(file, "mutator,totalKillingTests(t_p2f (m)),totalSucceedingTests(t_f2p (m))\r\n", "utf-8", false);
        StringBuilder result = new StringBuilder();
        for(Map.Entry<String, BigDecimal> entry : totalKillingTestsMap.entrySet()){
            String mutator = entry.getKey();
            BigDecimal totalKillingTests = entry.getValue();
            BigDecimal totalSuccedeedingTests = totalSucceedingTestsMap.get(mutator);
            result.append(mutator).append(",").append(totalKillingTests.toPlainString()).append(",").append(totalSuccedeedingTests.toPlainString()).append("\r\n");
        }
        FileUtils.writeStringToFile(file, result.toString(), "utf-8", true);
        FileUtils.writeStringToFile(file, "sum,totalPasstoFailTestsNum(T_p2f),totalFailtoPassTestsNum(T_f2p)\r\n", "utf-8", true);
        FileUtils.writeStringToFile(file, "," + totalPasstoFailTestsNum + "," + totalFailtoPassTestsNum, "utf-8", true);
    }

    private void outMutator(Map<String, BigDecimal> museMap, String project, String bugId) throws IOException {
        File file = new File(System.getProperty("user.home") + File.separator + "mutationReports" + File.separator + project + File.separator + bugId + File.separator + "变异体的可疑值.csv");
        FileUtils.writeStringToFile(file, "mutator,suspofMutator_MUSE\r\n", "utf-8", false);
        StringBuilder result = new StringBuilder();
        for(Map.Entry<String, BigDecimal> entry : museMap.entrySet()){
            String mutator = entry.getKey();
            BigDecimal muse = entry.getValue();
            result.append(mutator).append(",").append(muse.toPlainString()).append("\r\n");
        }
        FileUtils.writeStringToFile(file, result.toString(), "utf-8", true);
    }

    private int getSucceedingTestsNum(String[] succeedingTests, String[] failingTests){
        int succeedingTestsNum = 0;
        for(String succeedingTest : succeedingTests){
            for(String failingTest : failingTests){
                if(succeedingTest.contains(failingTest)){
                    succeedingTestsNum ++;
                }
            }
        }
        return succeedingTestsNum;
    }

    private Map<String, BigDecimal> getMUSE(Map<String, BigDecimal> totalSucceedingTestsMap, Map<String, BigDecimal> totalKillingTestsMap,
                                                   BigDecimal failingTestsNum, BigDecimal totalFailtoPassTestsNum,
                                                   BigDecimal totalPasstoFailTestsNum){
        Map<String, BigDecimal> museMap = new HashMap<>(totalSucceedingTestsMap.size());
        for(Map.Entry<String, BigDecimal> entry : totalSucceedingTestsMap.entrySet()){
            String mutator = entry.getKey();
            BigDecimal totalSucceedingTests = entry.getValue();
            BigDecimal totalKillingTests = totalKillingTestsMap.get(mutator);
            BigDecimal result = totalSucceedingTests.divide(failingTestsNum, 2, BigDecimal.ROUND_HALF_UP).subtract(totalFailtoPassTestsNum.divide(totalPasstoFailTestsNum, 2, BigDecimal.ROUND_HALF_UP).multiply(totalKillingTests.divide(failingTestsNum, 2, BigDecimal.ROUND_HALF_UP)));

            museMap.put(mutator, result);
        }
        return museMap;
    }

    private Map<String, BigDecimal> getSuspLineNumber(Map<String, BigDecimal> museMap, Map<String, List<MutatorReport>> groupByMap){
        Map<String, BigDecimal> resultMap = new HashMap<>(groupByMap.size());
        for(Map.Entry<String, List<MutatorReport>> entry : groupByMap.entrySet()){
            String lineNumber = entry.getKey();
            List<MutatorReport> list = entry.getValue();
            BigDecimal susp = new BigDecimal("0");
            for(MutatorReport report : list){
                susp = susp.add(museMap.get(report.mutator));
            }
            resultMap.put(lineNumber, susp.divide(new BigDecimal(list.size() + ""), 2, BigDecimal.ROUND_HALF_UP));
        }
        return resultMap;
    }

    public static class MutatorReport{
        private String line;
        private int lineNumber;
        private String mutatedClass;
        private String mutator;
        private String status;
        private String[] killingTests;
        private String[] succeedingTests;

        public MutatorReport(String line){
            try {
                String[] arr = line.split(",", -1);
                this.lineNumber = Integer.parseInt(arr[0]);
                this.mutatedClass = arr[1];
                this.mutator = arr[2];
                this.status = arr[3];
                this.killingTests = arr[4].split("\\|");
                this.succeedingTests = arr[5].split("\\|");
            }catch (Exception e){
                System.out.println("[ERROR] 初始化变异报告异常，行：" + line);
                e.printStackTrace();
                throw new RuntimeException(e.getMessage());
            }
        }

        public static List<MutatorReport> getMutatorReportList(List<String> csvList){
            List<MutatorReport> list = new ArrayList<>(csvList.size());
            csvList.forEach(line -> list.add(new MutatorReport(line)));
            return list;
        }

        public String getLine() {
            return line;
        }

        public int getLineNumber() {
            return lineNumber;
        }

        public String getMutatedClass() {
            return mutatedClass;
        }

        public String getMutator() {
            return mutator;
        }

        public String getStatus() {
            return status;
        }

        public String[] getKillingTests() {
            return killingTests;
        }

        public String[] getSucceedingTests() {
            return succeedingTests;
        }
    }

}
