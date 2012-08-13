package net.minecraft.src.buildcraft.krapht.forestry;

import net.minecraft.src.ItemStack;
import net.minecraft.src.TileEntity;
import net.minecraft.src.krapht.ItemIdentifier;

public interface IForestryProxy {
	
	public abstract boolean forestryEnabled();

	public abstract boolean isBee(ItemStack item);
	
	public abstract boolean isBee(ItemIdentifier item);
	
	public abstract boolean isAnalysedBee(ItemStack item);
	
	public abstract boolean isAnalysedBee(ItemIdentifier item);
	
	public abstract boolean isTileAnalyser(TileEntity tile);
}
