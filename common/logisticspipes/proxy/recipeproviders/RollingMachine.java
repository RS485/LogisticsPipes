package logisticspipes.proxy.recipeproviders;

import java.lang.reflect.Method;

import logisticspipes.LogisticsPipes;
import logisticspipes.proxy.interfaces.ICraftingRecipeProvider;
import logisticspipes.utils.SimpleInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class RollingMachine implements ICraftingRecipeProvider {

	private static Class<?> tileRollingMachineClass;
	private static Method getCraftMatrixMethod;

	public static boolean load() {
		try {
			tileRollingMachineClass = Class.forName("mods.railcraft.common.blocks.machine.alpha.TileRollingMachine");
			getCraftMatrixMethod = tileRollingMachineClass.getMethod("getCraftMatrix");
		} catch (Exception ex) {
			LogisticsPipes.log.fine("Necessary classes from Railcraft were not found");
			return false;
		}
		return true;
	}

	@Override
	public boolean canOpenGui(TileEntity tile) {
		return tileRollingMachineClass.isInstance(tile);
	}

	private ItemStack getResult(InventoryCrafting inventorycrafting, World world) {
		if (inventorycrafting == null)
			return null;
		try {
			Class<?> c = Class.forName("mods.railcraft.common.util.crafting.RollingMachineCraftingManager");
			Method inst = c.getMethod("getInstance");
			Object instance = inst.invoke(null);
			Method findMatchingRecipe = c.getMethod("findMatchingRecipe", InventoryCrafting.class, World.class);
			return (ItemStack)findMatchingRecipe.invoke(instance, inventorycrafting, world);
		} catch (Exception ex) {
			LogisticsPipes.log.fine("getResult fail");
		}		
		return null;
	}

	private InventoryCrafting getCraftMatrix(TileEntity tile) {
		try {
			return (InventoryCrafting) getCraftMatrixMethod.invoke(tile);
		} catch (Exception ex) {
			LogisticsPipes.log.fine("getCraftMatrix fail");
		}		
		return null;
	}


	@Override
	public boolean importRecipe(TileEntity tile, SimpleInventory inventory) {
		if (!tileRollingMachineClass.isInstance(tile))
			return false;

		InventoryCrafting craftMatrix = getCraftMatrix(tile);
		if (craftMatrix == null)
			return false;

		ItemStack result = getResult(craftMatrix, tile.getWorldObj());

		if (result == null)
			return false;

		inventory.setInventorySlotContents(9, result);

		// Import
		for (int i = 0; i < craftMatrix.getSizeInventory(); i++) {
			if (i >= inventory.getSizeInventory() - 2) {
				break;
			}
			final ItemStack newStack = craftMatrix.getStackInSlot(i) == null ? null : craftMatrix.getStackInSlot(i).copy();
			inventory.setInventorySlotContents(i, newStack);
		}

		inventory.compact_first_9();
		
		return true;
	}
}
