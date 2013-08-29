package logisticspipes.hud;

import logisticspipes.interfaces.IHUDConfig;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class HUDConfig implements IHUDConfig {
	
	private NBTTagCompound configTag;
	
	public HUDConfig(ItemStack stack) {
		this(stack.getTagCompound());
		stack.setTagCompound(configTag);
	}
	
	public HUDConfig(NBTTagCompound tag) {
		configTag = tag;
		if(configTag == null) {
			configTag = new NBTTagCompound("tag");
		}
		
		if(configTag.hasNoTags()) {
			configTag.setBoolean("HUDChassie", true);
			configTag.setBoolean("HUDCrafting", true);
			configTag.setBoolean("HUDInvSysCon", true);
			configTag.setBoolean("HUDPowerJunction", true);
			configTag.setBoolean("HUDProvider", true);
			configTag.setBoolean("HUDSatellite", true);
		}
	}
	
	public boolean isHUDChassie() {
		return configTag.getBoolean("HUDChassie");
	}
	
	public boolean isHUDCrafting() {
		return configTag.getBoolean("HUDCrafting");
	}
	
	public boolean isHUDInvSysCon() {
		return configTag.getBoolean("HUDInvSysCon");
	}
	
	public boolean isHUDPowerJunction() {
		return configTag.getBoolean("HUDPowerJunction");
	}
	
	public boolean isHUDProvider() {
		return configTag.getBoolean("HUDProvider");
	}
	
	public boolean isHUDSatellite() {
		return configTag.getBoolean("HUDSatellite");
	}
	
	public void setHUDChassie(boolean flag) {
		configTag.setBoolean("HUDChassie", flag);
	}
	
	public void setHUDCrafting(boolean flag) {
		configTag.setBoolean("HUDCrafting", flag);
	}
	
	public void setHUDInvSysCon(boolean flag) {
		configTag.setBoolean("HUDInvSysCon", flag);
	}
	
	public void setHUDPowerJunction(boolean flag) {
		configTag.setBoolean("HUDPowerJunction", flag);
	}
	
	public void setHUDProvider(boolean flag) {
		configTag.setBoolean("HUDProvider", flag);
	}
	
	public void setHUDSatellite(boolean flag) {
		configTag.setBoolean("HUDSatellite", flag);
	}
}