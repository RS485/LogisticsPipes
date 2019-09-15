package logisticspipes.proxy.interfaces;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.Inventory;

import network.rs485.logisticspipes.routing.request.Resource;

public interface FuzzyRecipeProvider extends CraftingRecipeProvider {

	void importFuzzyFlags(BlockEntity tile, Inventory inventory, Resource.Dict[] flags, Resource.Dict output);

}
