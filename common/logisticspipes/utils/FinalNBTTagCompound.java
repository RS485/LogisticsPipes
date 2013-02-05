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
		super(base.getName());
		try {
			Field fMap;
			try {
				fMap = NBTTagCompound.class.getDeclaredField("tagMap");
			} catch(Exception e) {
				fMap = NBTTagCompound.class.getDeclaredField("a");
			}
			fMap.setAccessible(true);
			HashMap<String, NBTBase> source = (HashMap<String, NBTBase>) fMap.get(base);

			Iterator<Entry<String, NBTBase>> var2 = source.entrySet().iterator();
			while (var2.hasNext())
			{
				Entry<String, NBTBase> var3 = var2.next();
				this.setTag(var3.getKey(), var3.getValue().copy());
			}
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
