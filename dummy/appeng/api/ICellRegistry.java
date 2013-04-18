package appeng.api;

import net.minecraft.item.ItemStack;
import appeng.api.me.util.IMEInventoryHandler;

public interface ICellRegistry {
	
	void addCellHandler( ICellHandler h );
	
	boolean isCellHandled( ItemStack is );
	IMEInventoryHandler getHandlerForCell( ItemStack is );
	
}
