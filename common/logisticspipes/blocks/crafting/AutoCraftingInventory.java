package logisticspipes.blocks.crafting;

import java.util.UUID;

import net.minecraft.container.Container;
import net.minecraft.container.ContainerType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.Inventory;

public class AutoCraftingInventory extends CraftingInventory {

	public final UUID placedByPlayer;

	public AutoCraftingInventory(UUID playerID) {
		super(new Container(ContainerType.CRAFTING, -1) {

			@Override
			public boolean canUse(PlayerEntity player) {
				return false;
			}

			@Override
			public void onContentChanged(Inventory inventory_1) {}

		}, 3, 3);

		placedByPlayer = playerID;
	}
}
