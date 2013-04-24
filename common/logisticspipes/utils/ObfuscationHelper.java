package logisticspipes.utils;

import java.lang.reflect.Field;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public class ObfuscationHelper {
	public static enum NAMES {
		tagMap("tagMap", "field_74784_a", NBTTagCompound.class),
		tagList("tagList", "field_74747_a", NBTTagList.class);
		String dev;
		String srg;
		Field field;
		Class<?> clazz;
		private NAMES(String dev, String srg, Class<?> clazz) {
			this.dev = dev;
			this.srg = srg;
			this.clazz = clazz;
		}
	}
	
	public static Field getDeclaredField(NAMES name) throws NoSuchFieldException, SecurityException {
		if(name.field == null) {
			try {
				name.field = name.clazz.getDeclaredField(name.srg);
			} catch(Exception e) {
				name.field = name.clazz.getDeclaredField(name.dev);
			}
		}
		return name.field;
	}
}
