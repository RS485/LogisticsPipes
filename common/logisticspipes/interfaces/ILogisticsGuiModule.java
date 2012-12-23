package logisticspipes.interfaces;

import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.NBTTagCompound;

public interface ILogisticsGuiModule extends ILogisticsModule {

	/**
	 * 
	 * @return The gui id of the given module; 
	 */
	public int getGuiHandlerID();
}
