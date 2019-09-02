package logisticspipes.interfaces;

import java.util.List;

import net.minecraft.tileentity.TileEntity;

public interface ISpecialTankHandler {

	boolean init();

	boolean isType(TileEntity tile);

	List<TileEntity> getBaseTilesFor(TileEntity tile);
}
