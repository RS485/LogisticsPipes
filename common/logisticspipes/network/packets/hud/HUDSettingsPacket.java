package logisticspipes.network.packets.hud;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import logisticspipes.hud.HUDConfig;
import logisticspipes.network.abstractpackets.ModernPacket;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.entity.player.EntityPlayer;

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
		HUDConfig config = new HUDConfig(player.inventory.getStackInSlot(slot));
		switch(buttonId) {
			case 0:
				config.setHUDChassie(state);
				if(config.isHUDChassie()) {
					player.sendChatToPlayer("Enabled Chassie.");
				} else {
					player.sendChatToPlayer("Disabled Chassie.");
				}
				break;
			case 1:
				config.setHUDCrafting(state);
				if(config.isHUDCrafting()) {
					player.sendChatToPlayer("Enabled Crafting.");
				} else {
					player.sendChatToPlayer("Disabled Crafting.");
				}
				break;
			case 2:
				config.setHUDInvSysCon(state);
				if(config.isHUDInvSysCon()) {
					player.sendChatToPlayer("Enabled InvSysCon.");
				} else {
					player.sendChatToPlayer("Disabled InvSysCon.");
				}
				break;
			case 3:
				config.setHUDPowerJunction(state);
				if(config.isHUDPowerJunction()) {
					player.sendChatToPlayer("Enabled Power Junction.");
				} else {
					player.sendChatToPlayer("Disabled Power Junction.");
				}
				break;
			case 4:
				config.setHUDProvider(state);
				if(config.isHUDProvider()) {
					player.sendChatToPlayer("Enabled Provider.");
				} else {
					player.sendChatToPlayer("Disabled Provider.");
				}
				break;
			case 5:
				config.setHUDSatellite(state);
				if(config.isHUDSatellite()) {
					player.sendChatToPlayer("Enabled Satellite.");
				} else {
					player.sendChatToPlayer("Disabled Satellite.");
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

