package logisticspipes.proxy.computers.wrapper;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import net.minecraft.nbt.NBTBase;

import logisticspipes.proxy.computers.interfaces.CCCommand;
import logisticspipes.proxy.computers.interfaces.CCQueued;
import logisticspipes.proxy.computers.interfaces.CCSecurtiyCheck;
import logisticspipes.proxy.computers.interfaces.CCType;
import logisticspipes.proxy.computers.interfaces.ILPCCTypeDefinition;
import logisticspipes.proxy.computers.interfaces.ILPCCTypeHolder;
import logisticspipes.proxy.computers.objects.CCFilterInventory;
import logisticspipes.proxy.computers.objects.CCFluidIdentifier;
import logisticspipes.proxy.computers.objects.CCItemIdentifier;
import logisticspipes.proxy.computers.objects.CCItemIdentifierInventory;
import logisticspipes.proxy.computers.objects.CCItemIdentifierStack;
import logisticspipes.proxy.computers.objects.CCPair;
import logisticspipes.proxy.computers.objects.CCQuartet;
import logisticspipes.proxy.computers.objects.CCResource;
import logisticspipes.proxy.computers.objects.CCTriplet;
import logisticspipes.request.resources.IResource;
import logisticspipes.utils.FluidIdentifier;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.item.ItemIdentifierInventory;
import logisticspipes.utils.item.ItemIdentifierStack;
import logisticspipes.utils.tuples.Pair;
import logisticspipes.utils.tuples.Quartet;
import logisticspipes.utils.tuples.Triplet;

public class CCObjectWrapper {

	private static Map<Class<?>, CCWrapperInformation> ccMapings = new HashMap<>();
	private static Map<Object, Object> wrappedObjects = new WeakHashMap<>();
	private static Map<Class<? extends ILPCCTypeHolder>, ILPCCTypeDefinition> specialMappings = new HashMap<>();

	static {
		CCObjectWrapper.specialMappings.put(ItemIdentifier.class, new CCItemIdentifier());
		CCObjectWrapper.specialMappings.put(ItemIdentifierStack.class, new CCItemIdentifierStack());
		CCObjectWrapper.specialMappings.put(Pair.class, new CCPair());
		CCObjectWrapper.specialMappings.put(Triplet.class, new CCTriplet());
		CCObjectWrapper.specialMappings.put(Quartet.class, new CCQuartet());
		CCObjectWrapper.specialMappings.put(IResource.class, new CCResource());
		CCObjectWrapper.specialMappings.put(FluidIdentifier.class, new CCFluidIdentifier());
	}

	private static String checkForTypeAnotation(Class<?> clazz) {
		if (clazz.getAnnotation(CCType.class) != null) {
			return clazz.getAnnotation(CCType.class).name();
		}
		String result;
		if (!clazz.getSuperclass().equals(Object.class)) {
			if (!(result = CCObjectWrapper.checkForTypeAnotation(clazz.getSuperclass())).equals("")) {
				return result;
			}
		}
		return "";
	}

	public static Object checkForAnnotations(final Object input, final ICommandWrapper wrapper) {
		if (input == null) {
			return null;
		}
		if (input instanceof ILPCCTypeHolder && ((ILPCCTypeHolder) input).getCCType() != null) {
			return ((ILPCCTypeHolder) input).getCCType();
		}
		Object wrapped = input;
		if (CCObjectWrapper.specialMappings.containsKey(input.getClass())) {
			wrapped = CCObjectWrapper.specialMappings.get(input.getClass()).getTypeFor(input);
		} else if (input instanceof ItemIdentifierInventory) {
			if (((ItemIdentifierInventory) input).getInventoryStackLimit() == 1) {
				wrapped = new CCFilterInventory((ItemIdentifierInventory) input);
			} else {
				wrapped = new CCItemIdentifierInventory((ItemIdentifierInventory) input);
			}
		}
		CCWrapperInformation info = CCObjectWrapper.getWrapperInformation(wrapped.getClass());
		if (!info.isCCType) {
			return wrapped;
		}
		if (input instanceof ILPCCTypeHolder) {
			Object finalWrapped = wrapper.getWrappedObject(info, wrapped);
			((ILPCCTypeHolder) input).setCCType(finalWrapped);
			return finalWrapped;
		} else if (CCObjectWrapper.wrappedObjects.containsKey(input)) {
			return CCObjectWrapper.wrappedObjects.get(input);
		} else {
			Object finalWrapped = wrapper.getWrappedObject(info, wrapped);
			CCObjectWrapper.wrappedObjects.put(input, finalWrapped);
			return finalWrapped;
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Object getWrappedObject(Object input, final ICommandWrapper wrapper) {
		if (input instanceof Object[]) {
			Object[] array = (Object[]) input;
			for (int i = 0; i < array.length; i++) {
				array[i] = CCObjectWrapper.getWrappedObject(array[i], wrapper);
			}
			return array;
		} else if (input instanceof List) {
			List list = (List) input;
			Map map = new HashMap();
			for (int i = 0; i < list.size(); i++) {
				map.put((i + 1), CCObjectWrapper.getWrappedObject(list.get(i), wrapper));
			}
			return map;
		} else if (input instanceof Map) {
			Map oldMap = (Map) input;
			Map map = new HashMap();
			for (Object key : oldMap.keySet()) {
				map.put(CCObjectWrapper.getWrappedObject(key, wrapper), CCObjectWrapper.getWrappedObject(oldMap.get(key), wrapper));
			}
			return map;
		} else if (input instanceof NBTBase) {
			try {
				return ItemIdentifier.getNBTBaseAsMap((NBTBase) input);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
		return CCObjectWrapper.checkForAnnotations(input, wrapper);
	}

	public static CCWrapperInformation getWrapperInformation(Class<?> wrappedClass) {
		CCWrapperInformation info = CCObjectWrapper.ccMapings.get(wrappedClass);
		if (info == null) {
			info = new CCWrapperInformation();
			String type = CCObjectWrapper.checkForTypeAnotation(wrappedClass);
			if (!type.equals("")) {
				info.isCCType = true;
				info.type = type;
				Class<?> clazz = wrappedClass;
				int i = 0;
				while (clazz != Object.class) {
					for (Method method : clazz.getDeclaredMethods()) {
						if (method.isAnnotationPresent(CCSecurtiyCheck.class)) {
							if (method.getParameterTypes().length > 0) {
								throw new InternalError("Internal Excption (Code: 4)");
							}
							info.securityMethod = method;
						}
						if (!method.isAnnotationPresent(CCCommand.class)) {
							continue;
						}
						for (Class<?> param : method.getParameterTypes()) {
							if (param.isPrimitive()) {
								throw new InternalError("Internal Excption (Code: 2)");
							}
						}
						info.commandMap.put(i, method.getName());
						info.commands.put(i, method);
						if (info.commandTypes.containsKey(method.getName())) {
							Pair<Boolean, String> pair = info.commandTypes.get(method.getName());
							if (pair.getValue1().booleanValue() ^ method.isAnnotationPresent(CCQueued.class)) {
								throw new InternalError("Internal Excption (Code: 5, " + method + ")");
							}
							if (!pair.getValue2().equals(method.getAnnotation(CCCommand.class).description())) {
								pair.setValue2("Multipurpose method. Use help() for more information");
							}
						} else {
							info.commandTypes.put(method.getName(), new Pair<>(method
									.isAnnotationPresent(CCQueued.class), method.getAnnotation(CCCommand.class)
									.description()));
						}
						i++;
					}
					clazz = clazz.getSuperclass();
				}
			}
			CCObjectWrapper.ccMapings.put(wrappedClass, info);
		}
		return info;
	}

	public static Object[] createArray(Object input) {
		if (input instanceof Object[]) {
			return (Object[]) input;
		}
		if (input == null) {
			return null;
		}
		return new Object[] { input };
	}
}
