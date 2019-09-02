package logisticspipes.proxy.interfaces;

import net.minecraft.tileentity.TileEntity;

import logisticspipes.utils.item.ItemIdentifierInventory;

public interface ICraftingRecipeProvider {

	boolean canOpenGui(TileEntity tile);

	boolean importRecipe(TileEntity tile, ItemIdentifierInventory inventory);
}
