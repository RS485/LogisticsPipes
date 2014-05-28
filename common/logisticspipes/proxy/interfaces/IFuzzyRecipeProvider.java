package logisticspipes.proxy.interfaces;

import logisticspipes.utils.item.ItemIdentifierInventory;
import net.minecraft.tileentity.TileEntity;

public interface IFuzzyRecipeProvider extends ICraftingRecipeProvider {
	public boolean importFuzzyFlags(TileEntity tile, ItemIdentifierInventory inventory, int[] flags);
}
