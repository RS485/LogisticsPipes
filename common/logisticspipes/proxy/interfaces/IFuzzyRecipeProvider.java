package logisticspipes.proxy.interfaces;

import logisticspipes.request.resources.DictResource;
import logisticspipes.utils.item.ItemIdentifierInventory;

import net.minecraft.tileentity.TileEntity;

public interface IFuzzyRecipeProvider extends ICraftingRecipeProvider {

	void importFuzzyFlags(TileEntity tile, ItemIdentifierInventory inventory, DictResource[] flags, DictResource output);
}
