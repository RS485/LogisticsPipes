package logisticspipes.utils;

import java.lang.reflect.Field;

import logisticspipes.utils.ObfuscationHelper.NAMES;
import net.minecraft.nbt.NBTTagCompound;

public class FinalNBTTagCompound extends NBTTagCompound {
	private final int hashcode;
	static Field fMap = null;

	public FinalNBTTagCompound(NBTTagCompound base) {
		super(base.getName() == "" ? "tag":base.getName());
		try {
			if(fMap==null){
				fMap = ObfuscationHelper.getDeclaredField(NAMES.tagMap);
				fMap.setAccessible(true);
			}
			fMap.set(this,fMap.get(base));
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		hashcode = super.hashCode();
	}

	@Override
	public int hashCode()
	{
		return hashcode;
	}
}
