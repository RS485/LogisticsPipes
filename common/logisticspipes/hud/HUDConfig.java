package logisticspipes.hud;

import java.lang.reflect.Field;
import java.util.HashMap;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class HUDConfig {
	
	private NBTTagCompound configTag;
	
	public HUDConfig(ItemStack stack) {
		this(stack.getTagCompound());
		stack.setTagCompound(getTag());
	}
	
	public HUDConfig(NBTTagCompound tag) {
		configTag = tag;
		if(configTag == null) {
			configTag = new NBTTagCompound("tag");
		}
		HashMap internal = new HashMap();
		Field fMap;
		try {
			fMap = NBTTagCompound.class.getDeclaredField("tagMap");
		} catch(Exception e) {
			try {
				fMap = NBTTagCompound.class.getDeclaredField("a");
			} catch (NoSuchFieldException e1) {
				e1.printStackTrace();
				return;
			} catch (SecurityException e1) {
				e1.printStackTrace();
				return;
			}
		}
		fMap.setAccessible(true);
		try {
			internal = (HashMap) fMap.get(configTag);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			return;
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			return;
		}
		if(internal.size() == 0) {
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
	
	public NBTTagCompound getTag() {
		return configTag;
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