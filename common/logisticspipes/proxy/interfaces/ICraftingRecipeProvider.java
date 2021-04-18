package logisticspipes.proxy.interfaces;

import net.minecraft.tileentity.TileEntity;

import network.rs485.logisticspipes.inventory.IItemIdentifierInventory;

public interface ICraftingRecipeProvider {

	boolean canOpenGui(TileEntity tile);

	boolean importRecipe(TileEntity tile, IItemIdentifierInventory inventory);

}
