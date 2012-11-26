package logisticspipes.interfaces;

import java.util.HashMap;

import logisticspipes.utils.ItemIdentifier;
import net.minecraft.src.ItemStack;
import net.minecraft.src.TileEntity;

public interface ISpecialInventoryHandler {
	public boolean init();
	public boolean isType(TileEntity tile);
	public HashMap<ItemIdentifier, Integer> getItemCount();
	public int roomForItem(TileEntity tile, ItemIdentifier item);
	public ItemStack getSingleItem(TileEntity tile, ItemIdentifier item);
	public boolean containsItem(TileEntity tile, ItemIdentifier item);
	public HashMap<ItemIdentifier, Integer> getItemsAndCount(TileEntity tile);
}
