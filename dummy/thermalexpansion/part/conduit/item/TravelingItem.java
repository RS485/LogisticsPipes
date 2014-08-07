package thermalexpansion.part.conduit.item;

import net.minecraft.item.ItemStack;

public class TravelingItem {
	public ItemStack	stack;
	public int			direction;

	/*
	 * Added By LP to Store the Original Destination
	 */
	public Object routedLPInfo;
	public TravelingItem(ItemStack theItem, int xCoord, int yCoord, int zCoord, ItemRoute itemPath, int oldDirection) {}
}
