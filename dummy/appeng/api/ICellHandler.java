package appeng.api;

import net.minecraft.item.ItemStack;
import appeng.api.me.util.IMEInventoryHandler;

public interface ICellHandler {
	
	boolean isCell( ItemStack is );
	
	IMEInventoryHandler getCellHandler( ItemStack is );
	
}
