package com.module;

import com.utils.Bean;
import com.utils.Configer;
import mysoot.MyMain;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.List;
import java.util.Map;

public class DataDepenceBetweenClz extends Bean implements IProcessModule{
    public DataDepenceBetweenClz(Configer config) {
        super(config);
    }

    @Override
    public void process(Runtime runTime) throws Exception {
        File oldDepPath = new File(super.projectPath + File.separator + "gzoltar_output" +
                File.separator + super.projectId + File.separator + super.bugId + File.separator +
                "dataDependence" + File.separator + "new" + File.separator);
        File[] depFiles = oldDepPath.listFiles(file -> file.isFile() && file.getName().endsWith(".csv") &&
                file.getName().startsWith(projectId + "-" + bugId + "-suspValue"));
        MyMain.setSootEnv(projectPath, projectId, bugId);
        for (File depFile : depFiles) {
            String[] fileNameArr = depFile.getName().split("-");
            String funcName = fileNameArr[fileNameArr.length - 1];
            File outputFile = new File(System.getProperty("user.home") + File.separator + "dataDepence" +
                    File.separator + "betweenClassAnalysis" + File.separator + projectId + File.separator +
                    projectId + "-" + bugId + "-" + funcName + ".csv");
            List<String> list = FileUtils.readLines(depFile, "utf-8");
            for (String line : list) {
                String[] elementArray = line.split(",")[0].split("#");
                String clzName = elementArray[0];
                String lineNumber = elementArray[1];
                List<Map<String, Integer>> betweenClzList = MyMain.analysisBetweenClz(clzName, Integer.parseInt(lineNumber));
                StringBuilder str = new StringBuilder();
                str.append(clzName).append("#").append(lineNumber).append(",");
                betweenClzList.forEach(row -> row.forEach((key, value) -> str.append(key).append("#").append(value).append("/")));
                str.append("\r\n");
                FileUtils.writeStringToFile(outputFile, str.toString(), "utf-8", true);
            }
        }
    }

    @Override
    public void onPrepare() {

    }
}
