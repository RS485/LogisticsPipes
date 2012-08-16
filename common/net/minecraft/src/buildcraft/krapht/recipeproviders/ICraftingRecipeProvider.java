package net.minecraft.src.buildcraft.krapht.recipeproviders;

import net.minecraft.src.TileEntity;
import net.minecraft.src.krapht.SimpleInventory;

public interface ICraftingRecipeProvider {
	public boolean canOpenGui(TileEntity tile);
	public boolean importRecipe(TileEntity tile, SimpleInventory inventory);
}
