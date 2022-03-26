package com.finalmodule;

import com.finalmodule.base.FinalBean;
import com.finalmodule.base.IFinalProcessModule;
import com.sun.xml.bind.v2.runtime.output.DOMOutput;
import com.utils.ConfigUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class IntelliFL1 extends FinalBean implements IFinalProcessModule {
    @Override
    public void process(Runtime runTime, StringBuilder processLog) throws Exception {
        String[] bugArray = config.getBugIdArr();
        for (String bug : bugArray) {
            dealOneBug(bug);
        }
    }

    public void dealOneBug(String bug)throws Exception{
        //failing_tests 文件路径
        File failingTests = new File(super.config.getConfig(ConfigUtils.PRO_PROJECT_PATH_KEY) + File.separator +
                config.getConfig(ConfigUtils.PRO_PROJECT_ID_KEY) + "_" + bug + File.separator + "failing_tests");
        //failing_tests中 取开头为 --- 的数据 class.method 失败的测试用例组成map key=class,value=method
        Map<String, List<String>> failingTestMap = getFailingTest(failingTests);

        // intelliFL生成的txt文件目录
        File failIntellFLPath = new File(System.getProperty("user.home") + File.separator +
                "MBFL" + File.separator + config.getConfig(ConfigUtils.PRO_PROJECT_ID_KEY) + File.separator +
                config.getConfig(ConfigUtils.PRO_PROJECT_ID_KEY) + "-" + bug + File.separator +
                "inteliFL" + File.separator);
        //先把成功的测试用例和失败的区分开
        //key=文件名,value=文件内容（去头）
        Map<String, List<String>> successMap = new HashMap<>();
        Map<String, List<String>> failMap = new HashMap<>();
        File[] fileArray = failIntellFLPath.listFiles(file -> file.getName().endsWith(".txt"));
        if(fileArray == null || fileArray.length == 0){
            throw new Exception("目录" + failIntellFLPath.getAbsolutePath() + "下无txt文件！");
        }
        for(File file : fileArray){
            List<String> list = FileUtils.readLines(file, "utf-8");
            String head = list.get(0);
            list.remove(0);//去头
            if(head.endsWith("true")){//成功的测试用例
                successMap.put(file.getName(), list);
            }else if(head.endsWith("false")){//失败的测试用例
                failMap.put(file.getName(), list);
            }else{
                System.out.println("[ERROR] 文件" + file.getName() + "无法判断是否是成功的测试用例，文件第一行为：" + head);
            }
        }

        //需要移除的成功的测试用例（调用方法中不包含失败测试用例调用的方法的测试用例）
        List<String> removeList = new ArrayList<>();

        for(Map.Entry<String, List<String>> entry : failingTestMap.entrySet()){
            String clzName = entry.getKey();
            List<String> methodList = entry.getValue();
            for (String method : methodList) {
                //失败测试用例调用的方法
                List<String> failMethodList = failMap.get(clzName + "." + method + ".txt");
                if(CollectionUtils.isEmpty(failMethodList)){
                    throw new Exception(clzName + "." + method + ".txt" + "不在失败的测试用例中，或该文件为空！");
                }
                for(Map.Entry<String, List<String>> entry1 : successMap.entrySet()){
                    String fileName = entry1.getKey();
                    List<String> successMethodList = entry1.getValue();
                    if(successMethodList.stream().anyMatch(failMethodList::contains)){
                        removeList.add(fileName);
                    }
                }
            }
        }
        if(CollectionUtils.isNotEmpty(removeList)){
            //去重
            removeList = removeList.stream().distinct().collect(Collectors.toList());
        }
        //输出的文件，路径
        File outputFile = new File(System.getProperty("user.home") + File.separator +
                "intelliFL" + File.separator + "methods" + File.separator +
                config.getConfig(ConfigUtils.PRO_PROJECT_ID_KEY) + "_" + bug + File.separator +
                "removeMethods.txt");
        StringBuilder str = new StringBuilder();
        removeList.forEach(row -> str.append(row).append("\r\n"));
        FileUtils.writeStringToFile(outputFile, str.toString(), "utf-8", false);
    }

    public Map<String, List<String>> getFailingTest(File failingTests)throws Exception{
        List<String> failingTestsList = FileUtils.readLines(failingTests, "utf-8").stream().filter(row -> row.startsWith("---")).collect(Collectors.toList());
        Map<String, List<String>> retMap = new HashMap<>(failingTestsList.size());
        failingTestsList.forEach(row -> {
            String[] array = row.replace("--- ", "").split("::");
            List<String> methodList = CollectionUtils.isEmpty(retMap.get(array[0])) ? new ArrayList<>() : retMap.get(array[0]);
            methodList.add(array[1]);
            retMap.put(array[0], methodList);
        });
        return retMap;
    }
}
