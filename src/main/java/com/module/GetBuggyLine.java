package com.module;

import java.io.File;

import com.utils.Bean;
import com.utils.ConfigUtils;
import com.utils.Configer;
import com.utils.Utils;

public class GetBuggyLine extends Bean implements IProcessModule {
	private String outputPath;
	
	public static String GET_BUGGY_LINE_COMMAND1 = "cd @:GZOLTAR_HOME@" + File.separator + "d4j_integration";
	public static String GET_BUGGY_LINE_COMMAND2 = "./get_buggy_lines.sh @:PROJECT_ID@ @:BUG_ID@ @:OUTPUT_PATH@";
	
	public GetBuggyLine(Configer config) {
		super(config);
	}

	@Override
	public void process(Runtime runTime) throws Exception {
		File outputPath = new File(this.outputPath);
		if(!outputPath.exists()) {
			outputPath.mkdirs();
		}else {
			File csvFile = new File(this.outputPath + File.separator + super.projectId + "-" + super.bugId + ".buggy.lines");
			if(csvFile.exists()) {
				System.out.println("[INFO] �Ѵ���"+super.projectId + "-" + super.bugId + ".buggy.lines"+"�ļ�������ʹ��get_buggy_lines.sh����");
				return;
			}
		}
		String[] msg = Utils.executeCommandLine(runTime, 
				GET_BUGGY_LINE_COMMAND1.replaceAll("@:GZOLTAR_HOME@", config.getConfig(ConfigUtils.PRO_FAULT_LOCALIZATION_DATA_HOME_KEY)),
				GET_BUGGY_LINE_COMMAND2.replaceAll("@:PROJECT_ID@", super.projectId)
				.replaceAll("@:BUG_ID@", super.bugId)
				.replaceAll("@:OUTPUT_PATH@", this.outputPath));
		if(!"0".equals(msg[0])) {
			throw new Exception("[ERROR] get_buggy_lines.sh ִ��ʧ�ܣ�");
		}
	}

	@Override
	public void onPrepare() {
		this.outputPath = config.getConfig(ConfigUtils.PRO_PROJECT_PATH_KEY) + File.separator + "get_buggy_lines_" + super.projectId;
	}

}
