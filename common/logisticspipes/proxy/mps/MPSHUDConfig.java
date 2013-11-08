package logisticspipes.proxy.mps;

import logisticspipes.interfaces.IHUDConfig;
import net.minecraft.nbt.NBTTagCompound;

public class MPSHUDConfig implements IHUDConfig {

	private NBTTagCompound configTag;
	
	public MPSHUDConfig(NBTTagCompound tag) {
		configTag = tag.getCompoundTag(LogisticsPipesHUDModule.NAME + "_Settings");
		
		if(configTag.hasNoTags()) {
			tag.setTag(LogisticsPipesHUDModule.NAME + "_Settings", configTag);
			configTag.setBoolean("HUDChassie", true);
			configTag.setBoolean("HUDCrafting", true);
			configTag.setBoolean("HUDInvSysCon", true);
			configTag.setBoolean("HUDPowerJunction", true);
			configTag.setBoolean("HUDProvider", true);
			configTag.setBoolean("HUDSatellite", true);
		}
	}
	
	@Override
	public boolean isHUDChassie() {
		return configTag.getBoolean("HUDChassie");
	}
	
	@Override
	public boolean isHUDCrafting() {
		return configTag.getBoolean("HUDCrafting");
	}
	
	@Override
	public boolean isHUDInvSysCon() {
		return configTag.getBoolean("HUDInvSysCon");
	}
	
	@Override
	public boolean isHUDPowerJunction() {
		return configTag.getBoolean("HUDPowerJunction");
	}
	
	@Override
	public boolean isHUDProvider() {
		return configTag.getBoolean("HUDProvider");
	}
	
	@Override
	public boolean isHUDSatellite() {
		return configTag.getBoolean("HUDSatellite");
	}
	
	@Override
	public void setHUDChassie(boolean flag) {
		configTag.setBoolean("HUDChassie", flag);
	}
	
	@Override
	public void setHUDCrafting(boolean flag) {
		configTag.setBoolean("HUDCrafting", flag);
	}
	
	@Override
	public void setHUDInvSysCon(boolean flag) {
		configTag.setBoolean("HUDInvSysCon", flag);
	}
	
	@Override
	public void setHUDPowerJunction(boolean flag) {
		configTag.setBoolean("HUDPowerJunction", flag);
	}
	
	@Override
	public void setHUDProvider(boolean flag) {
		configTag.setBoolean("HUDProvider", flag);
	}
	
	@Override
	public void setHUDSatellite(boolean flag) {
		configTag.setBoolean("HUDSatellite", flag);
	}
}
