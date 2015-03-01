package cofh.thermaldynamics.ducts.item;

import logisticspipes.routing.ItemRoutingInformation;
import cofh.thermaldynamics.multiblock.IMultiBlock;
import cofh.thermaldynamics.multiblock.Route;
import net.minecraft.item.ItemStack;

public class TravelingItem {
	
	public byte						direction;
	public ItemStack				stack;
	
	public ItemRoutingInformation	lpRoutingInformation;
	
	public TravelingItem(ItemStack makeNormalStack, IMultiBlock tile, Route route, byte direction, byte speed) {
		// TODO Auto-generated constructor stub
	}
	
}
