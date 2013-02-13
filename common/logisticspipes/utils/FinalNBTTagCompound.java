package logisticspipes.utils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;

public class FinalNBTTagCompound extends NBTTagCompound {
	private final int hashcode;

	@SuppressWarnings("unchecked")
	public FinalNBTTagCompound(NBTTagCompound base) {
		super(base.getName() == "" ? "tag":base.getName());
		try {
			Field fMap;
			try {
				fMap = NBTTagCompound.class.getDeclaredField("a");
			} catch(Exception e) {
				fMap = NBTTagCompound.class.getDeclaredField("tagMap");
			}
			fMap.setAccessible(true);
			fMap.set(this,fMap.get(base));
		} catch(Exception e) {
			e.printStackTrace();
		}
		hashcode = super.hashCode();
	}

	public int hashCode()
	{
		return hashcode;
	}
}
