package paperhelper;

import org.apache.commons.io.FileUtils;
import paperhelper.referenceshelper.ReferencesTemplate;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/***
 *  参考文献帮助
 */
public class ReferencesHelper {
    /**
     * 会议
     */
    public static final String TYPE_INPROCEEDINGS = "inproceedings";
    /**
     * 期刊
     */
    public static final String TYPE_ARTICLE = "article";

    /**
     * 书
     */
    public static final String TYPE_BOOK = "book";

    /**
     * 预出版物
     */
    public static final String TYPE_MISC = "misc";


    public static void main(String[] args)throws Exception{
        File dealFile = new File("C:\\Users\\44789\\Desktop\\paperhelper\\test.txt");
        List<String> dealList = FileUtils.readLines(dealFile, "utf-8");
        //以@开头的
        List<String> typeList = dealList.stream().filter(row -> row.startsWith("@")).collect(Collectors.toList());
        //=}的index
        List<Integer> eqIndexList = new ArrayList<>();
        for (int i = 0; i < dealList.size(); i++) {
            String row = dealList.get(i);
            if(row.trim().equals("}")){
                eqIndexList.add(i);
            }
        }
        if(typeList.size() != eqIndexList.size()){
            throw new Exception("以@开头的和等于}数量不等！");
        }
        List<String> list = new ArrayList<>();
        for (int i = 0; i < typeList.size(); i++) {
            String row = typeList.get(i);
            int startIndex = dealList.indexOf(row);
            int endIndex = eqIndexList.get(i);
            List<String> subList = dealList.subList(startIndex, endIndex);
            StringBuilder str = new StringBuilder();
            subList.forEach(str::append);
            list.add(str.toString().replace("\r\n", ""));
        }

        File outputFile = new File("C:\\Users\\44789\\Desktop\\paperhelper\\result.txt");
        int idx = 1;
        for (String row : list){
            ReferencesTemplate template = ReferencesTemplate.getInstanceByRow(row, idx);
            template.generated(outputFile);
            idx ++;
        }
    }
}
