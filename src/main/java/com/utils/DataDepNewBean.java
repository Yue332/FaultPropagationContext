package com.utils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zzy
 * @date 2021-04-04 19:44
 */
public class DataDepNewBean {

    private String line;
    private String clz;
    private List<String> lineNumbers;
    private BigDecimal score;

    public DataDepNewBean(String line){
        this.line = line;
        String[] tmpArr = line.split(",");
        String[] elementArr = tmpArr[0].split("#");
        this.clz = elementArr[0];
        String firstLieNumber = elementArr[1];
        this.lineNumbers = new ArrayList<>();
        this.lineNumbers.add(firstLieNumber);
        if(tmpArr.length == 3){
            String[] lineNumberTmpArr = tmpArr[1].split("/");
            for(String linenumber : lineNumberTmpArr){
                if(linenumber.equals(firstLieNumber)){
                    continue;
                }
                if(linenumber.equals("-1")) {
                	continue;
                }
                this.lineNumbers.add(linenumber);
            }
            this.score = new BigDecimal(tmpArr[2]);
        }else{
            this.score = new BigDecimal(tmpArr[1]);
        }

    }

    public List<String> getElements(){
        List<String> elements = new ArrayList<>(this.lineNumbers.size());
        for(String lineNumber : this.lineNumbers){
            elements.add(this.clz + "#" + lineNumber);
        }
        return elements;
    }

    public List<String> getLineNumbers(){
        return this.lineNumbers;
    }

    public String getLine() {
        return line;
    }

    public String getClz() {
        return clz;
    }

    public BigDecimal getScore() {
        return score;
    }

    public static List<DataDepNewBean> getList(List<String> lines){
        List<DataDepNewBean> list = new ArrayList<>(lines.size());
        for(String line : lines){
            DataDepNewBean bean = new DataDepNewBean(line);
            list.add(bean);
        }
        return list;
    }

    public static List<String> getAllElements(List<DataDepNewBean> beanList){
        List<String> list = new ArrayList<>();
        for(DataDepNewBean bean : beanList){
            List<String> elements = bean.getElements();
            list.addAll(elements);
        }
        return list;
    }
}
