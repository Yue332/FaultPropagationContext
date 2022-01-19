package com.module;

import java.io.File;
import java.math.BigDecimal;
import java.util.*;

import org.apache.commons.io.FileUtils;

import com.utils.Bean;
import com.utils.ConfigUtils;
import com.utils.Configer;
import com.utils.SuspValueBean;

import mysoot.MyMain;
import soot.G;
//Ochiai,Wong3,Dstar,Binary
public class MethodSuspValue extends Bean implements IProcessModule {
	private String suspValue;
	private String output;
	private String[] funcArr;
	public MethodSuspValue(Configer config) {
		super(config);
	}
	
	@Override
	public void onPrepare() {
		this.suspValue = super.projectPath + File.separator + "gzoltar_output" + File.separator + super.projectId + File.separator + super.bugId + File.separator + 
				super.projectId + "-" + super.bugId + "-" + "suspValue.csv";
		this.output = System.getProperty("user.home") + File.separator + "methodSuspValue" + File.separator + projectId + "-" + bugId + "-@:FUNC@-method-suspValue.csv";
		funcArr = super.config.getConfig(ConfigUtils.PRO_FUNC_KEY).split(",");
	}

	@Override
	public void process(Runtime runTime) throws Exception {
		System.out.println("开始计算");
		File suspValueFile = new File(this.suspValue);
		List<String> list = FileUtils.readLines(suspValueFile);
		list.remove(0);
		for(int funcIdx = 0; funcIdx < this.funcArr.length; funcIdx ++) {
			StringBuilder info = new StringBuilder();
			String func = funcArr[funcIdx];
			File logFile = new File(System.getProperty("user.home") + File.separator + "methodSuspValue" + File.separator + "logs" + File.separator + projectId + "-" + bugId + "-" + func + ".log");
			List<SuspValueBean> sList = SuspValueBean.getBeanList(list, funcIdx);
			Map<String, Map<String, List<BigDecimal>>> map = new HashMap<String, Map<String, List<BigDecimal>>>();
			for(SuspValueBean bean : sList) {
				String clz = bean.getClz();
				int lineNumber = bean.getLineNumber();
				BigDecimal suspValue = bean.getSuspValue();
				if(suspValue.compareTo(new BigDecimal("0")) == 0) {
					continue;
				}
				System.out.println("[DEBUG] 开始分析" + bean.getLine());
				MyMain.setSootEnv(this.projectPath, this.projectId, bugId);
				String method = MyMain.analysis(info, clz, lineNumber);
				G.reset();
				if(method == null) {
					continue;
				}
				Map<String, List<BigDecimal>> methodMap = map.containsKey(clz) ? map.get(clz) : new HashMap<String, List<BigDecimal>>();
				List<BigDecimal> suspValueList = methodMap.containsKey(method) ? methodMap.get(method) : new ArrayList<BigDecimal>();
				suspValueList.add(suspValue);
				methodMap.put(method, suspValueList);
				map.put(clz, methodMap);
			}
			File outputFile = new File(this.output.replace("@:FUNC@", func));
			if(!outputFile.getParentFile().exists()) {
				outputFile.getParentFile().mkdirs();
			}
			FileUtils.writeStringToFile(outputFile, "class,method,suspValue\r\n", false);
			List<SortBean> beanList = new ArrayList<>();
			for(Map.Entry<String, Map<String, List<BigDecimal>>> entry : map.entrySet()) {
				String clz = entry.getKey();
				Map<String, List<BigDecimal>> methodMap = entry.getValue();
				for(Map.Entry<String, List<BigDecimal>> e : methodMap.entrySet()) {
					String methodName = e.getKey();
					BigDecimal maxSuspValue = getMaxSuspValue(e.getValue());
					String line = methodName + "," + maxSuspValue.toPlainString() + "\r\n";
					SortBean bean = new SortBean(line, maxSuspValue);
					beanList.add(bean);
//					FileUtils.writeStringToFile(outputFile, clz + "," + methodName + "," + maxSuspValue.toPlainString() + "\r\n", true);
				}
			}
			Collections.sort(beanList);
			for(SortBean bean : beanList){
				FileUtils.writeStringToFile(outputFile, bean.getLine(), true);
			}
			FileUtils.writeStringToFile(logFile, info.toString(), false);
		}
	}

	private BigDecimal getMaxSuspValue(List<BigDecimal> suspValueList) {
		BigDecimal value = suspValueList.get(0);
		for(int i = 1; i < suspValueList.size(); i ++) {
			BigDecimal a = suspValueList.get(i);
			if(value.compareTo(a) < 0) {
				value = a;
			}
		}
		return value;
	}

	private class SortBean implements Comparable<SortBean>{
		private String line;
		private BigDecimal value;

		private SortBean(String line, BigDecimal value) {
			this.line = line;
			this.value = value;
		}

		public String getLine() {
			return line;
		}

		public BigDecimal getValue() {
			return value;
		}

		@Override
		public int compareTo(SortBean o) {
			return o.getValue().compareTo(this.value);
		}
	}
}
