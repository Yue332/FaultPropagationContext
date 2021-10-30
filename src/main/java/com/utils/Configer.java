package com.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.finalmodule.base.IFinalProcessModule;
import com.module.IProcessModule;
import com.utils.cal.IAnalysisFunc;

public class Configer {
	
	String configFilePath;
	Map<String, String> configMap;
	List<IProcessModule> processModuleList;
	List<IFinalProcessModule> finalProcessModuleList;
	List<IAnalysisFunc> funcList;
	String[] bugIdArr;
	String currentBugId = null;
	
	public Configer(String configFilePath) {
		super();
		this.configFilePath = configFilePath;
		this.processModuleList = new ArrayList<IProcessModule>();
		this.finalProcessModuleList = new ArrayList<IFinalProcessModule>();
		this.funcList = new ArrayList<IAnalysisFunc>();
	}
	
	public void loadConfig()throws Exception{
		File file = new File(configFilePath);
		if(!file.exists()) {
			throw new Exception("[ERROR] �����ļ� ��"+configFilePath+"�� �����ڣ�");
		}
		Properties pro = new Properties();
		InputStream in = null;
		try {
        	in = new FileInputStream(file);
        	pro.load(in);
        	this.configMap = (Map) pro;
        	if(this.configMap.size() == 0) {
        		throw new Exception("[ERROR] �����ļ������ԣ�");
        	}
        	// ��ȡ�������ģ�飬��ʵ����
        	String[] processModuleArray = configMap.get(ConfigUtils.PRO_PROCESS_MODULE_KEY).split(",");
        	for(String processModule : processModuleArray) {
        		System.out.println("[DEBUG] ��ʼ���ش���ģ�� : " + processModule);
        		if("".equals(processModule)) {
        			System.out.println("[INFO] ����ģ��Ϊ�գ������أ�");
        			continue;
        		}
				Class clz = Class.forName(processModule);
				Constructor c = clz.getDeclaredConstructor(Configer.class);
				c.setAccessible(true);
				Object obj = c.newInstance(this);
				if(obj instanceof IProcessModule) {
					processModuleList.add((IProcessModule) obj);
				}else {
					throw new Exception("����ģ�� ��"+processModule+"�� ����ʵ�ֽӿ��ࣺ " + IProcessModule.class.getName());
				}
        	}
        	// ��ȡ�����ļ��е�����ģ�飨����ģ��ִ�н����󣬲�ִ�е�ģ�飩����ʵ����
        	String[] finalProcessModuleArray = configMap.get(ConfigUtils.PRO_FINAL_PROCESS_MODULE_KEY).split(",");
        	for(String module : finalProcessModuleArray) {
        		if("".equals(module)) {
        			continue;
        		}
        		System.out.println("[DEBUG] ��ʼ�������մ���ģ�飺" + module);
        		Class clz = Class.forName(module);
        		Object obj = clz.newInstance();
        		if(obj instanceof IFinalProcessModule) {
        			this.finalProcessModuleList.add((IFinalProcessModule) obj);
        		}else {
        			throw new Exception("����ģ�� ��"+module+"�� ����ʵ�ֽӿ��ࣺ " + IFinalProcessModule.class.getName());
        		}
        	}
        	
        	// ��ȡ���û��4j��������Ҫ�����bugid
        	String projectId = configMap.get(ConfigUtils.PRO_PROJECT_ID_KEY);
        	if(!projectId.contains(",")) {// ��ĿID����������ţ�˵���Ƕ����Ŀ����ʱ������bugid
        		this.bugIdArr = configMap.get(ConfigUtils.PRO_BUG_ID_KEY).split(",");
//        	System.out.println("[DEBUG] bugidArr = " + Arrays.toString(this.bugIdArr));
        		if(this.bugIdArr.length == 0 || "".equals(this.bugIdArr[0])) {
        			System.out.println("[INFO] �����ļ���δ����BUG_ID��ʹ��ȫ��bugid");
        			String d4jHome = configMap.get(ConfigUtils.PRO_D4J_HOME_KEY);
        			this.bugIdArr = ConfigUtils.getAllBugIdByD4J(d4jHome, projectId);
        			System.out.println("[INFO] ��Ŀ��"+projectId+"���а���bug��" + Arrays.toString(this.bugIdArr));
        		}
        		
        	}
		}catch (Exception e) {
			throw e;
		}finally {
			if(in != null) {
				in.close();
			}
		}
	}
	
	public String getConfig(String configKey) {
		return this.configMap.get(configKey);
	}

	public String getConfigFilePath() {
		return configFilePath;
	}

	public void setConfigFilePath(String configFilePath) {
		this.configFilePath = configFilePath;
	}

	public Map<String, String> getConfigMap() {
		return configMap;
	}

	public void setConfigMap(Map<String, String> configMap) {
		this.configMap = configMap;
	}

	public List<IProcessModule> getProcessModuleList() {
		return processModuleList;
	}
	
	public List<IFinalProcessModule> getFinalProcessModuleList(){
		return finalProcessModuleList;
	}

	public void setProcessModuleList(List<IProcessModule> processModuleList) {
		this.processModuleList = processModuleList;
	}

	public String[] getBugIdArr() {
		return bugIdArr;
	}

	public void setBugIdArr(String[] bugIdArr) {
		this.bugIdArr = bugIdArr;
	}

	public String getCurrentBugId() {
		return currentBugId;
	}

	public void setCurrentBugId(String currentBugId) {
		this.currentBugId = currentBugId;
	}
	
	
	
}
