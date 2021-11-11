import com.utils.cal.topn.TopNCalculator;

import java.util.ArrayList;
import java.util.List;

/**
 * @program: SBTandem
 * @author: zhangziyi
 * @date: 2021/11/10
 * @description:
 **/
public class TestTopNUseMetallaxis {
    public static void main(String[] args)throws Exception{
        System.setProperty("user.home", "C:\\Users\\zhangziyi\\Desktop\\1\\");
        String projectPathBase = System.getProperty("user.home");
        List<String> funcList = new ArrayList<>();
        funcList.add("Tarantula");
        String project = "Chart";
        String[] bugArr = new String[]{"1"};
        int top = 5;
        TopNCalculator calculator = new TopNCalculator(projectPathBase, project, bugArr, funcList, top);
        calculator.calculate("C:\\Users\\zhangziyi\\Desktop\\1\\mutationReports\\@PROJECT@\\@PROJECT@-@BUG@-MetallaxisSuspValue.csv");
    }
}
