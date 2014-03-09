package logisticspipes.interfaces;

import java.util.List;

import net.minecraft.tileentity.TileEntity;

public interface ISpecialTankHandler {
	public boolean init();
	public boolean isType(TileEntity tile);
	public List<TileEntity> getBaseTilesFor(TileEntity tile);
}
