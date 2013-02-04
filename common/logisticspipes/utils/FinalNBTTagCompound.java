package logisticspipes.utils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;

public class FinalNBTTagCompound extends NBTTagCompound {
	private final int hashcode;

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
			HashMap<String, NBTBase> source = (HashMap) fMap.get(base);

			Iterator var2 = source.keySet().iterator();
			while (var2.hasNext())
			{
				String var3 = (String)var2.next();
				this.setTag(var3, ((NBTBase)source.get(var3)).copy());
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
