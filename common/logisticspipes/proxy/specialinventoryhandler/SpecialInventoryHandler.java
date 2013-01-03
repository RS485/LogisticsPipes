package logisticspipes.proxy.specialinventoryhandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import logisticspipes.interfaces.IInventoryUtil;
import logisticspipes.interfaces.ISpecialInventoryHandler;
import logisticspipes.logisticspipes.SidedInventoryAdapter;
import logisticspipes.utils.ItemIdentifier;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

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
	
	public IInventoryUtil getUtilForInv(IInventory inv, boolean hideOnePerStack, boolean hideOne, int cropStart, int cropEnd) {
		TileEntity tile = getTileEntityFromInventory(inv);
		if(tile == null) return null;
		for(ISpecialInventoryHandler invHandler:handler) {
			if(invHandler.isType(tile)) {
				return invHandler.getUtilForTile(tile, hideOnePerStack, hideOne, cropStart, cropEnd);
			}
		}
		return null;
	}
}
