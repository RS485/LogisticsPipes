package logisticspipes.asm;

import net.minecraftforge.common.ForgeDirection;
import buildcraft.api.transport.IPipeConnection;
import buildcraft.transport.TileGenericPipe;
import buildcraft.transport.TravelingItem;

public class LogisitcsASMHookClass {
	public static boolean continueCodeForCanReceivePipeObjects(ForgeDirection o, TravelingItem item, TileGenericPipe container) {
		if(!(container.getTile(o) instanceof IPipeConnection || container.getTile(o) instanceof TileGenericPipe) && item.getItemStack() != null && item.getItemStack().getItem() instanceof logisticspipes.interfaces.IItemAdvancedExistance && !((logisticspipes.interfaces.IItemAdvancedExistance)item.getItemStack().getItem()).canExistInNormalInventory(item.getItemStack())) return false;
		return true;
	}
	
	public static void callingClearedMethod() {
		throw new RuntimeException("This Method should never be called");
	}
}
