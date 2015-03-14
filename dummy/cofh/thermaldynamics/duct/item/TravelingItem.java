package cofh.thermaldynamics.duct.item;

import net.minecraft.item.ItemStack;
import cofh.thermaldynamics.multiblock.IMultiBlock;
import cofh.thermaldynamics.multiblock.Route;

public class TravelingItem {
	
	public byte						direction;
	public ItemStack				stack;
	
	public Object	lpRoutingInformation;
	
	public TravelingItem(ItemStack makeNormalStack, IMultiBlock tile, Route route, byte direction, byte speed) {
		// TODO Auto-generated constructor stub
	}
	
}
