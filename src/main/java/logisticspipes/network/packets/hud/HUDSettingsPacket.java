package logisticspipes.network.packets.hud;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentTranslation;

import lombok.Getter;
import lombok.Setter;

import logisticspipes.LPItems;
import logisticspipes.hud.HUDConfig;
import logisticspipes.interfaces.IHUDConfig;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.utils.StaticResolve;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

@StaticResolve
public class HUDSettingsPacket extends ModernPacket {

	@Getter
	@Setter
	private int buttonId;

	@Getter
	@Setter
	private boolean state;

	@Getter
	@Setter
	private int slot;

	public HUDSettingsPacket(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new HUDSettingsPacket(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		final ItemStack equipment = player.inventory.getStackInSlot(slot);
		if (equipment.getItem() != LPItems.hudGlasses) return;
		IHUDConfig config = new HUDConfig(equipment);
		switch (buttonId) {
			case 0:
				config.setChassisHUD(state);
				if (config.isChassisHUD()) {
					player.sendMessage(new TextComponentTranslation("lp.hud.config.chassie.enabled"));
				} else {
					player.sendMessage(new TextComponentTranslation("lp.hud.config.chassie.disabled"));
				}
				break;
			case 1:
				config.setHUDCrafting(state);
				if (config.isHUDCrafting()) {
					player.sendMessage(new TextComponentTranslation("lp.hud.config.crafting.enabled"));
				} else {
					player.sendMessage(new TextComponentTranslation("lp.hud.config.crafting.disabled"));
				}
				break;
			case 2:
				config.setHUDInvSysCon(state);
				if (config.isHUDInvSysCon()) {
					player.sendMessage(new TextComponentTranslation("lp.hud.config.invsyscon.enabled"));
				} else {
					player.sendMessage(new TextComponentTranslation("lp.hud.config.invsyscon.disabled"));
				}
				break;
			case 3:
				config.setHUDPowerJunction(state);
				if (config.isHUDPowerLevel()) {
					player.sendMessage(new TextComponentTranslation("lp.hud.config.powerjunction.enabled"));
				} else {
					player.sendMessage(new TextComponentTranslation("lp.hud.config.powerjunction.disabled"));
				}
				break;
			case 4:
				config.setHUDProvider(state);
				if (config.isHUDProvider()) {
					player.sendMessage(new TextComponentTranslation("lp.hud.config.provider.enabled"));
				} else {
					player.sendMessage(new TextComponentTranslation("lp.hud.config.provider.disabled"));
				}
				break;
			case 5:
				config.setHUDSatellite(state);
				if (config.isHUDSatellite()) {
					player.sendMessage(new TextComponentTranslation("lp.hud.config.satellite.enabled"));
				} else {
					player.sendMessage(new TextComponentTranslation("lp.hud.config.satellite.disabled"));
				}
				break;
		}
		if (player.inventoryContainer != null) {
			player.inventoryContainer.detectAndSendChanges();
		}
	}

	@Override
	public void readData(LPDataInput input) {
		buttonId = input.readInt();
		state = input.readBoolean();
		slot = input.readInt();
	}

	@Override
	public void writeData(LPDataOutput output) {
		output.writeInt(buttonId);
		output.writeBoolean(state);
		output.writeInt(slot);
	}
}
