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
			throw new Exception("[ERROR] δ�ҵ���"+sprctra.getParent()+"�����뽫gzoltar�����Ƶ�׼����(spectra�ļ���matrix)���Ƶ���Ŀ¼��");
		}
		File buggyLines = new File(this.buggyLinePath);
		if(!buggyLines.exists()) {
			throw new Exception("[ERROR] δ�ҵ���"+buggyLines.getParent()+"�����뽫get_buggy_line������ļ�(projectId-bugId.buggy.lines)���Ƶ���Ŀ¼��");
		}
        List<String> spectra = FileUtils.readLines(sprctra,"UTF-8");
        List<String> buggyLine = FileUtils.readLines(buggyLines,"UTF-8");
        List<String> newBuggyLine = new ArrayList();
        for (int i = 0; i < buggyLine.size(); i++) {
        	String temp1 = StringUtils.replace(StringUtils.substringBefore(buggyLine.get(i),"."),"/",".");
        	String temp2 = StringUtils.substringBefore(StringUtils.substringAfter(buggyLine.get(i),"#"),"#");
        	newBuggyLine.add(temp1+"#"+temp2);
        }
        System.out.println("bugId" + super.bugId + "��������У�" + newBuggyLine.toString());
        spectra = compare(spectra, newBuggyLine);
        writeToFile(spectra);
	}

	@Override
	public void onPrepare() {
		this.sprctraPath = super.projectPath + File.separator + "gzoltar_output" + File.separator + super.projectId + File.separator + super.bugId + File.separator + "spectra";
		this.buggyLinePath = config.getConfig(ConfigUtils.PRO_PROJECT_PATH_KEY) + File.separator + "get_buggy_lines_" + super.projectId + File.separator + super.projectId + "-" + super.bugId + ".buggy.lines";
		this.outputPath = projectPath + File.separator + "label";
	}

    //�Ƚϲ���ӱ�ǩ
    public List<String> compare(List<String> spectra,List<String> newBuggyLine){
        System.out.println("[INFO] ƥ������У�");
        for(int i = 0; i < spectra.size(); i++){
            for(int j = 0; j < newBuggyLine.size(); j++){
                //�ж��ַ����Ƿ�ƥ��,ƥ����1
                if(spectra.get(i).equals(newBuggyLine.get(j))){
                    System.out.println(spectra.get(i));
                    spectra.set(i,spectra.get(i)+",1");
                    break;
                }
                //���ѭ���� newBuggyLine�����һ����δƥ��,���0
                if(j == (newBuggyLine.size()-1)){
                    spectra.set(i,spectra.get(i)+",0");
                }
            }
        }
        return spectra;
    }

    //д����CSV�ļ�
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
