package com.mutation;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.finalmodule.base.FinalBean;
import com.finalmodule.base.IFinalProcessModule;
import com.utils.ConfigUtils;
import com.utils.Utils;

public class Mutation extends FinalBean implements IFinalProcessModule {
	
	private String[] projects;
	private String[] bugs;
	
	

	@Override
	public void process(Runtime runTime, StringBuilder processLog) throws Exception {
		this.projects = super.config.getConfig(ConfigUtils.PRO_PROJECT_ID_KEY).split(",");
		this.bugs = super.config.getBugIdArr();
		boolean needCheckOut = "YES".equals(config.getConfig("NEED_CHECKOUT"));
		String baseClassPath = config.getConfig(Command.PRO_KEY_CLASS_PATH);
		if("".equals(baseClassPath)) {
			throw new Exception("[ERROR] CLASS_PATH不能为空！");
		}
		String projectBasePath = super.config.getConfig(Command.PRO_MUTATION_PROJECT_PATH);
		String d4jPath = config.getConfig(ConfigUtils.PRO_D4J_HOME_KEY);
		String exeModule = super.config.getConfig(Command.PRO_KEY_EXE_MODULE);
		if("".equals(exeModule)) {
			exeModule = Command.DEFAULT_EXE_MODULE;
		}
		
		File outputFile = new File(System.getProperty("user.home") + File.separator + "mutations.sh");
		FileUtils.writeStringToFile(outputFile, "#!/bin/bash\n", false);
		
		String sourceDir;
//		String srcClassDir;
		String testClassDir;
		String reportDir;
		String targetTests;
		
		for(String project : projects) {

			for(String bugId : bugs) {
				String projectPath = projectBasePath + File.separator + "SBFL-" + project + File.separator +
						project + "_" + bugId + File.separator;
				createDir(projectPath);
				if(needCheckOut) {
					String checkoutCommand = "defects4j checkout -p " + project + " -v " + bugId + "b -w " + projectPath;
					Utils.executeCommandLine(runTime, checkoutCommand);
				}
				Utils.executeCommandLine(runTime, "cd " + projectPath, "defects4j compile", "defects4j test");
//				srcClassDir = projectPath + Utils.getCompilePathByProjectID(project, bugId);
				
				sourceDir = getSourceDir(runTime, projectPath, project, bugId);
				
				reportDir = System.getProperty("user.home") + File.separator + "mutationReports" + File.separator + project + File.separator + bugId + File.separator;
				createDir(reportDir);
				
				testClassDir = getTestClassDir(runTime, projectPath, project, bugId);
				
				String targetClasses = getLoadClasses(d4jPath, project, bugId);
				
				targetTests = getTargetTests(d4jPath, project, bugId);
				
				String command = Command.BASE_COMMAND
						.replace("@CLASS_PATH@", baseClassPath)
						.replace("@DYNAMIC_PATH@", getDynamicPath(projectPath, project, bugId))
						.replace("@srcClasses_Dir@", getSrcDir(runTime, projectPath, project, bugId))
						.replace("@testClasses_Dir@", testClassDir)
						.replace("@EXE_MODULE@", exeModule)
						.replace("@reports_Dir@", reportDir)
						.replace("@targetClasses@", targetClasses)
						.replace("@targetTests@", targetTests)
						.replace("@source_Dir@", sourceDir);
				
				FileUtils.writeStringToFile(outputFile, "rm -rf " + reportDir + File.separator + "* \n", true);
				if("1".equals(bugId)){
					FileUtils.writeStringToFile(outputFile, "startTime = $(date \"+%Y-%m-%d %H:%M:%S\").$((`date \"+%N\"`/1000000)) \n", true);
				}
				FileUtils.writeStringToFile(outputFile, command+"\n", true);
				if("1".equals(bugId)){
					FileUtils.writeStringToFile(outputFile, "endTime = $(date \"+%Y-%m-%d %H:%M:%S\").$((`date \"+%N\"`/1000000)) \n", true);
					FileUtils.writeStringToFile(outputFile, "echo \"cost time : \" \n", true);
					FileUtils.writeStringToFile(outputFile, "echo $(($endTime - $startTime)) > /home/yy/mutationReports/"+project+"-"+bugId+"-cost.txt \n", true);
				}
			}
		}
		
//		Utils.executeCommandLine(runTime, Command.COMMAND.replace("@MUTATION_FILE@", outputFile.getAbsolutePath()));
		System.out.println("[INFO] 模块执行完成，请使用命令 sh " + outputFile.getAbsolutePath() + "生成变异报告！！！");
	}
	
	private String getLoadClasses(String d4jHome, String projectId, String bugId) throws IOException{
		String path = Command.LOAD_CLASSES_PATH.replace("@D4J_HOME@", d4jHome)
				.replace("@PROJECT_ID@", projectId)
				.replace("@BUG_ID@", bugId);
		File file = new File(path);
		List<String> list = FileUtils.readLines(file, "utf-8");
		StringBuilder ret = new StringBuilder();
		for(String clz : list) {
			ret.append(clz).append(",");
		}
		return ret.length() >= 1 ? ret.substring(0, ret.length() - 1) : ret.toString();
	}
	
	private String getTargetTests(String d4jHome, String projectId, String bugId) throws IOException {
		String path = Command.TARGET_TESTS_PATH.replace("@D4J_HOME@", d4jHome)
				.replace("@PROJECT_ID@", projectId)
				.replace("@BUG_ID@", bugId);
		File file = new File(path);
		List<String> list = FileUtils.readLines(file, "utf-8");
		StringBuilder ret = new StringBuilder();
		for(String clz : list) {
			ret.append(clz).append(",");
		}
		return ret.length() >= 1 ? ret.substring(0, ret.length() - 1) : ret.toString();
	}
	
	private void createDir(String path) {
		File dir = new File(path);
		if(!dir.exists()) {
			dir.mkdirs();
		}
	}
	
	static String COMMAND_CD = "cd @PROJECT_PATH@";
	static String COMMAND_LOAD_CLASS = "defects4j export -p dir.bin.classes";
	
	private String getSrcDir(Runtime runTime, String projectPath, String projectId, String bug)throws Exception{
		String[] ret = Utils.executeCommandLine(runTime, COMMAND_CD.replace("@PROJECT_PATH@", projectPath), COMMAND_LOAD_CLASS);
		if(!"0".equals(ret[0])) {
			throw new Exception("[ERROR] 获取srcClasses_Dir异常！");
		}
		return projectPath + File.separator + ret[1].replace("\n", "");
	}
	
	static String COMMAND_TEST_DIR = "defects4j export -p dir.bin.tests";
	private String getTestClassDir(Runtime runTime, String projectPath, String projectId, String bug) throws Exception {
		String[] ret = Utils.executeCommandLine(runTime, COMMAND_CD.replace("@PROJECT_PATH@", projectPath), COMMAND_TEST_DIR);
		if(!"0".equals(ret[0])) {
			throw new Exception("[ERROR] 获取testClasses_Dir异常！");
		}
		return projectPath + File.separator + ret[1].replace("\n", "");
//		int bugId = Integer.parseInt(bug);
//		switch (projectId) {
//		case "Chart":
//			return projectPath + File.separator + "build-tests" + File.separator;
//		case "Lang":
//			if(bugId >= 21 && bugId <= 41) {
//				return projectPath + File.separator + "target" + File.separator + "test-classes" + File.separator;
//			}
//			return projectPath + File.separator + "target" + File.separator + "tests" + File.separator;
//		case "Closure":
//			return projectPath + File.separator + "build" + File.separator + "test" + File.separator;
//		case "Math":
//			return projectPath + File.separator + "target" + File.separator + "test-classes" + File.separator;
//		case "Mockito":
//			if((bugId >= 1 && bugId <= 11) || (bugId >= 18 && bugId <= 21)) {
//				return projectPath + File.separator + "build" + File.separator + "classes" + File.separator + "test" + File.separator;
//			}
//			return projectPath + File.separator + "target" + File.separator + "test-classes" + File.separator;
//		case "Time":
//			if(bugId >= 1 || bugId <= 11) {
//				return projectPath + File.separator + "target" + File.separator + "test-classes" + File.separator;
//			}
//			return projectPath + File.separator + "build" + File.separator + "tests" + File.separator;
//		default:
//			throw new Exception("错误的projectId");
//		}
	}
	
	static String COMMAND_SOURCE_DIR = "defects4j export -p dir.src.classes";
	private String getSourceDir(Runtime runTime, String projectPath, String projectId, String bug)throws Exception{
		String[] ret = Utils.executeCommandLine(runTime, COMMAND_CD.replace("@PROJECT_PATH@", projectPath), COMMAND_SOURCE_DIR);
		if(!"0".equals(ret[0])) {
			throw new Exception("[ERROR] 获取source_dir异常！");
		}
		return projectPath + File.separator + ret[1].replace("\n", "");
	}
	
	private String getDynamicPath(String projectPath, String projectId, String bugid) {
		StringBuilder path = new StringBuilder("");
		if("Closure".equals(projectId)) {
			File dir = new File(projectPath + File.separator + "lib" + File.separator);
			File[] jarList = dir.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return name.endsWith(".jar");
				}
			});
			for(File jar : jarList) {
				path.append(jar.getAbsolutePath()).append(File.pathSeparator);
			}
			dir = new File(projectPath + File.separator + "build" + File.separator + "lib" + File.separator);
			if (dir.exists()) {
				jarList = dir.listFiles(new FilenameFilter() {
					@Override
					public boolean accept(File dir, String name) {
						return name.endsWith(".jar");
					}
				});
				for(File jar : jarList) {
					path.append(jar.getAbsolutePath()).append(File.pathSeparator);
				}
			}
		}
		return path.toString();
	}
}
