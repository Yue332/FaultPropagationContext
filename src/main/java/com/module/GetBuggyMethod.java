package com.module;

import com.utils.Bean;
import com.utils.ConfigUtils;
import com.utils.Configer;
import mysoot.MyMain;
import org.apache.commons.io.FileUtils;
import soot.G;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GetBuggyMethod extends Bean implements IProcessModule {
    private String buggyLine;
    private String output;

    public GetBuggyMethod(Configer config) {
        super(config);
    }

    @Override
    public void process(Runtime runTime) throws Exception {
        File buggyLineFile = new File(buggyLine);
        if(!buggyLineFile.exists()){
            throw new Exception("buggyLine文件["+buggyLine+"]不存在！");
        }
        List<BuggyLineBean> beanList = getBuggyLineBeanList(FileUtils.readLines(buggyLineFile, "utf-8"));
        List<String> methodList = new ArrayList<>();
        StringBuilder info = new StringBuilder();
        for(BuggyLineBean bean : beanList){
            String clz = bean.getClz();
            String method = findBuggyLineMethod(info, bean);
            if(method == null){
                continue;
            }
            if(methodList.contains(method)){
                continue;
            }
            methodList.add(method);
        }
        File outputFile = new File(this.output);
        FileUtils.writeStringToFile(outputFile, "method\r\n", false);
        for(String method : methodList){
            FileUtils.writeStringToFile(outputFile, method + "\r\n", true);
        }
    }

    @Override
    public void onPrepare() {
        this.buggyLine = config.getConfig(ConfigUtils.PRO_PROJECT_PATH_KEY) + File.separator + "get_buggy_lines_" + super.projectId + File.separator + super.projectId + "-" + super.bugId + ".buggy.lines";
        this.output = System.getProperty("user.home") + File.separator + "BuggyMethod" + File.separator + projectId + "-" + bugId + ".buggy.methods";
    }

    private class BuggyLineBean{
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

    public List<BuggyLineBean> getBuggyLineBeanList(List<String> lineList){
        List<BuggyLineBean> list = new ArrayList<>(lineList.size());
        for(String line : lineList){
            BuggyLineBean bean = new BuggyLineBean(line);
            list.add(bean);
        }
        return list;
    }

    public String findBuggyLineMethod(StringBuilder info, BuggyLineBean bean) throws Exception {
        MyMain.setSootEnv(this.projectPath, this.projectId, bugId);
        String method = MyMain.analysis(info, bean.getClz(), bean.getLineNum());
        G.reset();
        return method;
    }
    
    public static void main(String[] args)throws Exception{
    	String projectPath = "C:\\Users\\44789\\Desktop\\Lang_8\\";
    	String projectId = "Lang";
    	String bugId = "8";
    	String buggyLine = "C:\\Users\\44789\\Desktop\\Lang-8.buggy.lines";
        File buggyLineFile = new File(buggyLine);
        if(!buggyLineFile.exists()){
            throw new Exception("buggyLine文件["+buggyLine+"]不存在！");
        }
        List<com.run.BuggyLineBean> beanList = new ArrayList<com.run.BuggyLineBean>();
        for(String line : FileUtils.readLines(buggyLineFile, "utf-8")) {
        	com.run.BuggyLineBean bean = new com.run.BuggyLineBean(line);
        	beanList.add(bean);
        }
        
        List<String> methodList = new ArrayList<>();
        StringBuilder info = new StringBuilder();
        for(com.run.BuggyLineBean bean : beanList){
            String clz = bean.getClz();
            MyMain.setSootEnv(projectPath, projectId, bugId);
            String method = MyMain.analysis(info, bean.getClz(), bean.getLineNum());
            G.reset();
            if(method == null){
                continue;
            }
            if(methodList.contains(method)){
                continue;
            }
            methodList.add(method);
        }
        System.out.println(info.toString());
//        File outputFile = new File(this.output);
//        FileUtils.writeStringToFile(outputFile, "class, method\r\n", false);
//        for(String method : methodList){
//            FileUtils.writeStringToFile(outputFile, method + "\r\n", true);
//        }
    }
}
