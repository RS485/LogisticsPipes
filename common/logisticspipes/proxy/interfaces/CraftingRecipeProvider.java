package logisticspipes.proxy.interfaces;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.Inventory;

public interface CraftingRecipeProvider {

	boolean canOpenGui(BlockEntity tile);

	boolean importRecipe(BlockEntity tile, Inventory inventory);

}
