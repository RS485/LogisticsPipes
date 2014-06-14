package logisticspipes.modules;

import logisticspipes.logisticspipes.IInventoryProvider;
import logisticspipes.network.abstractguis.ModuleCoordinatesGuiProvider;
import logisticspipes.network.abstractguis.ModuleInHandGuiProvider;
import net.minecraft.entity.player.EntityPlayer;

public abstract class LogisticsGuiModule extends LogisticsModule {


	protected IInventoryProvider _invProvider;

	/**
	 * 
	 * @return The gui id of the given module; 
	 */
	public int getGuiHandlerID() {
		return -1;
	}
	
	public ModuleCoordinatesGuiProvider getPipeGuiProvider() {
		return null;
	}
	
	public ModuleInHandGuiProvider getInHandGuiProvider() {
		return null;
	}
	
	@Override
	public boolean hasGui() {
		return true;
	}

	/**
	 * for passing args info needed for rendering of the gui that is not stored in the module itself (ie, presence of upgrades)
	 */
	public void sendGuiArgs(EntityPlayer entityplayer) {
		
	}
	
	@Override 
	public final int getX() {
		if(slot>=0)
			return this._invProvider.getX();
		else 
			return 0;
	}
	@Override 
	public final int getY() {
		if(slot>=0)
			return this._invProvider.getY();
		else 
			return -1;
	}
	
	@Override 
	public final int getZ() {
		if(slot>=0)
			return this._invProvider.getZ();
		else 
			return -1-slot;
	}
}
