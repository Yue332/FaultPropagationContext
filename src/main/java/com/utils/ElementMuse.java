package com.utils;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @program: SB-Tandem
 * @author: zhangziyi
 * @date: 2021/10/28
 * @description:
 **/
public class ElementMuse{
    private String line;
    private String element;
    private double muse;

    public ElementMuse(String line){
        try{
            String[] tmp = line.split(",");
            this.element = tmp[0];
            this.muse = Double.parseDouble(tmp[1]);
        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException("[ERROR] �У�" + line + "�쳣���޷�ת��Ϊ����");
        }
    }

    public static List<ElementMuse> buildList(String project, String bug) throws Exception {
        File museCsvFile = new File(System.getProperty("user.home") + File.separator + "mutationReports" + File.separator + project + File.separator + bug + File.separator + "MUSE��������Ŀ���ֵ.csv");
        List<String> museCsvList = FileUtils.readLines(museCsvFile, "utf-8");
        museCsvList.remove(0);
        List<ElementMuse> retList = new ArrayList<>(museCsvList.size());
        museCsvList.forEach(str -> retList.add(new ElementMuse(str)));
        return retList;
    }

    public String getElement() {
        return element;
    }

    public double getMuse() {
        return muse;
    }
}
