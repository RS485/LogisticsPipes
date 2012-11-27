package logisticspipes.proxy.specialinventoryhandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;
import net.minecraft.src.TileEntity;

import logisticspipes.interfaces.ISpecialInventoryHandler;
import logisticspipes.logisticspipes.SidedInventoryAdapter;
import logisticspipes.utils.ItemIdentifier;
import logisticspipes.utils.SimpleInventory;

public class SpecialInventoryHandler {
	
	private List<ISpecialInventoryHandler> handler = new ArrayList<ISpecialInventoryHandler>();
	
	public void registerHandler(ISpecialInventoryHandler invHandler) {
		if(invHandler.init()) {
			handler.add(invHandler);
		}
	}
	
	private TileEntity getTileEntityFromInventory(IInventory inv) {
		if(inv instanceof TileEntity) {
			return (TileEntity) inv;
		} else if(inv instanceof SidedInventoryAdapter) {
			if(((SidedInventoryAdapter) inv)._sidedInventory instanceof TileEntity) {
				return (TileEntity) ((SidedInventoryAdapter) inv)._sidedInventory;
			}
		}
		return null;
	}
	
	public boolean isSpecialType(IInventory inv) {
		TileEntity tile = getTileEntityFromInventory(inv);
		if(tile == null) return false;
		for(ISpecialInventoryHandler invHandler:handler) {
			if(invHandler.isType(tile)) {
				return true;
			}
		}
		return false;
	}

	public int roomForItem(IInventory _inventory, ItemIdentifier item) {
		TileEntity tile = getTileEntityFromInventory(_inventory);
		if(tile == null) return 0;
		for(ISpecialInventoryHandler invHandler:handler) {
			if(invHandler.isType(tile)) {
				return invHandler.roomForItem(tile, item);
			}
		}
		return 0;
	}

	public ItemStack getSingleItem(IInventory _inventory, ItemIdentifier item) {
		TileEntity tile = getTileEntityFromInventory(_inventory);
		if(tile == null) return null;
		for(ISpecialInventoryHandler invHandler:handler) {
			if(invHandler.isType(tile)) {
				return invHandler.getSingleItem(tile, item);
			}
		}
		return null;
	}

	public boolean containsItem(IInventory _inventory, ItemIdentifier item) {
		TileEntity tile = getTileEntityFromInventory(_inventory);
		if(tile == null) return false;
		for(ISpecialInventoryHandler invHandler:handler) {
			if(invHandler.isType(tile)) {
				return invHandler.containsItem(tile, item);
			}
		}
		return false;
	}

	public HashMap<ItemIdentifier, Integer> getItemsAndCount(IInventory _inventory) {
		TileEntity tile = getTileEntityFromInventory(_inventory);
		if(tile == null) return new HashMap<ItemIdentifier, Integer>();
		for(ISpecialInventoryHandler invHandler:handler) {
			if(invHandler.isType(tile)) {
				return invHandler.getItemsAndCount(tile);
			}
		}
		return new HashMap<ItemIdentifier, Integer>();
	}
}
