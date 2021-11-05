package com.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @program: SBTandem
 * @author: zhangziyi
 * @date: 2021/11/4
 * @description:
 **/
public class AllMiddleParams {
    private List<MiddleParams> middleParams;
    private int totalPasstoFailTestsNum;
    private int totalFailtoPassTestsNum;

    public AllMiddleParams(String filePath) throws Exception {
        System.out.println("[DEBUG] ¶ÁÈ¡ÎÄ¼þ" + filePath);
        File file = new File(filePath);
        List<String> list = FileUtils.readLines(file, "utf-8");
        list.remove(0);
        String lastLine = list.get(list.size() - 1);
        String[] tmp = lastLine.split(",");
        this.totalPasstoFailTestsNum = Integer.parseInt(tmp[1]);
        this.totalFailtoPassTestsNum = Integer.parseInt(tmp[2]);
        list.remove(list.size() - 1);
        list.remove(list.size() - 1);

        this.middleParams = new ArrayList<>(list.size());
        list.forEach(line -> middleParams.add(new MiddleParams(line)));
    }

    public List<MiddleParams> getMiddleParams() {
        return middleParams;
    }

    public int getTotalPasstoFailTestsNum() {
        return totalPasstoFailTestsNum;
    }

    public int getTotalFailtoPassTestsNum() {
        return totalFailtoPassTestsNum;
    }
}
