package com.utils.cal.topn;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;

import java.io.File;
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

    public FuncSuspValue(File suspValueFile)throws Exception{
        this.suspValueFile = suspValueFile;
        List<String> list = FileUtils.readLines(suspValueFile, "utf-8");
        if(CollectionUtils.isEmpty(list)){
            throw new RuntimeException("[ERROR] 文件" + suspValueFile.getAbsolutePath() + "内容为空！");
        }
        this.funcArray = list.get(0).split(",");
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

    public List<ElementFuncSuspValue> getSuspValueList(){
        return this.suspValueList;
    }

    /**
     * 按当前公式得分倒序排序
     */
    public void sortReversed(){
        suspValueList.sort(Comparator.comparingDouble(FuncSuspValue.ElementFuncSuspValue::getCurrentFuncScoreDouble).reversed());
    }

    /**
     * TODO:按指定公式排序，待实现
     * @param func func
     */
    public void sortReversed(String func){

    }

    public class ElementFuncSuspValue{
        private String element;
        private Map<String, BigDecimal> suspValue;

        public ElementFuncSuspValue(String line, String[] funcArr){
            List<String> lineSplit = Arrays.asList(line.split(","));
            this.element = lineSplit.get(0);
            suspValue = new HashMap<>(lineSplit.size() - 1);
            for(int i = 1, length = lineSplit.size(); i < length; i ++){
                suspValue.put(funcArr[i], new BigDecimal(lineSplit.get(i)));
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
