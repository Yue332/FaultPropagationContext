package com.module;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import com.utils.Bean;
import com.utils.ConfigUtils;
import com.utils.Configer;
import com.utils.Utils;

public class LithiumSlicer extends Bean implements IProcessModule{
	public static String LITHIUM_COMMAND1 = "cd @:LITHIUM_SLICER_HOME@/app/";
	public static String LITHIUM_COMMAND2 = "./runner.sh @:PROJECT_ID@ @:BUG_ID@ @:TOP@";
	
	private String top;
	
	public void executeLithiumSlicer(Runtime runTime)throws Exception{
		String[] msg = Utils.executeCommandLine(runTime, LITHIUM_COMMAND1
				.replaceAll("@:LITHIUM_SLICER_HOME@", config.getConfig(ConfigUtils.PRO_LITHIUM_SLICER_HOME_KEY)), 
				LITHIUM_COMMAND2.replaceAll("@:PROJECT_ID@", projectId)
				.replaceAll("@:BUG_ID@", bugId)
				.replaceAll("@:TOP@", this.top));
		if(!"0".equals(msg[0])) {
			throw new Exception("[ERROR] execute lithium-slicer runner.sh fail. ");
		}
	}
	

	
	public List<File> getLithiumFileList(File logFilePath){
		List<File> list = new ArrayList<File>();
		getLithiumFile(logFilePath, list);
		return list;
	}
	
	public LithiumSlicer(Configer config) {
		super(config);
		this.top = config.getConfig(ConfigUtils.PRO_TOP_KEY);
	}

	private void getLithiumFile(File file, List<File> list) {
		if(file.isDirectory()) {
			for(File f : file.listFiles()) {
				getLithiumFile(f, list);
			}
		}
		String fileName = file.getName();
		if(fileName.startsWith("lithium_") && fileName.endsWith(".java")) {
			list.add(file);
		}
	}
	

	@Override
	public void process(Runtime runTime) throws Exception {
		executeLithiumSlicer(runTime);
		File lithiumOuput = new File(super.projectPath + File.separator + "lithium_output");
		if(!lithiumOuput.exists()) {
			lithiumOuput.mkdirs();
		}
		File srcFile = new File(config.getConfig(ConfigUtils.PRO_LITHIUM_SLICER_HOME_KEY) + File.separator + "app" + File.separator + "logs_" + top + File.separator + super.projectId + "_" + super.bugId + File.separator + super.bugId);
		copy(srcFile.getAbsolutePath(), lithiumOuput.getAbsolutePath());
	}
	
	private static void copy(String f1, String f2) throws IOException {  
       File file1 = new File(f1);  
       File[] flist = file1.listFiles();   
       for (File f : flist) {  
           if(f.isFile()){  
               copyFile2(f.getPath(),f2 + File.separator + f.getName()); //调用复制文件的方法  
//               System.out.println("原路径["+f.getPath()+"] 被复制路径["+f2+"/"+f.getName()+"]");
           }else if(f.isDirectory()){  
               copyFile1(f.getPath(),f2 + File.separator + f.getName()); //调用复制文件夹的方法 
//               System.out.println("原路径["+f.getPath()+"] 被复制路径["+f2+"/"+f.getName()+"]");
           }  
       }      
	 }  
	 
	   /**
	    * 复制文件夹
	    * @throws IOException 
	    */
   public static void copyFile1(String f1,String f2) throws IOException{
       //创建文件夹
       File file=new File(f2);
       if(!file.exists()){
           file.mkdirs();
       }    
       copy(f1,f2);
   }
	     
   /** 
    * 复制文件
   * @throws IOException 
    */  
   public static void copyFile2(String f1, String f2) throws IOException {  
	   InputStream input = null;
	   OutputStream output = null;
	   try {  
            input = new FileInputStream(f1);
            output= new FileOutputStream(f2);
            byte[] bt=new byte[1024];
            if((input!=null)&&(output!=null)){
                while((input.read(bt))!=(-1)){
                        output.write(bt,0,bt.length);      
                   }
               }

             
       } catch (FileNotFoundException e) {  
           e.printStackTrace();  
       }finally {
    	   if(input != null) {
    		   input.close();
    	   }
    	   if(output != null) {
    		   output.close();
    	   }
       }
   }



@Override
public void onPrepare() {
	// TODO Auto-generated method stub
	
}
}
