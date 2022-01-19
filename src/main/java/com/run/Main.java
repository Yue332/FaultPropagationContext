package com.run;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import com.utils.FileUtils;

import com.finalmodule.base.IFinalProcessModule;
import com.module.IProcessModule;
import com.utils.ConfigUtils;
import com.utils.Configer;
import com.utils.Utils;

public class Main {

//	public static void main(String[] args) {
//		String command1 = "cd /root/defects4j/framework/bin/";
//		String command2 = "./defects4j -p Chart -v 1b -w ";
//	}
	public static void main(String[] args) {
		String osName = System.getProperty("os.name").toLowerCase();
		if(osName.contains("windows")){
			System.setProperty("user.home", System.getProperty("user.home") + File.separator + "Desktop");
			System.out.println("当前为windows系统，设置家目录为" + System.getProperty("user.home"));
		}
		
		System.out.println("请输入配置文件全路径（输入回车则使用默认配置文件） :");
		Scanner sc = new Scanner(System.in);
		String configPath = sc.nextLine();
		sc.close();
		//生成默认的配置文件 默认路径为 家目录/config.properties
		if("".equals(configPath)) {
			configPath = ConfigUtils.DEF_CONFIG_FILE_PATH;
			System.out.println("[INFO] 未发现配置文件，使用默认配置文件 ("+configPath+")。");
			if(!new File(ConfigUtils.DEF_CONFIG_FILE_PATH).exists()) {
				System.out.println("[INFO] 配置文件不存在，开始生成配置文件。");
				try {
					ConfigUtils.generateDefaultPropertyFile();
					System.out.println("[INFO] 配置文件生成完成！");
					System.out.println("[!!!INFO!!!] 请配置完配置文件后再次运行程序");
					System.exit(0);
				}catch(Exception e) {
					System.out.println("[ERROR] 配置文件生成异常！" + e.getMessage());
					e.printStackTrace();
					System.exit(1);
				}
			}
		}
		
		// 读取配置信息
		Configer config = new Configer(configPath);
		try {
			config.loadConfig();
		} catch (Exception e) {
			System.out.println("[ERROR] 配置文件加载异常！" + e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}
		Runtime runTime = Runtime.getRuntime();
		List<IProcessModule> processModuleList = config.getProcessModuleList();
		List<String> failBugIdList = new ArrayList<String>();
		String[] bugIdArr = config.getBugIdArr();
		StringBuilder failMsg = new StringBuilder();
		long startTime = System.currentTimeMillis();
		if(processModuleList != null && processModuleList.size() != 0) {
			for(String bugId : bugIdArr) {
//				config.setCurrentBugId(bugId);
				System.out.println("[INFO] 开始处理bug：" + bugId);
				for(IProcessModule module : processModuleList) {
					try {
						module.setBugId(bugId);
						module.setProjectPath(config.getConfig(ConfigUtils.PRO_PROJECT_PATH_KEY) + config.getConfig(ConfigUtils.PRO_PROJECT_ID_KEY) + "_" + bugId + File.separator);
						module.onPrepare();
						module.process(runTime);
					} catch (Exception e) {
						failBugIdList.add(bugId);
						failMsg.append("bug["+bugId+"]模块["+module.getClass().getName()+"]执行异常！异常原因：" + Utils.getExceptionString(e)).append("\r\n");
						System.out.println("[ERROR] 模块 【"+module.getClass().getName()+"】 执行异常！" + e.getMessage());
						e.printStackTrace();
						System.exit(1);
					}
				}
				System.out.println("[INFO] bug【"+bugId+"】处理完成！");
			}
		}
		// 最终处理模块
		List<IFinalProcessModule> fianlProcessModuleList = config.getFinalProcessModuleList();
		StringBuilder finalProcessLog = new StringBuilder();
		for(IFinalProcessModule module : fianlProcessModuleList) {
			System.out.println("[INFO] 开始执行模块" + module.getClass().getName());
			finalProcessLog.append("模块").append(module.getClass().getName()).append("执行日志：\r\n");
			try {
				module.setConfig(config);
				module.setFailBugId(failBugIdList);
				module.onPrepare();
				module.process(runTime, finalProcessLog);
			} catch (Exception e) {
				failMsg.append("最终模块【"+module.getClass().getName()+"】执行异常！异常原因：" + Utils.getExceptionString(e)).append("\r\n");
				System.out.println("[ERROR] 最终模块【"+module.getClass().getName()+"】执行异常！" + e.getMessage());
				e.printStackTrace();
			}
			System.out.println("[INFO] 模块" + module.getClass().getName() + "执行结束");
		}
		long endTime = System.currentTimeMillis();
		
		String failFilePath = config.getConfig(ConfigUtils.FAIL_FILE_PATH_KEY);
		if(!"".contentEquals(failFilePath)) {
			System.out.println("[INFO] 失败信息不为空！写入失败文件");
			File failFile = new File(failFilePath);
			try {
				FileUtils.writeStringToFile(failFile, failMsg + "\r\n" + finalProcessLog, false);
			} catch (Exception e) {
				System.out.println("[WARNING] 失败信息写入异常！" + e.getMessage());
				e.printStackTrace();
			}
		}
		System.out.println("执行时间：" + (endTime - startTime) + "ms");
		System.out.println("[INFO] 所有模块执行完成，程序结束！");
	}

}
