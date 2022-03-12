package com.module;

import java.io.File;
import java.io.FilenameFilter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.utils.Bean;
import com.utils.ConfigUtils;
import com.utils.Configer;
import com.utils.FileUtils;
import com.utils.Utils;
import com.utils.cal.IAnalysisFunc;

import mysoot.MyMain;
import soot.G;

public class DataDependenceAnalysis extends Bean implements IProcessModule {
	private String csvFilePath;
//	private String sortFuncName;
	private int top;
//	private int sortFuncIdx;
	private String[] funcArr;

	public static final String CD_PROJECT_DIR_COMMAND = "cd @:PROJECT_PATH@";
	public static final String D4J_COMPILE_COMMAND =  "defects4j compile";

	public DataDependenceAnalysis(Configer config) {
		super(config);

	}
	@Override
	public void onPrepare() {
		this.csvFilePath = super.projectPath + File.separator + "gzoltar_output" + File.separator + super.projectId + File.separator + super.bugId + File.separator;
//		this.sortFuncName = config.getConfig(ConfigUtils.SORT_FUNC);
		this.top = Integer.parseInt(config.getConfig(ConfigUtils.TOP_N_KEY));
		this.funcArr = config.getConfig(ConfigUtils.PRO_FUNC_KEY).split(",");
//		if(this.sortFuncName == null || "".equals(this.sortFuncName)) {
//			this.sortFuncName = funcArr[0];
//			this.sortFuncIdx = 0;
//			System.out.println("[INFO] 未配置排序公式，按照默认公式["+this.sortFuncName+"]进行排序");
//		}else if(!Arrays.asList(funcArr).contains(this.sortFuncName)) {
//			System.out.println("[INFO] 排序公式["+this.sortFuncName+"]未找到，按照默认公式["+funcArr[0]+"]进行排序");
//			this.sortFuncName = funcArr[0];
//			this.sortFuncIdx = 0;
//		}
	}

	@Override
	public void process(Runtime runTime) throws Exception {
		// 先编译4j项目
		String[] msg = Utils.executeCommandLine(runTime, CD_PROJECT_DIR_COMMAND.replaceAll("@:PROJECT_PATH@", super.projectPath),
				D4J_COMPILE_COMMAND);
		System.out.println("[DEBUG] " + Arrays.toString(msg));
		File csvFilePath = new File(this.csvFilePath);
		if(!csvFilePath.exists()) {
			throw new Exception("[ERROR] 路径["+this.csvFilePath+"]不存在！");
		}
		// 读取怀疑度表
		// Chart-1-suspValue.csv
		File[] csvFileList = csvFilePath.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
//				return name.endsWith(".csv") && name.contains("suspValue");
				return name.endsWith("suspValue.csv");
			}
		});

		for(File csvFile : csvFileList) {
			// Chart-1-suspValue.csv
			System.out.println("[INFO] 读取文件：" + csvFile.getName());
			// element,fun1,func2,func3
			// list[0] = xxx#121, 0.1, 1, 0.2
			// list[1] = xxx#120, 0.5, 0.3, 0.3
			List<String> csvList = FileUtils.readLines(csvFile, "UTF-8");
			csvList.remove(0);// 去掉首行

			// FUNC=func1,func2,func3
			// 0,1,2
			for(int sortFuncIdx = 0; sortFuncIdx < funcArr.length; sortFuncIdx ++){
				String sortFuncName = funcArr[sortFuncIdx];
				//定义了一个排序的对象，将怀疑度中所有的语句按照sortFuncName排倒序
				List<DataDependenceSortBean> list = new ArrayList<DataDependenceSortBean>();
				for(String line : csvList) {
					DataDependenceSortBean bean = new DataDependenceSortBean(line, sortFuncIdx);
					list.add(bean);
				}
				// list[0] = xxx#120, 0.5, 0.3, 0.3
				// list[1] = xxx#121, 0.1, 1, 0.2
				Collections.sort(list);
				
				String csvFileName = csvFile.getName().substring(0, csvFile.getName().length() - 4);
				String pFileName =  csvFileName + (csvFileName.endsWith("suspValue") ? "-src" : "") + "-" + sortFuncName + ".csv";
				// Chart-1-suspValue-com.utils.cal.func.Ochiai.csv
				File dataDependenceFile = new File((this.csvFilePath + File.separator + "dataDependence" + File.separator + pFileName.replaceAll("_", "-")));
				System.out.println("[INFO] 写入文件：" + dataDependenceFile.getAbsolutePath());
				FileUtils.write(dataDependenceFile, "", "UTF-8", false);

				// 生成倒序排序以后的怀疑度表
				String pSortFileName = csvFileName + (csvFileName.endsWith("suspValue") ? "-src" : "") + "-" + sortFuncName + "-sort.csv";
				File sortFile = new File(this.csvFilePath + File.separator + "sort" + File.separator);
				if(!sortFile.exists()) {
					sortFile.mkdirs();
				}
				sortFile = new File(sortFile.getAbsolutePath() + File.separator + pSortFileName);
				FileUtils.write(sortFile, "", "UTF-8", false);
				for(DataDependenceSortBean bean : list) {
					FileUtils.write(sortFile, bean.getLine() + "\r\n", "UTF-8", true);
				}

				int realTop = Math.min(this.top, list.size());
//				for(int i = 0; i < realTop; i ++) {
//					System.out.println("[DEBUG]" + list.get(i).getLine());
//				}
				for(int i = 0; i < realTop; i ++) {
					//xxx#120, 0.5, 0.3, 0.3
					String[] element = list.get(i).getLine().split(",");
					// org.xxx.Class 120
					String[] lineArr = element[0].split("#");
					String clz = lineArr[0];
					int lineNumber = Integer.parseInt(lineArr[1]);
					System.out.println("[INFO] 开始分析类" + clz + "第" + lineNumber + "行");
					MyMain.setSootEnv(this.projectPath, this.projectId, bugId);
					// [120,121,50]
					List<String> lineNumberList = MyMain.doMyAnalysis(clz, lineNumber);
//					System.out.println("[INFO] " + lineNumberList.toString());
					FileUtils.write(dataDependenceFile,  list.get(i).getLine().replaceAll("\\,", "_") + "," +
							lineNumberList.toString().replaceAll("\\[", "").replaceAll("\\]", "").replaceAll(" ", "").replaceAll("\\,", "/") + "\r\n", "UTF-8", true);
					G.reset();
				}

				Map<String, String> map = covertList2Map(csvList, sortFuncIdx);
				dataDependenceFile = new File(dataDependenceFile.getAbsolutePath());
				List<String> data = FileUtils.readLines(dataDependenceFile, "UTF-8");
				List<DataDepBean> listDataDep = new ArrayList<DataDependenceAnalysis.DataDepBean>(data.size());
				for(String temp : data) {
					listDataDep.add(new DataDepBean(temp, sortFuncIdx));
				}

				String dataDepNewFileName = csvFileName + (csvFileName.endsWith("suspValue") ? "-src" : "") + "-" + sortFuncName + ".csv";
				// Chart-1-suspValue_new-com.utils.cal.func.Ochiai.csv
				File dataDepNewFile = new File(dataDependenceFile.getParent() + File.separator + "new" + File.separator + dataDepNewFileName);
				FileUtils.write(dataDepNewFile, "", "UTF-8", false);
				for(DataDepBean bean : listDataDep) {
					FileUtils.write(dataDepNewFile, bean.getElement() + "," + bean.getNewScore(map).toPlainString() + "\r\n", "UTF-8", true);
				}
			}
		}


	}


	private class DataDependenceSortBean implements Comparable<DataDependenceSortBean>{
		private String line;
		private int funcIdx;


		private DataDependenceSortBean(String line, int funcIdx) {
			this.line = line;
			this.funcIdx = funcIdx;
		}

		private String getLine() {
			return this.line;
		}


		// DataDependenceSortBean1
		// DataDependenceSortBean2
		@Override
		public int compareTo(DataDependenceSortBean o) {
			// xxx#121,0.1,1,0.2
			// [xxx#121]	[0.1]	[1]	[0.2]
			// xxx#120,0.5,0.3,0.3
			String[] arr = this.line.split(",");
			String[] oArr = o.getLine().split(",");
//			return new BigDecimal(arr[this.funcIdx + 1]).compareTo(new BigDecimal(oArr[this.funcIdx + 1]));
			return new BigDecimal(oArr[this.funcIdx + 1]).compareTo(new BigDecimal(arr[this.funcIdx + 1]));
		}

	}

	private class DataDepBean{
		private String line;
		private int funcIdx;
		private String element;
		private BigDecimal score;
		private String[] lineNumberArr;
		private String clz;
		private boolean canGetNewScore = true;

		private DataDepBean(String line, int funcIdx) {
			System.out.println("[INFO] " + line);
			this.line = line;
			this.funcIdx = funcIdx;
			String temp[] = this.line.split(",");
			this.clz = temp[0].split("#")[0];
			String temp1[] = temp[0].split("_");
			this.score = new BigDecimal(temp1[1]);
			this.element = temp1[0];
			if(temp.length < 2) {
				System.out.println("[INFO] 语句" + this.line + "没有对应行号");
				canGetNewScore = false;
				return;
			}
			this.element += "," + temp[1];
			this.lineNumberArr = temp[1].split("/");
		}

		public BigDecimal getNewScore(Map<String, String> map) {
			if(!canGetNewScore) {
				return this.score;
			}
			BigDecimal sumScore = new BigDecimal("0");
			sumScore.setScale(IAnalysisFunc.scale, IAnalysisFunc.roundingMode);
			for(String lineNumber : lineNumberArr) {
				if(map.containsKey(this.clz + "#" + lineNumber)) {
					BigDecimal a = new BigDecimal(map.get(this.clz + "#" + lineNumber));
					a.setScale(IAnalysisFunc.scale, IAnalysisFunc.roundingMode);
					sumScore = sumScore.add(a);
				}else {
					System.out.println("[INFO] 怀疑度中不包含语句：" + this.clz + "#" + lineNumber);
				}
			}
			return sumScore;
		}

		public String getElement() {
			return this.element;
		}
	}

	public static Map<String, String> covertList2Map(List<String> csvList, int sortFuncIdx){
		Map<String, String> map = new HashMap<String, String>(csvList.size());
		for(String tmp : csvList) {
			// xxx#1231 1 0 2
			String[] arr = tmp.split(",");
			map.put(arr[0], arr[sortFuncIdx + 1]);
		}
		return map;
	}
}
