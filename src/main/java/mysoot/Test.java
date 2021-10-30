package mysoot;

public class Test {
	
	public String iftest(int a) {
		String b = "";
		if(a > 1) {
			b = "a > 1";
		}else if (a == 1) {
			b = "a = 1";
		}else {
			b = "a < 1";
		}
		a++;
		return b;
	}
	
	public String circleTest(int c) {
		String a = "";
		for(int i = 0; i < c; i ++) {
			a = a + i;
		}
		return a;
	}
}
