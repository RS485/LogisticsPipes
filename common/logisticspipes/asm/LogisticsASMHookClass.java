package logisticspipes.asm;


public class LogisticsASMHookClass {
	public static void callingClearedMethod() {
		throw new RuntimeException("This Method should never be called");
	}
}
