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
            int count = 0;  //�鿴����������ǰ,�ж������
            int label = 0;  //�ж��Ƿ��Ǵ������
            double fault_susValue = 0;  //����Ǵ������,��ȡ�仳��ֵ
            int sameSusValue_count = 0; //��ͬ����ֵ,�ж������
            int sameSusValueAndLabel_count =0;  //��ͬ����ֵ�ͱ�ǩ,�ж������
            double best = 0.0 ,worse = 0.0;     //���ź����
            boolean flag = true;    //�ж��Ƿ��һ������

            for (int i = 0; i < data.size(); i++) {
                label = Integer.parseInt(StringUtils.substringAfterLast(data.get(i),","));
                if(label != 1){ //�������
                    count++;
                }
                if(label == 1 && flag == true){
                    flag = false;
                    fault_susValue = Double.parseDouble(data.get(i).split("\\,")[col]);
                    //fault_susValue = Double.parseDouble(StringUtils.substringBefore(data.get(i),","));
                    for (int j = i; j < data.size(); j++) {
                        double temp = Double.parseDouble(data.get(j).split("\\,")[col]);
                        //double temp = Double.parseDouble(StringUtils.substringBefore(data.get(j),","));
                        if(temp == fault_susValue){ //����ֵ��ͬ
                            sameSusValue_count ++;
                            int tempLabel =  Integer.parseInt(StringUtils.substringAfterLast(data.get(j),","));
                            if(tempLabel == 1){
                                sameSusValueAndLabel_count++;
                            }
                        }
                    }
                    break; // �����һ���������λ��,����
                }
            }
            best = (float)count / data.size();
            worse = (float)(count + sameSusValue_count - sameSusValueAndLabel_count)/data.size();
            score[0] = best;
            score[1] = worse;
            */
            int count = 0;  //�鿴����������ǰ,�ж������
            int label = 0;  //�ж��Ƿ��Ǵ������
            for (int i = 0; i < data.size(); i++) {
                label = Integer.parseInt(StringUtils.substringAfterLast(data.get(i),","));
                if(label != 1) count++; //�������
                if(label == 1) break;
            }
            score = (float)count / data.size();
        }

        return score;
    }
}
