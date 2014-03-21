package logisticspipes.network.packets.hud;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import logisticspipes.hud.HUDConfig;
import logisticspipes.interfaces.IHUDConfig;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.proxy.SimpleServiceLocator;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;

@Accessors(chain=true)
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
		if(player.inventory.getStackInSlot(slot) == null) return;
		IHUDConfig config;
		if(SimpleServiceLocator.mpsProxy.isMPSHand(player.inventory.getStackInSlot(slot))) {
			if(SimpleServiceLocator.mpsProxy.isMPSHelm(player.inventory.armorItemInSlot(3))) {
				config = SimpleServiceLocator.mpsProxy.getConfigFor(player.inventory.armorItemInSlot(3));
			} else {
				config = new HUDConfig(player.inventory.armorItemInSlot(3));
			}
		} else {
			config = new HUDConfig(player.inventory.getStackInSlot(slot));
		}
		switch(buttonId) {
			case 0:
				config.setHUDChassie(state);
				if(config.isHUDChassie()) {
					player.addChatMessage(new ChatComponentText("Enabled Chassie."));
				} else {
					player.addChatMessage(new ChatComponentText("Disabled Chassie."));
				}
				break;
			case 1:
				config.setHUDCrafting(state);
				if(config.isHUDCrafting()) {
					player.addChatMessage(new ChatComponentText("Enabled Crafting."));
				} else {
					player.addChatMessage(new ChatComponentText("Disabled Crafting."));
				}
				break;
			case 2:
				config.setHUDInvSysCon(state);
				if(config.isHUDInvSysCon()) {
					player.addChatMessage(new ChatComponentText("Enabled InvSysCon."));
				} else {
					player.addChatMessage(new ChatComponentText("Disabled InvSysCon."));
				}
				break;
			case 3:
				config.setHUDPowerJunction(state);
				if(config.isHUDPowerJunction()) {
					player.addChatMessage(new ChatComponentText("Enabled Power Junction."));
				} else {
					player.addChatMessage(new ChatComponentText("Disabled Power Junction."));
				}
				break;
			case 4:
				config.setHUDProvider(state);
				if(config.isHUDProvider()) {
					player.addChatMessage(new ChatComponentText("Enabled Provider."));
				} else {
					player.addChatMessage(new ChatComponentText("Disabled Provider."));
				}
				break;
			case 5:
				config.setHUDSatellite(state);
				if(config.isHUDSatellite()) {
					player.addChatMessage(new ChatComponentText("Enabled Satellite."));
				} else {
					player.addChatMessage(new ChatComponentText("Disabled Satellite."));
				}
				break;
		}
		if(player.inventoryContainer != null) {
			player.inventoryContainer.detectAndSendChanges();
		}
	}

	@Override
	public void readData(DataInputStream data) throws IOException {
		buttonId = data.readInt();
		state = data.readBoolean();
		slot = data.readInt();
	}

	@Override
	public void writeData(DataOutputStream data) throws IOException {
		data.writeInt(buttonId);
		data.writeBoolean(state);
		data.writeInt(slot);
	}
}

