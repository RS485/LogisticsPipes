package logisticspipes.utils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import net.minecraft.tileentity.TileEntity;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.transport.TileGenericPipe;

public class ReflectionHelper {
	public static void setFinalField(Class<?> clazz, String fieldName, Object target, Object object) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		Field field = clazz.getDeclaredField(fieldName);
		field.setAccessible(true);
		
		Field modifiersField = Field.class.getDeclaredField("modifiers");
		modifiersField.setAccessible(true);
		modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
		field.set(target, object);
	}

	@SuppressWarnings("unchecked")
	public static <T> T invokePrivateMethod(Class<T> type, Class<?> clazz, Object target, String name, Class<?>[] classes, Object[] objects) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Method method;
		try {
			method = clazz.getDeclaredMethod(name, classes);
		} catch(NoSuchMethodException e1) {
			try {
				method = clazz.getMethod(name, classes);
			} catch(NoSuchMethodException e2) {
				method = clazz.getDeclaredMethod(name, classes);
			}
		}
		method.setAccessible(true);
		Object result = method.invoke(target, objects);
		return (T) result;
	}
	
	public static <T> T invokePrivateMethodCatched(Class<T> type, Class<?> clazz, Object target, String name, Class<?>[] classes, Object[] objects) {
		try {
			return invokePrivateMethod(type, clazz, target, name, classes, objects);
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> T getPrivateField(Class<T> resultClass, Class<?> objectClass, String name, Object object) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		Field field = objectClass.getDeclaredField(name);
		field.setAccessible(true);
		Object result = field.get(object);
		return (T) result;
	}
}
