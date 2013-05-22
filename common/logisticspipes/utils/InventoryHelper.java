package logisticspipes.utils;

import logisticspipes.proxy.SimpleServiceLocator;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.ISidedInventory;
import buildcraft.api.core.Position;
import buildcraft.api.inventory.ISpecialInventory;
import buildcraft.core.inventory.ITransactor;
import buildcraft.core.inventory.TransactorForgeSided;
import buildcraft.core.inventory.TransactorSimple;
import buildcraft.core.inventory.TransactorSpecial;
import buildcraft.core.inventory.TransactorVanillaSided;
import buildcraft.core.utils.Utils;

public class InventoryHelper {
	//BC getInventory with fixed doublechest halves ordering.
	public static IInventory getInventory(IInventory inv) {
		if (inv instanceof TileEntityChest) {
			TileEntityChest chest = (TileEntityChest) inv;
			Position pos = new Position(chest.xCoord, chest.yCoord, chest.zCoord);
			TileEntity tile;
			tile = Utils.getTile(chest.worldObj, pos, ForgeDirection.WEST);
			if (tile instanceof TileEntityChest) {
				return new InventoryLargeChestLogistics("", (IInventory) tile, inv);
			}
			tile = Utils.getTile(chest.worldObj, pos, ForgeDirection.EAST);
			if (tile instanceof TileEntityChest) {
				return new InventoryLargeChestLogistics("", inv, (IInventory) tile);
			}
			tile = Utils.getTile(chest.worldObj, pos, ForgeDirection.NORTH);
			if (tile instanceof TileEntityChest) {
				return new InventoryLargeChestLogistics("", (IInventory) tile, inv);
			}
			tile = Utils.getTile(chest.worldObj, pos, ForgeDirection.SOUTH);
			if (tile instanceof TileEntityChest) {
				return new InventoryLargeChestLogistics("", inv, (IInventory) tile);
			}
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

		else if (object instanceof net.minecraft.inventory.ISidedInventory)
		    return new TransactorVanillaSided((net.minecraft.inventory.ISidedInventory) object);

		else if (object instanceof ISidedInventory)
			return new TransactorForgeSided((ISidedInventory) object);

		else if (object instanceof IInventory)
			return new TransactorSimple(getInventory((IInventory) object));

		return null;
	}
}
