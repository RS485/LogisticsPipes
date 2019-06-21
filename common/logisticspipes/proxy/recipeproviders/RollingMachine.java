package logisticspipes.proxy.recipeproviders;

import java.lang.reflect.Method;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import logisticspipes.LogisticsPipes;
import logisticspipes.proxy.interfaces.ICraftingRecipeProvider;
import logisticspipes.utils.item.ItemIdentifierInventory;

public class RollingMachine implements ICraftingRecipeProvider {

	private Class<?> tileRollingMachineClass;
	private Method getCraftMatrixMethod;

	public RollingMachine() throws ClassNotFoundException, NoSuchMethodException, SecurityException {
		tileRollingMachineClass = Class.forName("mods.railcraft.common.blocks.machine.alpha.TileRollingMachine");
		getCraftMatrixMethod = tileRollingMachineClass.getMethod("getCraftMatrix");
	}

	@Override
	public boolean canOpenGui(TileEntity tile) {
		return tileRollingMachineClass.isInstance(tile);
	}

	private ItemStack getResult(InventoryCrafting inventorycrafting, World world) {
		if (inventorycrafting == null) {
			return null;
		}
		try {
			Class<?> c = Class.forName("mods.railcraft.common.util.crafting.RollingMachineCraftingManager");
			Method inst = c.getMethod("getInstance");
			Object instance = inst.invoke(null);
			Method findMatchingRecipe = c.getMethod("findMatchingRecipe", InventoryCrafting.class, World.class);
			return (ItemStack) findMatchingRecipe.invoke(instance, inventorycrafting, world);
		} catch (Exception ex) {
			LogisticsPipes.log.error("getResult fail");
		}
		return null;
	}

	private InventoryCrafting getCraftMatrix(TileEntity tile) {
		try {
			return (InventoryCrafting) getCraftMatrixMethod.invoke(tile);
		} catch (Exception ex) {
			LogisticsPipes.log.error("getCraftMatrix fail");
		}
		return null;
	}

	@Override
	public boolean importRecipe(TileEntity tile, ItemIdentifierInventory inventory) {
		if (!tileRollingMachineClass.isInstance(tile)) {
			return false;
		}

		InventoryCrafting craftMatrix = getCraftMatrix(tile);
		if (craftMatrix == null) {
			return false;
		}

		ItemStack result = getResult(craftMatrix, tile.getWorld());

		if (result == null) {
			return false;
		}

		inventory.setInventorySlotContents(9, result);

		// Import
		for (int i = 0; i < craftMatrix.getSizeInventory(); i++) {
			if (i >= inventory.getSizeInventory() - 2) {
				break;
			}
			inventory.setInventorySlotContents(i, craftMatrix.getStackInSlot(i).copy());
		}

		inventory.compactFirst(9);

		return true;
	}
}
