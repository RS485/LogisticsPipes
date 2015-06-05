package logisticspipes.utils;

import net.minecraft.nbt.NBTTagCompound;

public class FinalNBTTagCompound extends NBTTagCompound {

	private final int hashcode;

	public FinalNBTTagCompound(NBTTagCompound base) {
		super();
		tagMap = base.tagMap;
		hashcode = super.hashCode();
	}

	@Override
	public int hashCode() {
		return hashcode;
	}

	@Override
	public boolean equals(Object par1Obj) {
		if (par1Obj instanceof FinalNBTTagCompound && hashcode != par1Obj.hashCode()) {
			return false;
		}
		return super.equals(par1Obj);
	}
}
