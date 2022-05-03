package otherutils;

import com.finalmodule.base.FinalBean;
import com.finalmodule.base.IFinalProcessModule;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ObjectUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ReplaceEnum extends FinalBean implements IFinalProcessModule {
    @Override
    public void process(Runtime runTime, StringBuilder processLog) throws Exception {
        String projectId = "Lang";
        String[] bugIdArr = new String[]{"56","58","64"};
        String projectBasePath = "/home/yy/reduce/SBFL-Lang/";

        for (String bug : bugIdArr) {
            String projectPath = projectBasePath + projectId + "_" + bug;
            List<File> fileList = new ArrayList<>();
            getFileList(projectPath, fileList);
            if(CollectionUtils.isNotEmpty(fileList)){
                fileList.forEach(row -> System.out.println(row.getAbsolutePath()));
            }
        }

    }

    private void getFileList(String path, List<File> fileList)throws Exception{
        File dir = new File(path);
        File[] files = dir.listFiles();
        if(ObjectUtils.isNotEmpty(files)){
            for (File file : files) {
                String name = file.getName();
                if(file.isDirectory()){
                    getFileList(file.getAbsolutePath(), fileList);
                }else if(name.endsWith(".java")){
                    List<String> list = FileUtils.readLines(file, "utf-8");
                    StringBuilder builder = new StringBuilder();
                    if(CollectionUtils.isNotEmpty(list)){
                        boolean ifReplace = false;
                        for (String line : list) {
                            if(line.contains("org.apache.commons.lang.enum") && !line.contains("org.apache.commons.lang.enums")){
                                line = line.replace("org.apache.commons.lang.enum", "org.apache.commons.lang.renum");
                                ifReplace = true;
                            }
                            builder.append(line).append("\n");
                        }
                        if(ifReplace){
                            fileList.add(file);
                            FileUtils.writeStringToFile(file, builder.toString(), "utf-8", false);
                        }
                    }
                }
            }
        }
    }
}
