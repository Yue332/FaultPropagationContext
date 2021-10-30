package com.run;

public class BuggyLineBean {
    private String clz;
    private int lineNum;

    public BuggyLineBean(String line) {
        String[] tmp = line.split("#");
        this.lineNum = Integer.parseInt(tmp[1]);
        this.clz = tmp[0].replace(".java", "").replace("/", ".");
    }

    public String getClz() {
        return clz;
    }

    public int getLineNum() {
        return lineNum;
    }
}
