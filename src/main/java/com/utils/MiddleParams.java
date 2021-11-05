package com.utils;

/**
 * @program: SBTandem
 * @author: zhangziyi
 * @date: 2021/11/4
 * @description:
 **/
public class MiddleParams {
    private String line;
    private String mutator;
    private int totalKillingTests;
    private int totalSucceedingTests;

    public MiddleParams(String line){
        this.line = line;
        String[] tmp = line.split(",");
        try {
            this.mutator = tmp[0];
            this.totalKillingTests = Integer.parseInt(tmp[1]);
            this.totalSucceedingTests = Integer.parseInt(tmp[2]);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public String getLine() {
        return line;
    }

    public String getMutator() {
        return mutator;
    }

    public int getTotalKillingTests() {
        return totalKillingTests;
    }

    public int getTotalSucceedingTests() {
        return totalSucceedingTests;
    }
}
