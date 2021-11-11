import com.finalmodule.CalTopNWithMUSE;
import org.apache.commons.io.FileUtils;

import java.io.File;

/**
 * @program: SBTandem
 * @author: zhangziyi
 * @date: 2021/11/2
 * @description:
 **/
public class CalTopNWithMUSETest {
    public static void main(String[] args)throws Exception{
        System.setProperty("user.home", "C:\\Users\\zhangziyi\\Desktop\\1\\");
        CalTopNWithMUSE cal = new CalTopNWithMUSE();
        File outputFile = new File("C:\\Users\\zhangziyi\\Desktop\\1\\TestTop10.csv");
        FileUtils.writeStringToFile(outputFile, "bugId,function,Top-N,contained,total_lines\r\n", "utf-8", false);
        String projectPath = "C:\\Users\\zhangziyi\\Desktop\\1\\Chart_1";
        String project = "Chart";
        String bug = "1";
        int top = 10;
        cal.processOneBug(outputFile, projectPath, project, bug, top);
    }
}
