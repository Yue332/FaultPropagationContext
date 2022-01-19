package com.module;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * @author WangChen
 * @create 2020-09-03 15:21
 */
public class metricUtils {

    static double DataMetric(List<String> data,int col,String metric) {
    // static double[] DataMetric(List<String> data,int col,String metric) {
        // double[] score=new double[2];
        double score = 0.0;

        if (metric.equals("EXAM")){
            /*
            int count = 0;  //查看到达错误语句前,有多少语句
            int label = 0;  //判断是否是错误语句
            double fault_susValue = 0;  //如果是错误语句,获取其怀疑值
            int sameSusValue_count = 0; //相同怀疑值,有多少语句
            int sameSusValueAndLabel_count =0;  //相同怀疑值和标签,有多少语句
            double best = 0.0 ,worse = 0.0;     //最优和最差
            boolean flag = true;    //判断是否第一次遇到

            for (int i = 0; i < data.size(); i++) {
                label = Integer.parseInt(StringUtils.substringAfterLast(data.get(i),","));
                if(label != 1){ //错误语句
                    count++;
                }
                if(label == 1 && flag == true){
                    flag = false;
                    fault_susValue = Double.parseDouble(data.get(i).split("\\,")[col]);
                    //fault_susValue = Double.parseDouble(StringUtils.substringBefore(data.get(i),","));
                    for (int j = i; j < data.size(); j++) {
                        double temp = Double.parseDouble(data.get(j).split("\\,")[col]);
                        //double temp = Double.parseDouble(StringUtils.substringBefore(data.get(j),","));
                        if(temp == fault_susValue){ //怀疑值相同
                            sameSusValue_count ++;
                            int tempLabel =  Integer.parseInt(StringUtils.substringAfterLast(data.get(j),","));
                            if(tempLabel == 1){
                                sameSusValueAndLabel_count++;
                            }
                        }
                    }
                    break; // 到达第一个错误语句位置,跳出
                }
            }
            best = (float)count / data.size();
            worse = (float)(count + sameSusValue_count - sameSusValueAndLabel_count)/data.size();
            score[0] = best;
            score[1] = worse;
            */
            int count = 0;  //查看到达错误语句前,有多少语句
            int label = 0;  //判断是否是错误语句
            for (int i = 0; i < data.size(); i++) {
                label = Integer.parseInt(StringUtils.substringAfterLast(data.get(i),","));
                if(label != 1) count++; //错误语句
                if(label == 1) break;
            }
            score = (float)count / data.size();
        }

        return score;
    }
}
