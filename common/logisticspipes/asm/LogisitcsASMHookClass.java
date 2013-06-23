package logisticspipes.asm;

import net.minecraftforge.common.ForgeDirection;
import buildcraft.api.transport.IPipeEntry;
import buildcraft.api.transport.IPipedItem;
import buildcraft.transport.TileGenericPipe;

public class LogisitcsASMHookClass {
	public static boolean continueCodeForCanReceivePipeObjects(ForgeDirection o, IPipedItem item, TileGenericPipe container) {
		if(!(container.getTile(o) instanceof IPipeEntry || container.getTile(o) instanceof TileGenericPipe) && item.getItemStack() != null && item.getItemStack().getItem() instanceof logisticspipes.interfaces.IItemAdvancedExistance && !((logisticspipes.interfaces.IItemAdvancedExistance)item.getItemStack().getItem()).canExistInNormalInventory(item.getItemStack())) return false;
		return true;
	}
}
