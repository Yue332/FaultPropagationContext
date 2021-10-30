package com.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.utils.FileUtils;

import com.finalmodule.CalculateExam;
import com.module.Defects4j;
import com.module.Gzoltar;
import com.utils.cal.func.Ochiai;


public class ConfigUtils {
	public static String USER_OS_NAME = System.getProperty("os.name");
	
	public static String DEF_CONFIG_FILE_PATH = System.getProperty("user.home", "/tmp") + File.separator + "config.properties";
	
	public static String PRO_PROCESS_MODULE_KEY = "PROCESS_MODULE";
	public static String PRO_PROCESS_MODULE_COMMENT = "# ����ģ����ʹ��Ӣ�Ķ��Ÿ��������� " + Defects4j.class.getName() + "," + Gzoltar.class.getName();
	
	public static String PRO_FINAL_PROCESS_MODULE_KEY = "FINAL_PROCESS_MODULE";
	public static String PRO_FINAL_PROCESS_MODULE_COMMENT = "# ���մ���ģ�� ���ʹ��Ӣ�Ķ��Ÿ���������" + CalculateExam.class.getName();
	
	public static String PRO_D4J_HOME_KEY = "D4J_HOME";
	
	public static String PRO_D4J_HOME_COMMENT = "# defects4j ��ȫ·��";
	
	public static String PRO_FAULT_LOCALIZATION_DATA_HOME_KEY = "FAULT_LOCALIZATION_DATA_HOME";
	
	public static String PRO_FAULT_LOCALIZATION_DATA_HOME_COMMENT = "# fault-localization-data ��ȫ·��";
	
	public static String PRO_LITHIUM_SLICER_HOME_KEY = "LITHIUM_SLICER_HOME";
	
	public static String PRO_LITHIUM_SLICER_HOME_COMMENT = "# lithium-slicer ��ȫ·��";
	
	public static String PRO_PROJECT_ID_KEY = "PROJECT_ID";
	
	public static String PRO_PROJECT_ID_COMMENT = "# defects4j��ĿID [Lang, Chart, Closure, Math, Mockito, Time]";
	
	public static String PRO_BUG_ID_KEY = "BUG_ID";
	
	public static String PRO_BUG_ID_COMMENT = "# defects4j��ĿBUG_ID�����ʹ��Ӣ�Ķ��ŷָ�����������ʹ��ȫ��bug";
	
	public static String PRO_TOP_KEY = "TOP";
	
	public static String PRO_TOP_COMMENT = "# lithium slicer��TOP����";
	
	public static String PRO_PROJECT_PATH_KEY = "PROJECT_PATH";
	
	public static String PRO_PROJECT_PATH_COMMENT = "# defects4j�����Ŀ��ȫ·��";
	
	public static String PRO_TOOL_KEY = "TOOL";
	
	public static String PRO_TOOL_COMMENT = "# gzoltar��TOOL���� [developer, evosuite, randoop]";
	
	public static String PRO_FUNC_KEY = "FUNC";
	
	public static String PRO_FUNC_COMMENT = "# ����Ƶ�����÷��������ʹ��Ӣ�Ķ��Ÿ���������" + Ochiai.class.getName();
	
	public static String FAIL_FILE_PATH_COMMENT = "# ִ��ʧ��ʱʧ����־д����ļ�ȫ·����Ϊ�ղ�д�룩";
	
	public static String FAIL_FILE_PATH_KEY = "FAIL_FILE_PATH";
	
	public static String TOP_N_KEY = "TOP_N";
	
	public static String TOP_N_COMMENT = "# TOP-N";
	
	public static String SORT_FUNC = "SORT_FUNC";
	
	public static String SORT_FUNC_COMMENT = "# csv�ļ����ŵ���Ĺ�ʽ������Ϊ����Ĭ��ʹ�õ�һ����ʽ��";
	
	public static final String DEFAULT_PROCESS_MODULES = "com.module.Defects4j,com.module.Gzoltar,com.module.LithiumSlicer,com.module.Analysis,com.module.CalculateValueAndGenCSV";
	
	public static Map<String, String> PRO_MAP = new LinkedHashMap<String, String>();
	static {
		PRO_MAP.put(PRO_D4J_HOME_KEY, PRO_D4J_HOME_COMMENT);
		PRO_MAP.put(PRO_PROCESS_MODULE_KEY, PRO_PROCESS_MODULE_COMMENT);
		PRO_MAP.put(PRO_FINAL_PROCESS_MODULE_KEY, PRO_FINAL_PROCESS_MODULE_COMMENT);
		PRO_MAP.put(PRO_FAULT_LOCALIZATION_DATA_HOME_KEY, PRO_FAULT_LOCALIZATION_DATA_HOME_COMMENT);
		PRO_MAP.put(PRO_LITHIUM_SLICER_HOME_KEY, PRO_LITHIUM_SLICER_HOME_COMMENT);
		PRO_MAP.put(PRO_PROJECT_ID_KEY, PRO_PROJECT_ID_COMMENT);
		PRO_MAP.put(PRO_PROJECT_PATH_KEY, PRO_PROJECT_PATH_COMMENT);
		PRO_MAP.put(PRO_BUG_ID_KEY, PRO_BUG_ID_COMMENT);
		PRO_MAP.put(PRO_TOP_KEY, PRO_TOP_COMMENT);
		PRO_MAP.put(SORT_FUNC, SORT_FUNC_COMMENT);
		PRO_MAP.put(PRO_TOOL_KEY, PRO_TOOL_COMMENT);
		PRO_MAP.put(PRO_FUNC_KEY, PRO_FUNC_COMMENT);
		PRO_MAP.put(TOP_N_KEY, TOP_N_COMMENT);
		PRO_MAP.put(FAIL_FILE_PATH_KEY, FAIL_FILE_PATH_COMMENT);
	}
	
	public static void generateDefaultPropertyFile()throws Exception{
		
		File f = new File(DEF_CONFIG_FILE_PATH);
		if(f.exists()) {
			return;
		}
		f.createNewFile();
		FileWriter writer = new FileWriter(f);
    	try {
    		String key;
    		for(Map.Entry<String, String> entry : PRO_MAP.entrySet()) {
    			writer.write(entry.getValue() + "\n");
    			key = entry.getKey();
    			writer.write(key + "=" + (PRO_PROCESS_MODULE_KEY.equals(key) ? DEFAULT_PROCESS_MODULES : "") + "\n");
    		}
    	}catch (Exception e) {
			throw e;
		}finally {
			if(writer != null) {
				writer.close();
			}
		}
	}
	
	public static String[] getAllBugIdByD4J(String d4jHome, String projectId)throws Exception {
		String[] allBugArr = getAllBugIdByFile(projectId);
		if(allBugArr == null) {
			String command = Defects4j.D4J_BASE_COMMAND.replaceAll("@:D4J_HOME@", d4jHome) + "defects4j query -p "+projectId+" -q bug.id -o " + System.getProperty("user.home") + File.separator + projectId + "_allbugs";
			Runtime runTime = Runtime.getRuntime();
			String[] msg = Utils.executeCommandLine(runTime, command);
			System.out.println("[DEBUG] " + Arrays.toString(msg));
			allBugArr = getAllBugIdByFile(projectId);
		}
		return allBugArr;
	}
	
	public static String[] getAllBugIdByFile(String projectId) throws Exception {
		File allBugIdFile = new File(System.getProperty("user.home") + File.separator + projectId + "_allbugs");
		if(!allBugIdFile.exists()) {
			System.out.println("[INFO] �ļ���"+allBugIdFile.getAbsolutePath()+"�������ڣ���ʼʹ��defects4j��������");
			return null;
		}
		List<String> list = FileUtils.readLines(allBugIdFile, "UTF-8");
		return list.toArray(new String[] {});
	}
}
