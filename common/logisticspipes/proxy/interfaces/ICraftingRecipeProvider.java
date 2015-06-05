package logisticspipes.proxy.interfaces;

import logisticspipes.utils.item.ItemIdentifierInventory;

import net.minecraft.tileentity.TileEntity;

public interface ICraftingRecipeProvider {

	public boolean canOpenGui(TileEntity tile);

	public boolean importRecipe(TileEntity tile, ItemIdentifierInventory inventory);
}
