import com.finalmodule.CalExamUseMetallaxis;

/**
 * @program: SBTandem
 * @author: zhangziyi
 * @date: 2021/11/15
 * @description:
 **/
public class TestExamUseMetallaxis {
    public static void main(String[] args) throws Exception {
        System.setProperty("user.home", "C:\\Users\\zhangziyi\\Desktop\\1\\");
        CalExamUseMetallaxis bean = new CalExamUseMetallaxis();
        String projectPath = "C:\\Users\\zhangziyi\\Desktop\\1\\";
        String project = "Chart";
        String[] bugIdArr = new String[]{"1"};
        String[] sortFuncCustom = new String[]{};
        StringBuilder log = new StringBuilder();
        bean.process(projectPath, project, bugIdArr, sortFuncCustom, log);
    }
}
