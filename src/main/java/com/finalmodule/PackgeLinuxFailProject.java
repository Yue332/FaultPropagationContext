package com.finalmodule;

import com.finalmodule.base.FinalBean;
import com.finalmodule.base.IFinalProcessModule;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * ���linuxִ�������������ʧ�ܵ���Ŀ
 */
public class PackgeLinuxFailProject extends FinalBean implements IFinalProcessModule {
    @Override
    public void process(Runtime runTime, StringBuilder processLog) throws Exception {
        File linuxFailFile = new File(System.getProperty("user.home") + File.separator +
                "fail" + File.separator + "fail.txt");

    }

    private static class LinuxFailProject{
        private String project;
        private String bug;
        public LinuxFailProject(String line){
            String[] array = line.split("#");
            this.project = array[0];
            this.bug = array[1];
        }

        public static List<LinuxFailProject> buildList(File failFile) throws IOException {
            List<String> failList = FileUtils.readLines(failFile, "utf-8");
            List<LinuxFailProject> retList = new ArrayList<>(failList.size());
            failList.stream().forEach(row -> retList.add(new LinuxFailProject(row)));
            return retList;
        }

        public String getProject() {
            return project;
        }

        public String getBug() {
            return bug;
        }
    }
}
