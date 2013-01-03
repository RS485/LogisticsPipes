package logisticspipes.interfaces;

import net.minecraft.tileentity.TileEntity;

public interface ISpecialInventoryHandler {
	public boolean init();
	public boolean isType(TileEntity tile);
	public IInventoryUtil getUtilForTile(TileEntity tile, boolean hideOnePerStack, boolean hideOne, int cropStart, int cropEnd);
}
