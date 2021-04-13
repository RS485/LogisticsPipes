package logisticspipes.proxy.interfaces;

import net.minecraft.tileentity.TileEntity;

import logisticspipes.request.resources.DictResource;
import network.rs485.logisticspipes.inventory.IItemIdentifierInventory;

public interface IFuzzyRecipeProvider extends ICraftingRecipeProvider {

	void importFuzzyFlags(TileEntity tile, IItemIdentifierInventory inventory, DictResource[] flags, DictResource output);
}
