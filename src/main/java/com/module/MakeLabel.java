package com.module;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.utils.FileUtils;
import org.apache.commons.lang3.StringUtils;

import com.utils.Bean;
import com.utils.ConfigUtils;
import com.utils.Configer;

public class MakeLabel extends Bean implements IProcessModule {
	private String sprctraPath;
	private String buggyLinePath;
	private String outputPath;
	public MakeLabel(Configer config) {
		super(config);
	}

	@Override
	public void process(Runtime runTime) throws Exception {
		File sprctra = new File(sprctraPath);
		if(!sprctra.exists()) {
			throw new Exception("[ERROR] 未找到【"+sprctra.getParent()+"】，请将gzoltar输出的频谱及语句(spectra文件和matrix)复制到该目录下");
		}
		File buggyLines = new File(this.buggyLinePath);
		if(!buggyLines.exists()) {
			throw new Exception("[ERROR] 未找到【"+buggyLines.getParent()+"】，请将get_buggy_line输出的文件(projectId-bugId.buggy.lines)复制到该目录下");
		}
        List<String> spectra = FileUtils.readLines(sprctra,"UTF-8");
        List<String> buggyLine = FileUtils.readLines(buggyLines,"UTF-8");
        List<String> newBuggyLine = new ArrayList();
        for (int i = 0; i < buggyLine.size(); i++) {
        	String temp1 = StringUtils.replace(StringUtils.substringBefore(buggyLine.get(i),"."),"/",".");
        	String temp2 = StringUtils.substringBefore(StringUtils.substringAfter(buggyLine.get(i),"#"),"#");
        	newBuggyLine.add(temp1+"#"+temp2);
        }
        System.out.println("bugId" + super.bugId + "错误代码行：" + newBuggyLine.toString());
        spectra = compare(spectra, newBuggyLine);
        writeToFile(spectra);
	}

	@Override
	public void onPrepare() {
		this.sprctraPath = super.projectPath + File.separator + "gzoltar_output" + File.separator + super.projectId + File.separator + super.bugId + File.separator + "spectra";
		this.buggyLinePath = config.getConfig(ConfigUtils.PRO_PROJECT_PATH_KEY) + File.separator + "get_buggy_lines_" + super.projectId + File.separator + super.projectId + "-" + super.bugId + ".buggy.lines";
		this.outputPath = projectPath + File.separator + "label";
	}

    //比较并添加标签
    public List<String> compare(List<String> spectra,List<String> newBuggyLine){
        System.out.println("[INFO] 匹配代码行：");
        for(int i = 0; i < spectra.size(); i++){
            for(int j = 0; j < newBuggyLine.size(); j++){
                //判断字符串是否匹配,匹配标记1
                if(spectra.get(i).equals(newBuggyLine.get(j))){
                    System.out.println(spectra.get(i));
                    spectra.set(i,spectra.get(i)+",1");
                    break;
                }
                //如果循环到 newBuggyLine的最后一个还未匹配,标记0
                if(j == (newBuggyLine.size()-1)){
                    spectra.set(i,spectra.get(i)+",0");
                }
            }
        }
        return spectra;
    }

    //写出到CSV文件
    public void writeToFile(List<String> spectra) throws Exception {
    	File path = new File(this.outputPath);
    	if(!path.exists()) {
    		path.mkdirs();
    	}
        File dest = new File(path.getAbsolutePath() + File.separator + projectId + "-" + bugId + "-label.csv");
        FileUtils.writeStringToFile(dest, "", false);
        for (int i = 0; i < spectra.size(); i++) {
            FileUtils.writeStringToFile(dest, spectra.get(i) + "\n",true);
        }
    }
}
