package com.module;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import com.utils.FileUtils;

import com.utils.Bean;
import com.utils.Configer;
import com.utils.Utils;
import com.utils.cal.IAnalysisFunc;
import com.utils.cal.TFuncRegister;


public class CalculateValueAndGenCSV extends Bean implements IProcessModule {
	protected String spectrumPath;
	protected String sprctraPath;
	
	public CalculateValueAndGenCSV(Configer config) {
		super(config);
	}

	@Override
	public void process(Runtime runTime) throws Exception {
		File path = new File(spectrumPath);
		if(!path.exists()) {
			throw new Exception("[ERROR] δ�ҵ���"+spectrumPath+"�����뽫gzoltar�����Ƶ�׼����(spectra�ļ���matrix)���Ƶ���Ŀ¼��");
		}
		File[] matrixList = path.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				if(name.startsWith("matrix")) {
					return true;
				}
				return false;
			}
		});
		File spectra = new File(sprctraPath);
		for(File matrix : matrixList) {
			String type = Utils.getType(matrix.getName());
			readDatasAndCalculate(spectra, matrix, type);
		}
	}
	

	
	public void readDatasAndCalculate(File spectra, File matrix, String type) throws Exception {
		System.out.println("��ʼ��ȡƵ�ס�����ļ�������["+projectId+"]bugID["+bugId+"]�Ļ���ֵ");
        List<String> codeLines = FileUtils.readLines(spectra, "UTF-8");
        List<String> testExecResults = FileUtils.readLines(matrix, "UTF-8");
        System.out.println("��ȡ���");
        System.out.println("����ȡ��Ϣд���ά����");
        //������Ϣд���ά����
        String[][] testExecResults2 = new String [testExecResults.size()][codeLines.size()+1];
        for (int i = 0; i < testExecResults.size(); i++) {
            String[] temps = testExecResults.get(i).split(" ");
            for (int j = 0; j < temps.length; j++) {
                testExecResults2[i][j] = temps[j];
            }
        }
//        n11:�������   + ͨ��
//        n10:�������   + ʧ��
//        n01:δ������� + ͨ��
//        n00:δ������� + ʧ��
	    double[][] testExecInfo = new double[codeLines.size()][4];
	    for (int i = 0; i < codeLines.size(); i++) {
	        double n11 = 0, n10 = 0, n01 = 0, n00 = 0;
	        for (int j = 0; j < testExecResults2.length; j++) {
	            double temp = Integer.parseInt(testExecResults2[j][i]);
	            String flag = testExecResults2[j][codeLines.size()];
	            if(1 == temp && flag.equals("+")){
	                n11 ++;
	            }else if(1 == temp && flag.equals("-")){
	                n10 ++;
	            }else if(0 == temp && flag.equals("+")){
	                n01 ++;
	            }else if(0 == temp && flag.equals("-")){
	                n00 ++;
	            }
	        }
	        testExecInfo[i][0]=n11;
	        testExecInfo[i][1]=n10;
	        testExecInfo[i][2]=n01;
	        testExecInfo[i][3]=n00;
	    }
	    System.out.println("[INFO] д����ɣ���ʼ���㻳��ֵ");
	    calculateValue(codeLines, testExecInfo, type);
	}
	
	//���㻳��ֵ������csv�ļ�
	public void calculateValue(List<String> codeLines, double[][] ints, String type) throws Exception {
		ArrayList<List<BigDecimal>> suspValues = new ArrayList<List<BigDecimal>>();
		LinkedHashMap<String, IAnalysisFunc> funcMap = TFuncRegister.getRegistClass(config);
		
        String[] titleLine = new String[funcMap.size() + 1];
        titleLine[0] = "element";
        
        int idx = 1;
        Iterator<Entry<String, IAnalysisFunc>> entries = funcMap.entrySet().iterator();
        StringBuilder funcBuilder = new StringBuilder();
        while (entries.hasNext()) {
			Entry<String, IAnalysisFunc> entry = entries.next();
			String clzName = entry.getKey();
			IAnalysisFunc func = entry.getValue();
			
			titleLine[idx] = clzName;
			funcBuilder.append(clzName).append(",");
			idx ++;
			try {
				suspValues.add(func.onProcess(ints));
			}catch (Exception e) {
				throw new Exception("ִ�з���["+clzName+"]�쳣��" + e.getMessage());
			}
		}
        System.out.println("[INFO] ʹ�ù�ʽ��" + funcBuilder.substring(0, funcBuilder.length() - 1).toString());
        System.out.println("[INFO] ��ʼд��CSV�ļ�");
        
        String csvName = spectrumPath + File.separator + super.projectId + "-" + super.bugId + "-" + "suspValue" + ("".equals(type) ? "" : "_" + type) + ".csv";
        File dest = new File(csvName);
        FileUtils.writeStringToFile(dest, Arrays.toString(titleLine).replace("[", "").replace("]", "") + "\r\n", false);
        for (int i = 0; i < codeLines.size(); i++) {
            for (int j = 0; j < suspValues.size(); j++) {
                BigDecimal result = suspValues.get(j).get(i);
                if(j == 0){
                	String splitFlag = suspValues.size() == 1 ? "\r\n" : ",";
                    FileUtils.writeStringToFile(dest, codeLines.get(i) + ",", true);
                    FileUtils.writeStringToFile(dest, result.toPlainString() + splitFlag, true);
                }else if (j == (suspValues.size()-1)){
                    FileUtils.writeStringToFile(dest, result.toPlainString() + "\r\n", true);
                }else{
                    FileUtils.writeStringToFile(dest, result.toPlainString() + ",", true);
                }
            }
        }
        System.out.println("[INFO] CSV�ļ�������ɣ�");
	}

	@Override
	public void onPrepare() {
		spectrumPath = super.projectPath + File.separator + "gzoltar_output" + File.separator + super.projectId + File.separator + super.bugId;
		sprctraPath = spectrumPath + File.separator + "spectra";
	}
}
