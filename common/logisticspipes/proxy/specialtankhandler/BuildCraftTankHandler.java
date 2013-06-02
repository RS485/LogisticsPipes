package logisticspipes.proxy.specialtankhandler;

import java.util.ArrayList;
import java.util.List;

import logisticspipes.interfaces.ISpecialTankHandler;
import net.minecraft.tileentity.TileEntity;
import buildcraft.factory.TileTank;

public class BuildCraftTankHandler implements ISpecialTankHandler {

	@Override
	public boolean init() {
		return true;
	}

	@Override
	public boolean isType(TileEntity tile) {
		return tile instanceof TileTank;
	}

	@Override
	public List<TileEntity> getBaseTilesFor(TileEntity tile) {
		List<TileEntity> tiles = new ArrayList<TileEntity>(1);
		tiles.add(((TileTank)tile).getBottomTank());
		return tiles;
	}
}
