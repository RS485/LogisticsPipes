package logisticspipes.utils;

import net.minecraft.tileentity.TileEntity;

import net.minecraftforge.common.util.ForgeDirection;

public class AdjacentTile {

	public TileEntity tile;
	public ForgeDirection orientation;

	public AdjacentTile(TileEntity tile, ForgeDirection orientation) {
		this.tile = tile;
		this.orientation = orientation;
	}
}
