package logisticspipes.utils;

import net.minecraft.src.TileEntity;
import buildcraft.api.core.Orientations;

public class AdjacentTile {
	public TileEntity tile;
	public Orientations orientation;

	public AdjacentTile(TileEntity tile, Orientations orientation){
		this.tile = tile;
		this.orientation = orientation;
	}
}