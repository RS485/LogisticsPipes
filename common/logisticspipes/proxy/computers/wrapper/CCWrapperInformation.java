package logisticspipes.proxy.computers.wrapper;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import logisticspipes.utils.tuples.Pair;

public class CCWrapperInformation {

	public boolean isCCType = false;
	public String type = "";
	public HashMap<Integer, String> commandMap = new HashMap<>();
	public Map<Integer, Method> commands = new LinkedHashMap<>();
	public Map<String, Pair<Boolean, String>> commandTypes = new LinkedHashMap<>();
	public Method securityMethod = null;
}
