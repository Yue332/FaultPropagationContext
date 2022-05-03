import example.Calculator;

public class Test2 extends Test{

    public void testCal(){
        int x = 1;
        int y = 1;
        int i = 5;
        if(i < 10){
            int result = Calculator.add(x, y);
            System.out.println(i / result);
        }
    }
}
