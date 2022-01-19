package com.module;

import com.utils.Bean;
import com.utils.Configer;

import java.io.File;
import java.io.FilenameFilter;

/**
 * @author zzy
 * @date 2021-03-31 17:27
 */
public class DeleteOldCsv extends Bean implements IProcessModule{

    public DeleteOldCsv(Configer config) {
        super(config);
    }

    @Override
    public void process(Runtime runTime) throws Exception {
        String oldCsvPath = super.projectPath + File.separator +
                "gzoltar_output" + File.separator + super.projectId + File.separator +
                super.bugId + File.separator;
        deleteFile(oldCsvPath);

        String dataDepPath = oldCsvPath + "dataDependence" + File.separator;
        deleteFile(dataDepPath);

        String dataDepNewPath = dataDepPath + "new" + File.separator;
        deleteFile(dataDepNewPath);

        String sortPath = oldCsvPath + "sort" + File.separator;
        deleteFile(sortPath);

    }

    private void deleteFile(String oldCsvPath){
        File path = new File(oldCsvPath);
        if(!path.exists()){
            System.out.println("[INFO] 目录" + path.getAbsolutePath() + "不存在，不处理");
            return;
        }
        File[] list = path.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".csv") && (name.contains("suspValue_") || name.contains("suspValue-") );
            }
        });
        for(File f : list){
            System.out.println("[INFO] 删除文件：" + f.getAbsolutePath());
            f.delete();
        }
    }

    @Override
    public void onPrepare() {

    }
}
