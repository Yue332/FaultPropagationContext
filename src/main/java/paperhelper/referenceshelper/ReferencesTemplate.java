package paperhelper.referenceshelper;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import paperhelper.ReferencesHelper;
import paperhelper.utils.PageHelperUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface ReferencesTemplate {

    static ReferencesTemplate getInstanceByRow(String row, int index)throws Exception{
        //取@和第一个{中间的内容
        int startIdx = 1;
        int endIdx = row.indexOf("{");
        String type = row.substring(startIdx, endIdx).toLowerCase();
        System.out.println("识别为：" + type);
        //去掉最外层的大括号{}
        String newRow = row.substring(endIdx + 1, row.length() - 1);
        String[] array = newRow.split(",");
        Map<String, String> beanMap = new HashMap<>();
        for (int i = 0; i < array.length; i++) {
            String attr = array[i];
            if(!attr.contains("=")){
                continue;
            }
            String[] attrArray = attr.split("=");
            String val = attrArray[1].trim();
            StringBuilder builder = new StringBuilder(val);
            String realVal = PageHelperUtils.getValueFromArray(array, i + 1, builder);
            beanMap.put(attrArray[0].trim(), realVal.substring(1, realVal.length() - 1));

        }
        ReferencesTemplate template;
        switch (type){
            case ReferencesHelper
                    .TYPE_ARTICLE:
                template = new ArticleTemplate();
                break;
            case ReferencesHelper.TYPE_INPROCEEDINGS:
                template = new InproceedingsTemplate();
                break;
            default:
                throw new Exception("类型不对！");
        }
        BeanUtils.populate(template, beanMap);
        template.setIndex(index);
        return template;
    }

    void setIndex(int idx);

    int getIndex();

    String getType();

    default void generated(File outputFile)throws Exception{
        File file = new File("C:\\Users\\44789\\Desktop\\paperhelper\\template\\" + getType() + ".txt");
        List<String> list = FileUtils.readLines(file, "utf-8");
        StringBuilder builder = new StringBuilder();
        list.forEach(row -> {
            builder.append(row).append("\r\n");
        });
        String str = builder.toString();
        Map<String, String> map = BeanUtils.describe(this);
        for (Map.Entry<String, String> entry : map.entrySet()){
            String key = entry.getKey();
            String value = StringUtils.isEmpty(entry.getValue()) ? "" : entry.getValue();
            str = str.replace("@:" + key + "@", value);
        }
        FileUtils.writeStringToFile(outputFile, str + "\r\n", this.getIndex() != 1);
    }
}
