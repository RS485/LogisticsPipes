package logisticspipes.modules;

import net.minecraft.entity.player.EntityPlayer;

public abstract class LogisticsGuiModule extends LogisticsModule {

	/**
	 * 
	 * @return The gui id of the given module; 
	 */
	public abstract int getGuiHandlerID();
	
	@Override
	public boolean hasGui() {
		return true;
	}

	/**
	 * for passing args info needed for rendering of the gui that is not stored in the module itself (ie, presence of upgrades)
	 */
	public void sendGuiArgs(EntityPlayer entityplayer) {
		
	}
}
