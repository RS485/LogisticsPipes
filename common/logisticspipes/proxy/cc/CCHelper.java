package logisticspipes.proxy.cc;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import logisticspipes.proxy.cc.interfaces.CCCommand;
import logisticspipes.proxy.cc.interfaces.CCType;
import logisticspipes.utils.ItemIdentifier;
import logisticspipes.utils.ItemIdentifierStack;
import logisticspipes.utils.Pair;
import logisticspipes.utils.Pair3;
import logisticspipes.utils.Pair4;

public class CCHelper {
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Object getAnswer(Object input) {
		if(input instanceof Object[]) {
			Object[] array = (Object[]) input;
			for(int i=0;i<array.length;i++) {
				array[i] = getAnswer(array[i]);
			}
			return array;
		} else if(input instanceof List) {
			List list = (List) input;
			Map map = new HashMap();
			for(int i=0;i<list.size();i++) {
				map.put((i + 1), getAnswer(list.get(i)));
			}
			return map;
		} else if(input instanceof Map) {
			Map oldMap = (Map) input;
			Map map = new HashMap();
			for(Object key: oldMap.keySet()) {
				map.put(getAnswer(key), getAnswer(oldMap.get(key)));
			}
			return map;
		} else if(input instanceof ItemIdentifier) {
			return ((ItemIdentifier)input).getId();
		} else if(input instanceof Pair4) {
			Pair4 pair = (Pair4) input;
			Map map = new HashMap();
			map.put(1,getAnswer(pair.getValue1()));
			map.put(2,getAnswer(pair.getValue2()));
			map.put(3,getAnswer(pair.getValue3()));
			map.put(4,getAnswer(pair.getValue4()));
			return map;
		} else if(input instanceof Pair3) {
			Pair3 pair = (Pair3) input;
			Map map = new HashMap();
			map.put(1,getAnswer(pair.getValue1()));
			map.put(2,getAnswer(pair.getValue2()));
			map.put(3,getAnswer(pair.getValue3()));
			return map;
		} else if(input instanceof Pair) {
			Pair pair = (Pair) input;
			Map map = new HashMap();
			map.put(1,getAnswer(pair.getValue1()));
			map.put(2,getAnswer(pair.getValue2()));
			return map;
		} else if(input instanceof ItemIdentifierStack) {
			ItemIdentifierStack mes = (ItemIdentifierStack) input;
			Map map = new HashMap();
			map.put(1,getAnswer(mes.getItem()));
			map.put(2,getAnswer(mes.stackSize));
			return map;
		}
		return checkForAnnotations(input);
	}
	
	public static Object[] createArray(Object input) {
		if(input instanceof Object[]) {
			return (Object[]) input;
		}
		if(input == null) return null;
		return new Object[]{input};
	}
	
	private static String checkForTypeAnotation(Class<?> clazz) {
		if(clazz.getAnnotation(CCType.class) != null) {
			return clazz.getAnnotation(CCType.class).name();
		}
		String result=null;
		if(!clazz.getSuperclass().equals(Object.class)) {
			if(!(result = checkForTypeAnotation(clazz.getSuperclass())).equals("")) {
				return result;
			}
		}
		return "";
	}
	
	public static Object checkForAnnotations(Object object) {
		if(object == null) return null;
		CCInfos info = ccMapings.get(object.getClass());
		if(info == null) {
			info = new CCInfos();
			String type = checkForTypeAnotation(object.getClass());
			if(!type.equals("")) {
				info.isCCType = true;
				info.type = type;
				Class<?> clazz = object.getClass();
				int i = 0;
				while(clazz != Object.class) {
					for(Method method: clazz.getDeclaredMethods()) {
						if(!method.isAnnotationPresent(CCCommand.class)) continue;
						for(Class<?> param:method.getParameterTypes()) {
							if(!param.getName().startsWith("java")) {
								throw new InternalError("Internal Excption (Code: 2)");
							}
						}
						info.commandMap.put(i, method.getName());
						info.commands.put(i, method);
						i++;
					}
					clazz = clazz.getSuperclass();
				}
			}
			ccMapings.put(object.getClass(), info);
		}
		if(!info.isCCType) {
			return object;	
		}
		return new CCCommandWrapper(info, object);
	}
	
	private static Map<Class<?>, CCInfos> ccMapings = new HashMap<Class<?>, CCInfos>();
}
