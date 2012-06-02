package net.minecraft.src.krapht;

import net.minecraft.src.TileEntity;
import net.minecraft.src.buildcraft.api.Orientations;

public class AdjacentTile {
	public TileEntity tile;
	public Orientations orientation;

	public AdjacentTile(TileEntity tile, Orientations orientation){
		this.tile = tile;
		this.orientation = orientation;
	}
}