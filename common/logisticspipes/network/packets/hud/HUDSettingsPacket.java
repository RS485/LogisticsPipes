package logisticspipes.network.packets.hud;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentTranslation;

import lombok.Getter;
import lombok.Setter;

import logisticspipes.hud.HUDConfig;
import logisticspipes.interfaces.IHUDConfig;
import logisticspipes.network.abstractpackets.ModernPacket;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

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
		if (player.inventory.getStackInSlot(slot) == null) {
			return;
		}
		IHUDConfig config = new HUDConfig(player.inventory.getStackInSlot(slot));
		switch (buttonId) {
			case 0:
				config.setHUDChassie(state);
				if (config.isHUDChassie()) {
					player.addChatComponentMessage(new ChatComponentTranslation("lp.hud.config.chassie.enabled"));
				} else {
					player.addChatComponentMessage(new ChatComponentTranslation("lp.hud.config.chassie.disabled"));
				}
				break;
			case 1:
				config.setHUDCrafting(state);
				if (config.isHUDCrafting()) {
					player.addChatComponentMessage(new ChatComponentTranslation("lp.hud.config.crafting.enabled"));
				} else {
					player.addChatComponentMessage(new ChatComponentTranslation("lp.hud.config.crafting.disabled"));
				}
				break;
			case 2:
				config.setHUDInvSysCon(state);
				if (config.isHUDInvSysCon()) {
					player.addChatComponentMessage(new ChatComponentTranslation("lp.hud.config.invsyscon.enabled"));
				} else {
					player.addChatComponentMessage(new ChatComponentTranslation("lp.hud.config.invsyscon.disabled"));
				}
				break;
			case 3:
				config.setHUDPowerJunction(state);
				if (config.isHUDPowerLevel()) {
					player.addChatComponentMessage(new ChatComponentTranslation("lp.hud.config.powerjunction.enabled"));
				} else {
					player.addChatComponentMessage(new ChatComponentTranslation("lp.hud.config.powerjunction.disabled"));
				}
				break;
			case 4:
				config.setHUDProvider(state);
				if (config.isHUDProvider()) {
					player.addChatComponentMessage(new ChatComponentTranslation("lp.hud.config.provider.enabled"));
				} else {
					player.addChatComponentMessage(new ChatComponentTranslation("lp.hud.config.provider.disabled"));
				}
				break;
			case 5:
				config.setHUDSatellite(state);
				if (config.isHUDSatellite()) {
					player.addChatComponentMessage(new ChatComponentTranslation("lp.hud.config.satellite.enabled"));
				} else {
					player.addChatComponentMessage(new ChatComponentTranslation("lp.hud.config.satellite.disabled"));
				}
				break;
		}
		if (player.inventoryContainer != null) {
			player.inventoryContainer.detectAndSendChanges();
		}
	}

	@Override
	public void readData(LPDataInput input) throws IOException {
		buttonId = input.readInt();
		state = input.readBoolean();
		slot = input.readInt();
	}

	@Override
	public void writeData(LPDataOutput output) throws IOException {
		output.writeInt(buttonId);
		output.writeBoolean(state);
		output.writeInt(slot);
	}
}
