package logisticspipes.proxy.interfaces;

import logisticspipes.utils.SimpleInventory;
import net.minecraft.tileentity.TileEntity;

public interface ICraftingRecipeProvider {
	public boolean canOpenGui(TileEntity tile);
	public boolean importRecipe(TileEntity tile, SimpleInventory inventory);
}
