package logisticspipes.utils;

import logisticspipes.proxy.SimpleServiceLocator;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.tileentity.TileEntityChest;
import buildcraft.api.inventory.ISpecialInventory;
import buildcraft.core.inventory.ITransactor;
import buildcraft.core.inventory.InventoryWrapper;
import buildcraft.core.inventory.TransactorSimple;
import buildcraft.core.inventory.TransactorSpecial;

public class InventoryHelper {
	//BC getInventory with fixed doublechest halves ordering.
	public static IInventory getInventory(IInventory inv) {
		if (inv instanceof TileEntityChest) {
			TileEntityChest chest = (TileEntityChest) inv;
			
			TileEntityChest adjacent = null;
			
			if (chest.adjacentChestXNeg != null){
				adjacent = chest.adjacentChestXNeg;  
			}
			
			if (chest.adjacentChestXPos != null){
				adjacent = chest.adjacentChestXPos;  
			}
			
			if (chest.adjacentChestZNeg != null){
				adjacent = chest.adjacentChestZNeg;  
			}
			
			if (chest.adjacentChestZPosition != null){
				adjacent = chest.adjacentChestZPosition;  
			}
			
			if (adjacent != null){
				return new InventoryLargeChestLogistics("", inv, adjacent);
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

		else if (object instanceof net.minecraftforge.common.ISidedInventory)
			return new TransactorSimple(InventoryWrapper.getWrappedInventory(object));

		else if (object instanceof IInventory)
			return new TransactorSimple(getInventory((IInventory) object));

		return null;

	}
}
