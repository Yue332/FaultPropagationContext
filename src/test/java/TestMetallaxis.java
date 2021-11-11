import com.finalmodule.MetallaxisSuspValue;
import com.utils.AllMiddleParams;
import com.utils.ConfigUtils;
import com.utils.MiddleParams;
import com.utils.cal.IAnalysisFunc;
import com.utils.cal.TFuncRegister;
import com.utils.cal.func.ER5c;
import com.utils.cal.func.Euclid;
import com.utils.cal.func.Ochiai;
import com.utils.cal.func.Tarantula;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @program: SBTandem
 * @author: zhangziyi
 * @date: 2021/11/5
 * @description:
 **/
public class TestMetallaxis {
    public static void main(String[] args)throws Exception{
        System.setProperty("user.home", "C:\\Users\\zhangziyi\\Desktop\\1\\");
        String header = "element,Euclid\r\n";
        String[] funcArr = new String[]{"com.utils.cal.func.ER5c"};
        String project = "Chart";
        String[] bugArr = new String[]{"1"};
        int passCount = 354;
        int failCount = 2;
        MetallaxisSuspValue bean = new MetallaxisSuspValue();
        LinkedHashMap<String, IAnalysisFunc> funcMap = new LinkedHashMap<>(2);
        funcMap.put("com.utils.cal.func.ER5c", new ER5c());
//        funcMap.put("com.utils.cal.func.Ochiai", new Ochiai());
        Map<String, Map<String, BigDecimal>> outputMap = new HashMap<>();
        for(String bug : bugArr){
            String middleParamsFilePath = System.getProperty("user.home") + File.separator + "mutationReports" + File.separator +
                    project + File.separator + bug + File.separator + "为了后续计算的中间变量的值.csv";
            List<MiddleParams> middleParamList = new AllMiddleParams(middleParamsFilePath).getMiddleParams();
            double[][] martix = bean.getMartix(middleParamList, passCount, failCount, project, bug);

            for (Map.Entry<String, IAnalysisFunc> entry : funcMap.entrySet()){
                IAnalysisFunc analysisFunc = entry.getValue();
                bean.processOne(project, bug, outputMap, analysisFunc, middleParamList, martix);
            }

            String outputFilePath = System.getProperty("user.home") + File.separator + "mutationReports" + File.separator +
                    project + File.separator + project + "-" + bug + "-MetallaxisSuspValue.csv";
            File outputFile = new File(outputFilePath);
            FileUtils.writeStringToFile(outputFile, header, "utf-8", false);
            StringBuilder finalResult = new StringBuilder();
            for (Map.Entry<String, Map<String, BigDecimal>> entry : outputMap.entrySet()){
                String element = entry.getKey();
                Map<String, BigDecimal> funcScore = entry.getValue();
                StringBuilder score = new StringBuilder(element).append(",");
                funcMap.forEach((key, value) -> score.append(funcScore.get(value.getName()).toPlainString()).append(","));
                finalResult.append(score.substring(0, score.length() - 1)).append("\r\n");
            }
            FileUtils.writeStringToFile(outputFile, finalResult.toString(), "utf-8", true);
        }

    }
}
