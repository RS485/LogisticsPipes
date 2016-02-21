package logisticspipes.utils;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

public class AdjacentTile {

	public TileEntity tile;
	public EnumFacing orientation;

	public AdjacentTile(TileEntity tile, EnumFacing orientation) {
		this.tile = tile;
		this.orientation = orientation;
	}
}
