package logisticspipes.asm;

public class LogisticsASMHookClass {
	
	public static void callingClearedMethod() {
		throw new RuntimeException("This Method should never be called");
	}
	
	public static String getCrashReportAddition() {
		StringBuilder string = new StringBuilder();
		return string.toString();
	}
}
