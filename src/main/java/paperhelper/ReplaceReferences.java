package paperhelper;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ReplaceReferences {
    public static void main(String[] args) throws Exception {
        List<String> list = new ArrayList<>(42);
        for(int i = 1; i <= 42; i ++){
            list.add("[" + i + "]");
        }
        File file = new File("C:\\Users\\44789\\Desktop\\paperhelper\\test.txt");
        List<String> fileList = FileUtils.readLines(file, "utf-8");
        File outputFile = new File("C:\\Users\\44789\\Desktop\\paperhelper\\test_result.txt");
        int line = 1;
        for (String row : fileList) {
            if(line > 353 && row.contains("[") && row.contains("]")){
                for (String old : list) {
                    int number = Integer.parseInt(old.replace("[", "").replace("]", ""));
                    row = row.replace(old, "\\cite{"+number+"}");
                }
            }
            System.out.println(row);
            try {
                FileUtils.writeStringToFile(outputFile, row + "\r\n", true);
            } catch (IOException e) {
                e.printStackTrace();
            }
            line ++;
        }
    }
}
