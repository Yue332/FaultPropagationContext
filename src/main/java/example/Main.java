package example;

public class Main {

    public static void main(String[] args){
        int a = 1;
        int b = 2;
        int c = Calculator.add(a, b);
        if(c == 3){
            System.out.println("right");
        }else{
            System.out.println("wrong");
        }
    }
}
