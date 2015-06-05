package logisticspipes.utils;

import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.utils.transactor.ITransactor;
import logisticspipes.utils.transactor.TransactorSimple;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.tileentity.TileEntityChest;

import net.minecraftforge.common.util.ForgeDirection;

public class InventoryHelper {

	//BC getInventory with fixed doublechest halves ordering.
	public static IInventory getInventory(IInventory inv) {
		if (inv instanceof TileEntityChest) {
			TileEntityChest chest = (TileEntityChest) inv;
			TileEntityChest lower = null;
			TileEntityChest upper = null;

			if (chest.adjacentChestXNeg != null) {
				upper = chest.adjacentChestXNeg;
				lower = chest;
			}

			if (chest.adjacentChestXPos != null) {
				upper = chest;
				lower = chest.adjacentChestXPos;
			}

			if (chest.adjacentChestZNeg != null) {
				upper = chest.adjacentChestZNeg;
				lower = chest;
			}

			if (chest.adjacentChestZPos != null) {
				upper = chest;
				lower = chest.adjacentChestZPos;
			}

			if (lower != null && upper != null) {
				return new InventoryLargeChestLogistics("", upper, lower);
			}
			return inv;
		}
		return inv;
	}

	//BC getTransactorFor using our getInventory
	public static ITransactor getTransactorFor(Object object, ForgeDirection dir) {
		if (object instanceof IInventory) {
			ITransactor t = SimpleServiceLocator.inventoryUtilFactory.getUtilForInv((IInventory) object, dir, false, false, 0, 0);
			if (t != null) {
				return t;
			}
		}

		if (object instanceof ISidedInventory) {
			return new TransactorSimple((ISidedInventory) object);
		} else if (object instanceof IInventory) {
			return new TransactorSimple(InventoryHelper.getInventory((IInventory) object));
		}

		return null;

	}
}
