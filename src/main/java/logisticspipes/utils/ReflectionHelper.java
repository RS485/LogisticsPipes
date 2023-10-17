package logisticspipes.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.SneakyThrows;

import logisticspipes.utils.tuples.Pair;
import logisticspipes.utils.tuples.Triplet;

public class ReflectionHelper {

	private static final Map<Triplet<Class<?>, String, List<Class<?>>>, Method> methodCache = new HashMap<>();
	private static final Map<Pair<Class<?>, String>, Field> fieldCache = new HashMap<>();

	@SuppressWarnings("unchecked")
	@SneakyThrows
	public static <T> T invokePrivateMethod(Class<?> clazz, Object target, String name, String srgName, Class<?>[] classes, Object[] objects) {
		Triplet<Class<?>, String, List<Class<?>>> key = new Triplet<>(clazz, name, Arrays.asList(classes));
		Method method = ReflectionHelper.methodCache.get(key);
		if (method == null) {
			try {
				method = clazz.getDeclaredMethod(name, classes);
			} catch (NoSuchMethodException e1) {
				try {
					method = clazz.getMethod(name, classes);
				} catch (NoSuchMethodException e2) {
					try {
						method = clazz.getDeclaredMethod(srgName, classes);
					} catch (NoSuchMethodException e3) {
						try {
							method = clazz.getMethod(srgName, classes);
						} catch (NoSuchMethodException e4) {
							method = clazz.getDeclaredMethod(name, classes);
						}
					}
				}
			}
			method.setAccessible(true);
			ReflectionHelper.methodCache.put(key, method);
		}
		Object result = method.invoke(target, objects);
		return (T) result;
	}

	@SuppressWarnings("unchecked")
	@SneakyThrows
	public static <T> T getPrivateField(Class<?> clazz, Object target, String name, String srgName) {
		Field field = getField(clazz, name, srgName);
		Object result = field.get(target);
		return (T) result;
	}

	@SneakyThrows
	public static void setPrivateField(Class<?> clazz, Object target, String name, String srgName, Object value) {
		Field field = getField(clazz, name, srgName);
		field.set(target, value);
	}

	private static Field getField(Class<?> clazz, String name, String srgName) throws NoSuchFieldException {
		Pair<Class<?>, String> key = new Pair<>(clazz, name);
		Field field = ReflectionHelper.fieldCache.get(key);
		if (field == null) {
			try {
				field = clazz.getDeclaredField(name);
			} catch (NoSuchFieldException e1) {
				try {
					field = clazz.getField(name);
				} catch (NoSuchFieldException e2) {
					try {
						field = clazz.getDeclaredField(srgName);
					} catch (NoSuchFieldException e3) {
						try {
							field = clazz.getField(srgName);
						} catch (NoSuchFieldException e4) {
							field = clazz.getDeclaredField(name);
						}
					}
				}
			}
			field.setAccessible(true);
			ReflectionHelper.fieldCache.put(key, field);
		}
		return field;
	}
}
