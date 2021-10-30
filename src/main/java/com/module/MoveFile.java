package com.module;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import com.utils.FileUtils;

import com.utils.ConfigUtils;
import com.utils.Configer;
import com.utils.Utils;
@Deprecated
public class MoveFile extends LithiumSlicer implements IProcessModule {
	public static String LITHUIM_LOG_PATH = "@:LITHIUM_SLICER_HOME@/logs_@:BUG_ID@/";
	public MoveFile(Configer config) {
		super(config);
	}
	
	public void moveLithiumFile2Src()throws Exception{
		System.out.println("[INFO] start move lithum_xxx.java to src_path file");
		File logFilePath = new File(LITHUIM_LOG_PATH
				.replaceAll("@:BUG_ID@", bugId)
				.replaceAll("@:LITHIUM_SLICER_HOME@", config.getConfig(ConfigUtils.PRO_LITHIUM_SLICER_HOME_KEY)));
		if(!logFilePath.exists()) {
			throw new Exception("[ERROR] ¡¾"+logFilePath.getAbsolutePath()+"¡¿ not exists! please check lithium-slicer command execute success!");
		}
		List<File> lithiumFileList = getLithiumFileList(logFilePath);
		if(lithiumFileList.size() == 0) {
			throw new Exception("[ERROR] can not find lithum_xxx.java!");
		}
		String srcFilePath;
		for(File lithiumFile : lithiumFileList) {
			srcFilePath = getSrcFullPathBylithiumFile(lithiumFile);
			System.out.println("[INFO] copy file ¡¾"+lithiumFile+"¡¿ ----> ¡¾"+srcFilePath+"¡¿");
//			FileUtils.copyFile(lithiumFile, new File(srcFilePath));
		}
	}
	
	private String getSrcFullPathBylithiumFile(File lithiumFile)throws Exception {
		InputStream ins = null;
		InputStreamReader isr = null;
		BufferedReader reader = null;
		try {
			ins = new FileInputStream(lithiumFile);
			isr = new InputStreamReader(ins);
			reader = new BufferedReader(isr);
			String line;
			boolean isStartWithPackage = false;
			while((line = reader.readLine()) != null) {
				if(line.startsWith("package")) {
					isStartWithPackage = true;
					break;
				}
			}
			if(!isStartWithPackage) {
				throw new Exception("[ERROR] there is no 'package' in file ¡¾"+lithiumFile.getAbsolutePath()+"¡¿ cannot get src file path!");
			}
			System.out.println("[INFO] read ¡¾"+line+"¡¿ from file ¡¾"+lithiumFile.getAbsolutePath()+"¡¿");
			line = line.substring(8).replaceAll(";", "").replaceAll("[.]", "/") + "/";
			String srcPath = Utils.getSourcePathByProjectID(this.projectId);
			return this.projectPath + File.separator + srcPath + File.separator + line + lithiumFile.getName().replace("lithium_", "");
		} catch (Exception e) {
			throw e;
		}finally {
			if(reader != null) {
				reader.close();
			}
			if(isr != null) {
				isr.close();
			}
			if (ins != null) {
				ins.close();
			}
		}
		
	}

	@Override
	public void process(Runtime runTime) throws Exception {
		moveLithiumFile2Src();
	}

}
