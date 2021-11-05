package com.mutation;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;

import com.finalmodule.base.FinalBean;
import com.finalmodule.base.IFinalProcessModule;
import com.utils.ConfigUtils;

public class Mutation2 extends FinalBean implements IFinalProcessModule {

	@Override
	public void process(Runtime runTime, StringBuilder processLog) throws Exception {
		String[] projects = super.config.getConfig(ConfigUtils.PRO_PROJECT_ID_KEY).split(",");
		String[] bugIdArr = super.config.getBugIdArr();
		String baseReportDir = System.getProperty("user.home") + File.separator + "mutationReports" + File.separator + "@:PROJECT_ID@" + File.separator + "@:BUG_ID@" + File.separator;
		String output = baseReportDir + "result.csv";
		
		for(String projectId : projects) {
			for(String bugId : bugIdArr) {
				File baseReportPath = new File(baseReportDir.replace("@:PROJECT_ID@", projectId).replace("@:BUG_ID@", bugId));
				File[] dirList = baseReportPath.listFiles(new FileFilter() {
					@Override
					public boolean accept(File pathname) {
						return pathname.isDirectory();
					}
				});
				File newestFile = getNewestReportDir(dirList);
				System.out.println("[DEBUG] 获取最新的报告文件路径：" + newestFile.getAbsolutePath());
				File outputFile = new File(output.replace("@:PROJECT_ID@", projectId).replace("@:BUG_ID@", bugId));
				FileUtils.writeStringToFile(outputFile, "lineNumber,mutatedClass,mutator,status,killingTests,succeedingTests\r\n", false);
				SAXBuilder builder = new SAXBuilder();
				InputStream ins = null;
				try {
					ins = new FileInputStream(newestFile);
					Document document = builder.build(ins);
					Element rootElement = document.getRootElement();
					List<Element> childrenElement = rootElement.getChildren();
					for(Element e : childrenElement) {
			            String status = e.getAttribute("status").getValue();
			            String mutatedClass = e.getChildText("mutatedClass");
			            String lineNumber = e.getChildText("lineNumber");
			            String mutator = e.getChildText("mutator");
			            String killingTests = e.getChildText("killingTests").replace("\r", "").replace("\n", "");
			            String succeedingTests = e.getChildText("succeedingTests").replace("\r", "").replace("\n", "");
			            FileUtils.writeStringToFile(outputFile, lineNumber + "," + mutatedClass + "," + mutator + "," + status + "," + killingTests + "," + succeedingTests + "\r\n", true);
					}
				}catch (Exception e) {
					e.printStackTrace();
					throw e;
				}finally {
					if(ins != null) {
						ins.close();
					}
				}
			}
		}

	}

	
	private File getNewestReportDir(File[] dirList) throws Exception {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm");
		String newestName = dirList[0].getName();
		for(File f : dirList) {
			String name = f.getName();
			Date newestDate = sdf.parse(newestName);
			Date currentDate = sdf.parse(name);
			if(newestDate.before(currentDate)) {
				newestName = name;
			}
		}
		return new File(dirList[0].getParent() + File.separator + newestName + File.separator + "mutations.xml");
	}
}
