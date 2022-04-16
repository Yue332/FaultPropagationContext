import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;

public class Test {
    public static void main(String[] args){
        Test t = new Test();
        File f = t.getFileFromPath("C:\\Users\\44789\\Desktop\\Mockito_1\\Mockito_1", "MethodInterceptorFilterTest.java");
        System.out.println(f.getAbsolutePath());
    }

    private File getFileFromPath(String path, String fileName){
        File dir = new File(path);
        File[] files = dir.listFiles();
        if(ObjectUtils.isNotEmpty(files)){
            for (File file : files) {
                String name = file.getName();
                if(file.isDirectory()){
                    File retFile = getFileFromPath(file.getAbsolutePath(), fileName);
                    if(ObjectUtils.isEmpty(retFile)){
                        continue;
                    }
                    return retFile;
                }else if(name.equals(fileName)){
                    return file;
                }
            }
        }
        return null;
    }
}
