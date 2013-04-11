package logisticspipes.proxy.cc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import logisticspipes.utils.ItemIdentifier;
import logisticspipes.utils.ItemMessage;
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
		} else if(input instanceof ItemMessage) {
			ItemMessage mes = (ItemMessage) input;
			Map map = new HashMap();
			map.put(1,getAnswer(mes.getItemIdentifier()));
			map.put(2,getAnswer(mes.amount));
			return map;
		}
		return input;
	}
	
	public static Object[] createArray(Object input) {
		if(input instanceof Object[]) {
			return (Object[]) input;
		}
		if(input == null) return null;
		return new Object[]{input};
	}
}
