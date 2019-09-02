package logisticspipes.proxy.interfaces;

import net.minecraft.tileentity.TileEntity;

import logisticspipes.request.resources.DictResource;
import logisticspipes.utils.item.ItemIdentifierInventory;

public interface IFuzzyRecipeProvider extends ICraftingRecipeProvider {

	void importFuzzyFlags(TileEntity tile, ItemIdentifierInventory inventory, DictResource[] flags, DictResource output);
}
