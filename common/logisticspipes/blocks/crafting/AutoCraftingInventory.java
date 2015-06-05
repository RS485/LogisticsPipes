package logisticspipes.blocks.crafting;

import logisticspipes.utils.PlayerIdentifier;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;

public class AutoCraftingInventory extends InventoryCrafting {

	public final PlayerIdentifier placedByPlayer;

	public AutoCraftingInventory(PlayerIdentifier playerID) {
		super(new Container() {

			@Override
			public boolean canInteractWith(EntityPlayer entityplayer) {
				return false;
			}

			@Override
			public void onCraftMatrixChanged(IInventory par1iInventory) {}
		}, 3, 3);
		placedByPlayer = playerID;
	}
}
