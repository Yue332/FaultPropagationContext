package com.utils.cal.topn;

import com.utils.Utils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

/**
 * @program: SBTandem
 * @author: zhangziyi
 * @date: 2021/11/10
 * @description:
 **/
public class FuncSuspValue {
    private String[] funcArray;
    private List<ElementFuncSuspValue> suspValueList;
    private String currentFunc;
    private File suspValueFile;
    private String bug;

    public FuncSuspValue(String project, String bug)throws Exception{
        File suspValueFile = new File(System.getProperty("user.home") + File.separator +
                "mutationReports" + File.separator + project + File.separator +
                project + "-" + bug + "-MetallaxisSuspValue.csv");
        this.bug = bug;
        init(suspValueFile);

    }

    public FuncSuspValue(File suspValueFile)throws Exception{
        this.bug = suspValueFile.getName().split("-")[1];
        init(suspValueFile);
    }

    public void init(File suspValueFile) throws Exception {
        System.out.println("[INFO] 开始读取文件" + suspValueFile.getAbsolutePath());
        this.suspValueFile = suspValueFile;
        List<String> list;
        try {
            list = FileUtils.readLines(suspValueFile, "utf-8");
        }catch (IOException e){
            throw new SuspValueNotFoundException(e.getMessage());
        }
        //只有表头的情况
        if(list.size() == 1){
            throw new SuspValueNotFoundException("");
        }
        if(CollectionUtils.isEmpty(list)){
            throw new RuntimeException("[ERROR] 文件" + suspValueFile.getAbsolutePath() + "内容为空！");
        }
//        this.funcArray = list.get(0).split(",");
        this.funcArray = Utils.deleteArrayElements(list.get(0).split(","), "element");
        list.remove(0);
        this.suspValueList = new ArrayList<>(list.size() - 1);
        list.forEach(row -> suspValueList.add(new ElementFuncSuspValue(row, funcArray)));
    }


    public void setCurrentFunc(String func){
        this.currentFunc = func;
    }

    public String[] getFuncArray(){
        return this.funcArray;
    }

    public String getBug() {
        return bug;
    }

    public List<ElementFuncSuspValue> getSuspValueList(){
        return this.suspValueList;
    }

    public List<String> getAllElements(){
        if(this.suspValueList == null){
            throw new RuntimeException("[ERROR] 未初始怀疑度列表，请检查调用方式！");
        }
        List<String> allElements = new ArrayList<>(this.suspValueList.size());
        this.suspValueList.forEach(m -> allElements.add(m.getElement()));

        return allElements;
    }

    /**
     * 按当前公式得分倒序排序
     */
    public void sortReversed(){
        suspValueList.sort(Comparator.comparingDouble(FuncSuspValue.ElementFuncSuspValue::getCurrentFuncScoreDouble).reversed());
    }

    /**
     * 按指定公式排序
     * @param func func
     */
    public void sortReversed(String func){
        this.setCurrentFunc(func);
        sortReversed();
    }

    public class ElementFuncSuspValue{
        private String element;
        private Map<String, BigDecimal> suspValue;

        public ElementFuncSuspValue(String line, String[] funcArr){
            List<String> lineSplit = Arrays.asList(line.split(","));
            this.element = lineSplit.get(0);
            suspValue = new HashMap<>(lineSplit.size() - 1);
            for(int i = 1, length = lineSplit.size(); i < length; i ++){
                suspValue.put(funcArr[i - 1], new BigDecimal(lineSplit.get(i)));
            }
        }

        public String getElement(){
            return this.element;
        }

        public BigDecimal getFuncScore(String func){
            return this.suspValue.get(func);
        }

        public double getFuncScoreDouble(String func){
            return getFuncScore(func).doubleValue();
        }

        public BigDecimal getCurrentFuncScore(){
            BigDecimal ret = this.suspValue.get(currentFunc);
            if(ret == null){
                throw new RuntimeException("[ERROR] 文件" + suspValueFile.getAbsolutePath() + "中不存在公式" + currentFunc);
            }
            return ret;
        }
        
        public double getCurrentFuncScoreDouble(){
            return getCurrentFuncScore().doubleValue();
        }
    }
}
