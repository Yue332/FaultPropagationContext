package com.finalmodule;

import com.finalmodule.base.FinalBean;
import com.finalmodule.base.IFinalProcessModule;
import com.utils.cal.IAnalysisFunc;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileFilter;
import java.math.BigDecimal;
import java.util.List;

public class FinalMFR extends FinalBean implements IFinalProcessModule {
    @Override
    public void process(Runtime runTime) throws Exception {
        File outputFile = getOutputFile();
        for(File project : getMFRProjectPath()){
        	System.out.println("[DEBUG] ��ʼ����" + project.getName());
            File[] mfrFiles = getMFRFiles(project);
            for(File mfrFile : mfrFiles){
                String[] ret = getAvageAR(project.getName(), mfrFile);
                FileUtils.writeStringToFile(outputFile, project + "," + ret[0] + "," + ret[1] + "\r\n", true);
            }
        }
    }

    private File getOutputFile()throws Exception{
        String path = System.getProperty("user.home") + File.separator + "MFR" + File.separator + "finalMFR.csv";
        File file = new File(path);
        FileUtils.writeStringToFile(file, "func,project,avageAR\r\n", false);
        return file;
    }

    private File[] getMFRProjectPath()throws Exception{
        String path = System.getProperty("user.home") + File.separator + "MFR" + File.separator;
        File f = new File(path);
        if(!f.exists()){
            throw new Exception("Ŀ¼["+path+"]�����ڣ�����ʹ��MFR����");
        }
        File[] projects = f.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.isDirectory();
			}
		});
        if(projects == null || projects.length == 0){
            throw new Exception("Ŀ¼["+path+"]��û���ҵ���Ŀ��Ŀ¼�����飡");
        }
        return projects;
    }

    private File[] getMFRFiles(File mfrProjectPath)throws Exception{
//    	System.out.println(mfrProjectPath.exists());
        File[] mfrFiles = mfrProjectPath.listFiles(new FileFilter() {
			
			@Override
			public boolean accept(File pathname) {
				return pathname.isFile() && pathname.getName().startsWith("MFR-") && pathname.getName().endsWith(".csv");
			}
		});
        if(mfrFiles == null || mfrFiles.length == 0){
            throw new Exception("Ŀ¼["+mfrProjectPath.getAbsolutePath()+"]��û���ҵ�MFR����ļ������飡");
        }
        return mfrFiles;
    }

    /**
     * @param mfrFile
     * @return ret[0]-func ret[1]-avageAR
     * @throws Exception
     */
    private String[] getAvageAR(String project, File mfrFile)throws Exception{
        System.out.println("[INFO] ��ʼ������Ŀ["+project+"]["+mfrFile.getName()+"]��ƽ��ARֵ");
        String[] ret = new String[2];
        ret[0] = mfrFile.getName().replace("MFR-", "").replace(".csv", "");
        List<String> list = FileUtils.readLines(mfrFile, "utf-8");
        list.remove(0);
        if(list.size() == 0){
            throw new Exception("�ļ�["+mfrFile.getAbsolutePath()+"]Ϊ�գ����飡");
        }
        BigDecimal sumAR = new BigDecimal("0");
        for(String line : list){
            sumAR = sumAR.add(new BigDecimal(line.split(",")[1]));
        }
        System.out.println("[INFO] ["+mfrFile.getName()+"]��ARֵ�ܺ�Ϊ��" + sumAR.toPlainString());
        ret[1] = sumAR.divide(new BigDecimal(String.valueOf(list.size())), IAnalysisFunc.scale, IAnalysisFunc.roundingMode).toPlainString();
        return ret;
    }
}
