package logisticspipes.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public class ObfuscationHelper {
	public static enum NAMES {
		tagMap("tagMap", "field_74784_a", NBTTagCompound.class),
		tagList("tagList", "field_74747_a", NBTTagList.class),
		setupCameraTransform("setupCameraTransform", "func_78479_a"),
		isGamePausedServer("isGamePaused", "field_71348_o");
		String dev;
		String srg;
		Field field;
		Method method;
		Class<?> clazz;
		private NAMES(String dev, String srg, Class<?> clazz) {
			this.dev = dev;
			this.srg = srg;
			this.clazz = clazz;
		}
		private NAMES(String dev, String srg) {
			this.dev = dev;
			this.srg = srg;
		}
	}
	
	public static Field getDeclaredField(NAMES name, Class<?>... classes) throws NoSuchFieldException, SecurityException {
		if(name.field == null) {
			Class<?> clazz = name.clazz;
			if(clazz == null) {
				clazz = classes[0];
			}
			try {
				name.field = clazz.getDeclaredField(name.srg);
			} catch(Exception e) {
				name.field = clazz.getDeclaredField(name.dev);
			}
		}
		return name.field;
	}
	
	public static Method getDeclaredMethod(NAMES name, Class<?>... classes) throws NoSuchMethodException, SecurityException {
		if(name.method == null) {
			Class<?> clazz = name.clazz;
			if(clazz == null) {
				clazz = classes[0];
				classes = Arrays.copyOfRange(classes, 1, classes.length);
			}
			try {
				name.method = clazz.getDeclaredMethod(name.srg, classes);
			} catch(Exception e) {
				name.method = clazz.getDeclaredMethod(name.dev, classes);
			}
		}
		return name.method;
	}
}
