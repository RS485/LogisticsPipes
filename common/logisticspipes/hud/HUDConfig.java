package logisticspipes.hud;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import logisticspipes.interfaces.IHUDConfig;

public class HUDConfig implements IHUDConfig {

	private NBTTagCompound configTag;

	public HUDConfig(@Nonnull ItemStack stack) {
		this(stack.getTagCompound());
		stack.setTagCompound(configTag);
	}

	public HUDConfig(NBTTagCompound tag) {
		configTag = tag;
		if (configTag == null) {
			configTag = new NBTTagCompound();
		}

		if (configTag.hasNoTags()) {
			configTag.setBoolean("HUDChassie", true);
			configTag.setBoolean("HUDCrafting", true);
			configTag.setBoolean("HUDInvSysCon", true);
			configTag.setBoolean("HUDPowerJunction", true);
			configTag.setBoolean("HUDProvider", true);
			configTag.setBoolean("HUDSatellite", true);
		}
	}

	@Override
	public boolean isChassisHUD() {
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
	public boolean isHUDPowerLevel() {
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
	public void setChassisHUD(boolean flag) {
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
