package logisticspipes.datafixer;

import javax.annotation.Nonnull;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.datafix.FixTypes;
import net.minecraft.util.datafix.IFixableData;

import logisticspipes.blocks.BlockDummy;

public class DataFixerSolidBlockItems implements IFixableData {

	public static final FixTypes TYPE = FixTypes.ITEM_INSTANCE;
	public static final int VERSION = 1;

	@Override
	public int getFixVersion() {
		return VERSION;
	}

	@Nonnull
	@Override
	public NBTTagCompound fixTagCompound(NBTTagCompound compound) {
		if (
				!compound.getString("id").equals("logisticspipes:solid_block") &&
						!compound.getString("id").equals("logisticspipes:tile.logisticssolidblock")
		) return compound;

		int meta = compound.getShort("Damage");
		compound.removeTag("Damage");
		compound.setString("id", BlockDummy.updateItemMap.get(meta).getRegistryName().toString());

		return compound;
	}
}
