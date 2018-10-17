package logisticspipes.datafixer;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.datafix.FixTypes;
import net.minecraft.util.datafix.IFixableData;

public class DataFixerSolidBlockItems implements IFixableData {

	public static final FixTypes TYPE = FixTypes.ITEM_INSTANCE;
	public static final int VERSION = 1;

	@Override
	public int getFixVersion() {
		return VERSION;
	}

	@Override
	public NBTTagCompound fixTagCompound(NBTTagCompound compound) {
		return null;
	}
}
