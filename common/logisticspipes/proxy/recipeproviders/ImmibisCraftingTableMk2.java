package logisticspipes.proxy.recipeproviders;

import logisticspipes.LogisticsPipes;
import logisticspipes.proxy.interfaces.ICraftingRecipeProvider;
import logisticspipes.utils.CraftingUtil;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.item.ItemIdentifierInventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.tileentity.TileEntity;

public class ImmibisCraftingTableMk2 implements ICraftingRecipeProvider {

	private Class<?> tileAutoCraftingMk2;

	public ImmibisCraftingTableMk2() throws ClassNotFoundException {
		tileAutoCraftingMk2 = Class.forName("mods.immibis.tubestuff.TileAutoCraftingMk2");
	}

	@Override
	public boolean canOpenGui(TileEntity tile) {
		if (tileAutoCraftingMk2.isInstance(tile)) {
			return true;
		}
		return false;
	}

	@Override
	public boolean importRecipe(TileEntity tile, ItemIdentifierInventory inventory) {
		try {
			if (tileAutoCraftingMk2.isInstance(tile)) {

				// Import recipeInputs

				ItemStack[][] recipe = (ItemStack[][]) tileAutoCraftingMk2.getField("recipeInputs").get(tile);
				// Not really a AutoCraftingInventory, but same content
				InventoryCrafting tempCraftingInv = new InventoryCrafting(new Container() {

					@Override
					public boolean canInteractWith(EntityPlayer entityplayer) {
						return false;
					}

					@Override
					public void onCraftMatrixChanged(IInventory par1iInventory) {}
				}, 3, 3);

				for (int i = 0; i < 9; i++) {
					if (recipe[i].length > 0) {
						tempCraftingInv.setInventorySlotContents(i, recipe[i][0]);
						inventory.setInventorySlotContents(i, recipe[i][0]);
					} else {
						inventory.clearInventorySlotContents(i);
					}
				}

				// Compact

				int slotCount = 0;
				for (int i = 0; i < 9; i++) {
					ItemStack slotStack = inventory.getStackInSlot(i);
					inventory.clearInventorySlotContents(i);
					if (slotStack != null && slotStack.getItem() != null) {
						int count = 1;
						for (int j = i + 1; j < 9; j++) {
							ItemStack tempStack = inventory.getStackInSlot(j);
							if (tempStack != null && ItemIdentifier.get(slotStack).equals(ItemIdentifier.get(tempStack))) {
								inventory.clearInventorySlotContents(j);
								count++;
							}
						}
						slotStack.stackSize = count;
						inventory.setInventorySlotContents(slotCount, slotStack);
						slotCount++;
					}
				}

				ItemStack result = null;
				for (IRecipe r : CraftingUtil.getRecipeList()) {
					if (r.matches(tempCraftingInv, tile.getWorldObj())) {
						result = r.getCraftingResult(tempCraftingInv);
						break;
					}
				}

				inventory.setInventorySlotContents(9, result);
				return true;
			}
		} catch (IllegalArgumentException e) {
			LogisticsPipes.log.fatal("Error while importing recipe from Tubestuff's AutoCraftingMk2");
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			LogisticsPipes.log.fatal("Error while importing recipe from Tubestuff's AutoCraftingMk2");
			e.printStackTrace();
		} catch (Exception e) {
			LogisticsPipes.log.error("Got a problem on ImmibisCraftingTableMk2 CraftingRecipeProvider:");
			LogisticsPipes.log.error(e.getMessage());
		}
		return false;
	}

}
