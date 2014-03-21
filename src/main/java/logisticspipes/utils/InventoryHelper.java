package logisticspipes.utils;

import logisticspipes.proxy.SimpleServiceLocator;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.tileentity.TileEntityChest;
import buildcraft.api.inventory.ISpecialInventory;
import buildcraft.core.inventory.ITransactor;
import buildcraft.core.inventory.TransactorSimple;
import buildcraft.core.inventory.TransactorSpecial;

public class InventoryHelper {
	//BC getInventory with fixed doublechest halves ordering.
	public static IInventory getInventory(IInventory inv) {
		if (inv instanceof TileEntityChest) {
			TileEntityChest chest = (TileEntityChest) inv;
			TileEntityChest lower = null;
			TileEntityChest upper = null;
			
			if (chest.adjacentChestXNeg != null){
				upper = chest.adjacentChestXNeg;
				lower = chest;
			}
			
			if (chest.adjacentChestXPos != null){
				upper = chest;
				lower = chest.adjacentChestXPos;
			}
			
			if (chest.adjacentChestZNeg != null){
				upper = chest.adjacentChestZNeg;
				lower = chest;
			}
			
			if (chest.adjacentChestZPosition != null){
				upper = chest;
				lower = chest.adjacentChestZPosition;
			}
			
			if (lower != null && upper != null){
				return new InventoryLargeChestLogistics("", upper, lower);
			}
			return inv;
		}
		return inv;
	}

	//BC getTransactorFor using our getInventory
	public static ITransactor getTransactorFor(Object object) {
		if(object instanceof IInventory) {
			ITransactor t = SimpleServiceLocator.inventoryUtilFactory.getUtilForInv((IInventory)object, false, false, 0, 0);
			if(t != null) {
				return t;
			}
		}

		
		if (object instanceof ISpecialInventory)
			return new TransactorSpecial((ISpecialInventory) object);

		else if (object instanceof ISidedInventory)
			return new TransactorSimple((ISidedInventory) object);

		else if (object instanceof IInventory)
			return new TransactorSimple(getInventory((IInventory) object));

		return null;

	}
}
