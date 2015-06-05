package logisticspipes.proxy.computers.wrapper;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import logisticspipes.utils.tuples.Pair;

public class CCWrapperInformation {

	public boolean isCCType = false;
	public String type = "";
	public HashMap<Integer, String> commandMap = new HashMap<Integer, String>();
	public Map<Integer, Method> commands = new LinkedHashMap<Integer, Method>();
	public Map<String, Pair<Boolean, String>> commandTypes = new LinkedHashMap<String, Pair<Boolean, String>>();
	public Method securityMethod = null;
}
