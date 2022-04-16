package otherutils;

import com.finalmodule.base.FinalBean;
import com.finalmodule.base.IFinalProcessModule;
import com.utils.ConfigUtils;
import com.utils.FileUtils;
import org.apache.commons.collections4.CollectionUtils;

import java.io.File;
import java.util.List;

public class ReplaceKeyWord extends FinalBean implements IFinalProcessModule {
    @Override
    public void process(Runtime runTime, StringBuilder processLog) throws Exception {
        String projectPath = super.config.getConfig(ConfigUtils.PRO_PROJECT_PATH_KEY);
        String projectId = config.getConfig(ConfigUtils.PRO_PROJECT_ID_KEY);
        String[] bugArr = config.getBugIdArr();
        for(String bug : bugArr){
            File file = new File(projectPath + File.separator + projectId + "_" + bug + File.separator +
                    "test/com/google/javascript/jscomp/DataFlowAnalysisTest.java");
            if(file.exists()){
                List<String> list = FileUtils.readLines(file, "UTF-8");
                if(CollectionUtils.isNotEmpty(list)){
                    for (String row : list) {
                        if (row.contains("for (DiGraphEdge<Instruction, Branch> _")) {
                            int idx = list.indexOf(row);
                            row = row.replace("_", "ab");
                            list.set(idx, row);
                            System.out.println("[INFO] bug " + bug + "替换完成");
                        }
                    }
                    list.forEach(row -> {
                        if (row.contains("for (DiGraphEdge<Instruction, Branch> _")) {
                            row = row.replace("_", "ab");
                            System.out.println("[INFO] bug " + bug + "替换完成");
                        }
                    });
                    StringBuilder str = new StringBuilder();
                    list.forEach(row -> str.append(row).append("\n"));
                    FileUtils.writeStringToFile(file, str.toString(), false);
                }
            }
        }
    }
}
