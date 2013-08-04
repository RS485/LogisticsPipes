package logisticspipes.proxy.recipeproviders;

import logisticspipes.blocks.LogisticsSolderingTileEntity;
import logisticspipes.proxy.interfaces.ICraftingRecipeProvider;
import logisticspipes.utils.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

public class SolderingStation implements ICraftingRecipeProvider {

	@Override
	public boolean canOpenGui(TileEntity tile) {
		return tile instanceof LogisticsSolderingTileEntity;
	}

	@Override
	public boolean importRecipe(TileEntity tile, SimpleInventory inventory) {
		if (!(tile instanceof LogisticsSolderingTileEntity))
			return false;

		LogisticsSolderingTileEntity station = (LogisticsSolderingTileEntity) tile;
		ItemStack result = station.getTargetForTaget();

		if (result == null)
			return false;

		inventory.setInventorySlotContents(9, result);

		// Import
		for (int i = 0; i < station.getRecipeForTaget().length; i++) {
			if (i >= inventory.getSizeInventory() - 2) {
				break;
			}
			final ItemStack newStack = station.getRecipeForTaget()[i] == null ? null : station.getRecipeForTaget()[i].copy();
			inventory.setInventorySlotContents(i, newStack);
		}

		inventory.compact_first_9();
		
		for (int i = 0; i < inventory.getSizeInventory() - 2; i++) {
			if (inventory.getStackInSlot(i) != null) {
				continue;
			}
			inventory.setInventorySlotContents(i, new ItemStack(Item.ingotIron,1));
			break;
		}
		
		return true;
	}

}
