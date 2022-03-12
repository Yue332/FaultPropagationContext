import org.apache.commons.lang3.StringUtils;

public class Test {
    public static void main(String[] args){
        String data = "org.jfree.data.KeyedObjects2D#434,0";
        String label = "";

        System.out.println(StringUtils.substringAfter(data,","));
    }
}
