package logisticspipes.utils;

import net.minecraft.nbt.NBTTagCompound;

public class FinalNBTTagCompound extends NBTTagCompound {
	private final int hashcode;
	
	public FinalNBTTagCompound(NBTTagCompound base) {
		super();
		this.tagMap = base.tagMap;
		hashcode = super.hashCode();
	}

	@Override
	public int hashCode()
	{
		return hashcode;
	}
}
