package logisticspipes.asm;

import java.lang.reflect.Field;

import logisticspipes.Configs;


public class LogisticsASMHookClass {
	
	public static Field toLoad;
	
	public static void callingClearedMethod() {
		throw new RuntimeException("This Method should never be called");
	}
	
	public static String getCrashReportAddition() {
		StringBuilder string = new StringBuilder();
		if(Configs.TE_PIPE_SUPPORT) {
			string.append("YOU HAVE ENABLED THE LP SUPPORT FOR TE CONDUITS.");
			string.append("\n");
			string.append("DON'T REPORT BUGS TO TE IN THIS CONFIGURATION. LP MODIFIES TE, SO THEY COULD BE CAUSED BY LP.");
			string.append("\n");
			string.append("DISABLE THE SUPPORT AND TRY TO REPRODUCE THE BUG.");
			string.append("\n");
			string.append("IF YOU CAN ONLY REPRODUCE THE BUG WITH THE SUPPORT ENABLED PLEASE REPORT IT TO LP.");
			string.append("\n\n");
		}
		return string.toString();
	}
}
