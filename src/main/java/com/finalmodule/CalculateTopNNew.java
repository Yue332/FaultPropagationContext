package com.finalmodule;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.utils.BuggyLine;
import com.utils.DataDepNewBean;
import org.apache.commons.io.FileUtils;

import com.finalmodule.base.FinalBean;
import com.finalmodule.base.IFinalProcessModule;
import com.utils.ConfigUtils;

public class CalculateTopNNew extends FinalBean implements IFinalProcessModule {
	private String buggyLine = "@:PROJECT_PATH@" + File.separator + "get_buggy_lines_@:PROJECT_ID@" +
			File.separator + "@:PROJECT_ID@-@:BUG_ID@.buggy.lines";


	private String output = "@:PROJECT_PATH@" + File.separator + "Top@:TOP@-@:FUNC@_Context.csv";

	private String title = "projectid-bugid,function,Top-N,contained,total_lines\r\n";

	private String projectId;
	private String projectPath;
	private String[] bugIdArr;
	private String[] funcArr;

	private String dataDepen;


	@Override
	public void onPrepare() {
		super.onPrepare();
		projectId = super.config.getConfig(ConfigUtils.PRO_PROJECT_ID_KEY);
		projectPath = super.config.getConfig(ConfigUtils.PRO_PROJECT_PATH_KEY);
		bugIdArr = super.config.getBugIdArr();
		funcArr = super.config.getConfig(ConfigUtils.PRO_FUNC_KEY).split(",");

		dataDepen = projectPath + File.separator + projectId + "_" + "@:BUG_ID@" + File.separator + "gzoltar_output" + File.separator + projectId + File.separator +
				"@:BUG_ID@" + File.separator + "dataDependence" + File.separator + "new" + File.separator +
				projectId + "-@:BUG_ID@-suspValue_new-@:FUNCID@.csv";
	}

	@Override
	public void process(Runtime runTime) throws Exception {
		int top = Integer.parseInt(config.getConfig(ConfigUtils.TOP_N_KEY));
		// 遍历每个公式
		for (String func : funcArr) {
			File outputFile = new File(output.replaceAll("@:PROJECT_PATH@", projectPath)
					.replaceAll("@:TOP@", super.config.getConfig(ConfigUtils.TOP_N_KEY))
					.replaceAll("@:FUNC@", func));
			FileUtils.writeStringToFile(outputFile, title, false);

			for (String bugId : bugIdArr) {
				File buggyLineFile = new File(buggyLine.replaceAll("@:PROJECT_PATH@", projectPath)
						.replaceAll("@:PROJECT_ID@", projectId)
						.replaceAll("@:BUG_ID@", bugId));
				if (!buggyLineFile.exists()) {
					throw new Exception("[ERROR] 文件" + buggyLineFile.getAbsolutePath() + "不存在！");
				}
				List<String> tmpList = FileUtils.readLines(buggyLineFile);
				List<BuggyLine> buggyLineBeanList = BuggyLine.getBuggyLineList(tmpList);
				List<String> buggyLineList = BuggyLine.getAllElements(buggyLineBeanList);
//				for (String tmp : tmpList) {
//					String[] tmpArr = tmp.split("#");
//					String clz = tmpArr[0].replace(".java", "").replace("/", ".");
//					buggyLineList.add(clz + "#" + tmpArr[1]);
//				}

				File dataDepNewFile = new File(dataDepen.replaceAll("@:BUG_ID@", bugId)
												.replaceAll("@:FUNCID@", func));
				System.out.println("[INFO] 开始读取文件：" + dataDepNewFile.getAbsolutePath());
				if (!dataDepNewFile.exists()) {
					throw new Exception("[ERROR] 文件" + dataDepNewFile.getAbsolutePath() + "不存在！");
				}

				List<String> dataDepList = FileUtils.readLines(dataDepNewFile);
				List<DataDepNewBean> beanList = DataDepNewBean.getList(dataDepList);
				List<String> elements = DataDepNewBean.getAllElements(beanList);
				// modify by zzy 20210404 修改为从bean中取
//				for (String line : dataDepList) {
//					String[] tmp = line.split(",");
//					String[] t = tmp[0].split("#");
//					String clz = t[0];
//					String firstElement = t[1];
//					elements.add(tmp[0]);
//					String[] bugLines = tmp[1].split("/");
//					for (String bugLine : bugLines) {
//						if (bugLine.equals(firstElement)) {
//							continue;
//						}
//						elements.add(clz + "#" + bugLine);
//					}
//				}
				int totalLines = 0;
				boolean contained = false;
				for (String buggyLine : buggyLineList) {
					for (String element : elements) {
						if (buggyLine.equals(element)) {
							contained = true;
							totalLines++;
						}
					}
				}
				String data = projectId + "-" + bugId + "," + func + "," + top + "," + (contained ? "1" : "0") + "," + totalLines + "\r\n";
				FileUtils.writeStringToFile(outputFile, data, true);
			}
		}
	}
}
