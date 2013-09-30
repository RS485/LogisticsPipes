package logisticspipes.utils;

import net.minecraft.nbt.NBTTagCompound;

public class FinalNBTTagCompound extends NBTTagCompound {
	private final int hashcode;
	
	public FinalNBTTagCompound(NBTTagCompound base) {
		super(base.getName().isEmpty() ? "tag" : base.getName());
		this.tagMap = base.tagMap;
		hashcode = super.hashCode();
	}

	@Override
	public int hashCode()
	{
		return hashcode;
	}
}
