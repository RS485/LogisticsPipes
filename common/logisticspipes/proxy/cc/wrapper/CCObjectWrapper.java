package logisticspipes.proxy.cc.wrapper;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import net.minecraft.nbt.NBTBase;

import logisticspipes.proxy.cc.interfaces.CCCommand;
import logisticspipes.proxy.cc.interfaces.CCSecurtiyCheck;
import logisticspipes.proxy.cc.interfaces.CCType;
import logisticspipes.proxy.cc.interfaces.ILPCCTypeDefinition;
import logisticspipes.proxy.cc.objects.CCFilterInventory;
import logisticspipes.proxy.cc.objects.CCItemIdentifier;
import logisticspipes.proxy.cc.objects.CCItemIdentifierInventory;
import logisticspipes.proxy.cc.objects.CCItemIdentifierStack;
import logisticspipes.proxy.cc.objects.CCPair;
import logisticspipes.proxy.cc.objects.CCQuartet;
import logisticspipes.proxy.cc.objects.CCTriplet;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.item.ItemIdentifierInventory;
import logisticspipes.utils.item.ItemIdentifierStack;
import logisticspipes.utils.tuples.Pair;
import logisticspipes.utils.tuples.Quartet;
import logisticspipes.utils.tuples.Triplet;

public class CCObjectWrapper {
	
	private static Map<Class<?>, CCWrapperInformation> ccMapings = new HashMap<Class<?>, CCWrapperInformation>();
	private static Map<Object, CCCommandWrapper> wrappedObjects = new WeakHashMap<Object, CCCommandWrapper>();
	private static Map<Class<?>, ILPCCTypeDefinition> specialMappings = new HashMap<Class<?>, ILPCCTypeDefinition>();
	static {
		specialMappings.put(ItemIdentifier.class, new CCItemIdentifier());
		specialMappings.put(ItemIdentifierStack.class, new CCItemIdentifierStack());
		specialMappings.put(Pair.class, new CCPair());
		specialMappings.put(Triplet.class, new CCTriplet());
		specialMappings.put(Quartet.class, new CCQuartet());
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
	
	public static Object checkForAnnotations(final Object input) {
		if(input == null) return null;
		Object wrapped = input;
		if(specialMappings.containsKey(input.getClass())) {
			wrapped = specialMappings.get(input.getClass()).getTypeFor(input);
		} else if(input instanceof ItemIdentifierInventory) {
			if(((ItemIdentifierInventory)input).getInventoryStackLimit() == 1) {
				wrapped = new CCFilterInventory((ItemIdentifierInventory)input);
			} else {
				wrapped = new CCItemIdentifierInventory((ItemIdentifierInventory)input);
			}
		}
		CCWrapperInformation info = ccMapings.get(wrapped.getClass());
		if(info == null) {
			info = new CCWrapperInformation();
			String type = checkForTypeAnotation(wrapped.getClass());
			if(!type.equals("")) {
				info.isCCType = true;
				info.type = type;
				Class<?> clazz = wrapped.getClass();
				int i = 0;
				while(clazz != Object.class) {
					for(Method method: clazz.getDeclaredMethods()) {
						if(method.isAnnotationPresent(CCSecurtiyCheck.class)) {
							if(method.getParameterTypes().length > 0) throw new InternalError("Internal Excption (Code: 4)");
							info.securityMethod = method;
						}
						if(!method.isAnnotationPresent(CCCommand.class)) continue;
						for(Class<?> param:method.getParameterTypes()) {
							if(param.isPrimitive()) {
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
			ccMapings.put(wrapped.getClass(), info);
		}
		if(!info.isCCType) {
			return wrapped;	
		}
		if(wrappedObjects.containsKey(input)) {
			return wrappedObjects.get(input);
		} else {
			CCCommandWrapper wrapper = new CCCommandWrapper(info, wrapped);
			wrappedObjects.put(input, wrapper);
			return wrapper;
		}
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Object getWrappedObject(Object input) {
		if(input instanceof Object[]) {
			Object[] array = (Object[]) input;
			for(int i=0;i<array.length;i++) {
				array[i] = getWrappedObject(array[i]);
			}
			return array;
		} else if(input instanceof List) {
			List list = (List) input;
			Map map = new HashMap();
			for(int i=0;i<list.size();i++) {
				map.put((i + 1), getWrappedObject(list.get(i)));
			}
			return map;
		} else if(input instanceof Map) {
			Map oldMap = (Map) input;
			Map map = new HashMap();
			for(Object key: oldMap.keySet()) {
				map.put(getWrappedObject(key), getWrappedObject(oldMap.get(key)));
			}
			return map;
		} else if(input instanceof NBTBase) {
			try {
				return ItemIdentifier.getNBTBaseAsMap((NBTBase)input);
			} catch(Exception e) {
				e.printStackTrace();
				return null;
			}
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
}
