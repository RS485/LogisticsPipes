package logisticspipes.proxy.interfaces;

import logisticspipes.utils.item.ItemIdentifierInventory;

import net.minecraft.tileentity.TileEntity;

public interface ICraftingRecipeProvider {

	boolean canOpenGui(TileEntity tile);

	boolean importRecipe(TileEntity tile, ItemIdentifierInventory inventory);
}
