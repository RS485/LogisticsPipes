package logisticspipes.proxy.interfaces;

import logisticspipes.utils.SimpleInventory;
import net.minecraft.src.TileEntity;

public interface ICraftingRecipeProvider {
	public boolean canOpenGui(TileEntity tile);
	public boolean importRecipe(TileEntity tile, SimpleInventory inventory);
}
