package com.module;

import com.utils.Bean;
import com.utils.Configer;
import com.utils.FileUtils;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.*;

/**
 * 
 */
public class Analysis extends Bean implements IProcessModule{
	private String lithiumOutputPath;
	protected String spectrumPath;
	protected String spectraPath;
	protected String matrixPath;

	public Analysis(Configer config) {
		super(config);
	}

	public static String CHART_SET = "UTF-8";
	
	@Override
	public void process(Runtime runTime) throws Exception {
		File spectra = new File(spectraPath);
		File matrix = new File(matrixPath);
		// 删除matrix文件所在目录下，以matrix_开头的文件
		File[] matrixs = matrix.getParentFile().listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.startsWith("matrix_");
			}
		});
		if(matrixs.length > 0){
			for(File f : matrixs){
				f.delete();
			}
		}
		List<String> codeLinesList = FileUtils.readLines(spectra, "UTF-8");
		List<String> matrixList = FileUtils.readLines(matrix, "UTF-8");
		// 获取失败的测试用例（末尾 - 的）
		String failMatrix = getFailMatrixArr(matrixList);
		int failMatrixIdx = matrixList.indexOf(failMatrix);
		if(failMatrixIdx == -1) {
			throw new Exception("matrix文件中不存在" + failMatrix);
		}
		matrixList.remove(failMatrixIdx);
		String[] failMatrixArr = failMatrix.split(" ");
		if(failMatrixArr.length - 1 != codeLinesList.size()) {
			throw new Exception("spectra文件中的代码行数("+codeLinesList.size()+")不等于matrix文件中失败的列数-1("+(failMatrixArr.length - 1)+")");
		}
		File lithiumOuput = new File(lithiumOutputPath);
		if(!lithiumOuput.exists()) {
			throw new Exception("[ERROR] 目录【"+lithiumOutputPath+"】不存在，请将约减后的目录放至此目录下");
		}
		System.out.println("[INFO] 计算前失败用例：" + Arrays.toString(failMatrixArr));
		File[] paths = lithiumOuput.listFiles(new FileFilter() {
			public boolean accept(File pathname) {
				return pathname.isDirectory();
			}
		});
		List<String> newMatrixList = new ArrayList<String>();
		for(File path : paths) {
			System.out.println("[INFO] 开始处理目录【"+path.getAbsolutePath()+"】");
			List<String> compareList = getCompareFileList(path);
			List<Integer> codeLineIdxList = new ArrayList<Integer>();
			int idx;
			for(String compare : compareList) {
				idx = codeLinesList.indexOf(compare);
				if(idx == -1) {
					continue;
				}
				codeLineIdxList.add(idx);
			}
			StringBuilder info = new StringBuilder();
			for(int i : codeLineIdxList) {
				if("1".equals(failMatrixArr[i])) {
					info.append(i).append(",");
					failMatrixArr[i] = "0";
				}
			}
			System.out.println("[INFO] 数组第["+info.toString()+"]列变为0");
			System.out.println("[INFO] " + path.getAbsolutePath() + "处理结果：" + Arrays.toString(failMatrixArr));
		}
		newMatrixList.addAll(matrixList);
		newMatrixList.add(failMatrixIdx, Arrays.toString(failMatrixArr).replace(",", "").replace("[", "").replace("]", ""));
		File newMatrix = new File(spectrumPath + File.separator + "matrix_new");
		for(String a : newMatrixList) {
			FileUtils.writeStringToFile(newMatrix, a + "\n", true);
		}
		System.out.println("[INFO] 处理完成，生成文件【"+newMatrix.getAbsolutePath()+"】");
	}

	public static String getFailMatrixArr(List<String> matrixList) {
		for(String s : matrixList) {
			if(s.endsWith("-")) {
				return s;
			}
		}
		return null;
	}
	/**
	 * 格式 类名#行号

	 */
	public static List<String> getCompareFileList(File path)throws Exception{
		Map<String, List<Integer>> map = getCompareFileMap(path);
		List<String> list = new ArrayList<String>();
		String name;
		for(Map.Entry<String, List<Integer>> entry : map.entrySet()) {
			for(int i : entry.getValue()) {
				name = entry.getKey();
				list.add(name + "#" + i);
			}
		}
		return list;
	}
	
	public static Map<String, List<Integer>> getCompareFileMap(File path)throws Exception{
		Map<String, List<Integer>> retMap = new LinkedHashMap<String, List<Integer>>();
		List<File> javaFileList = Arrays.asList(path.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				if(name.endsWith(".java") && !name.startsWith("lithium_")) {
					return true;
				}
				return false;
			}
		}));
		List<File> lithiumJavaFileList = Arrays.asList(path.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				if(name.endsWith(".java") && name.startsWith("lithium_")) {
					return true;
				}
				return false;
			}
		}));
		if(javaFileList.size() != lithiumJavaFileList.size()) {
			throw new Exception("目录中约减前的文件数量与约减后的文件数量不同，无法比较！");
		}
		
		List<String> srcList;
		List<String> destList;
		
		List<Integer> compareFileList;
		String name;
		for(int i = 0, length = javaFileList.size(); i < length; i ++) {
			File srcFile = javaFileList.get(i);
			File destFile = lithiumJavaFileList.get(i);
			srcList = FileUtils.readLines(srcFile, CHART_SET);
			destList = FileUtils.readLines(destFile, CHART_SET);
			compareFileList = getCompareFileList(srcList, destList);
			name = getClassFullName(srcFile, srcList);
			
			retMap.put(name, compareFileList);
		}
		
		return retMap;
	}
	
	/**
	 * 获取被约减文件的行号
	 * @param srcFile
	 * @param destFile
	 * @return
	 * @throws Exception
	 */
	public static List<Integer> getCompareFileList(File srcFile, File destFile)throws Exception{
		List<String> srcList = FileUtils.readLines(srcFile, CHART_SET);
		List<String> destList = FileUtils.readLines(destFile, CHART_SET);
		return getCompareFileList(srcList, destList);
	}
	
	public static List<Integer> getCompareFileList(List<String> srcList, List<String> destList)throws Exception{
		List<Integer> retList = new ArrayList<Integer>();
		int srcSize = srcList.size();
		String src;
		for(int i = 0; i < srcSize; i ++) {
			src = srcList.get(i);
			if(compareFileFilter(src)) {
				continue;
			}
			if(!destList.contains(src)) {
				retList.add(i + 1);
			}
		}
		return retList;
		
	}
	
	public static boolean compareFileFilter(String src) {
		String s = src.trim();
		return "".equals(s) || s.startsWith("*") || s.startsWith("/*") || s.startsWith("import") || s.startsWith("*/")
				|| s.startsWith("//");
	}
	
	public static String getClassFullName(File srcFile, List<String> srcList) {
		String clsName = srcFile.getName().replace(".java", "");
		String packageName = "";
		for(String s : srcList) {
			if(s.startsWith("package")) {
				packageName = s.substring(8).replaceAll(";", "").replaceAll(" ", "");
			}
		}
		return packageName + "." + clsName;
	}

	@Override
	public void onPrepare() {
		spectrumPath = super.projectPath + File.separator + "gzoltar_output" + File.separator + super.projectId + File.separator + super.bugId;
		spectraPath = spectrumPath + File.separator + "spectra";
		matrixPath = spectrumPath + File.separator + "matrix";
		lithiumOutputPath = super.projectPath + File.separator + "lithium_output";
	}
}
