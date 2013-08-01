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
import net.minecraft.util.ChatMessageComponent;

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
					player.sendChatToPlayer(ChatMessageComponent.func_111066_d("Enabled Chassie."));
				} else {
					player.sendChatToPlayer(ChatMessageComponent.func_111066_d("Disabled Chassie."));
				}
				break;
			case 1:
				config.setHUDCrafting(state);
				if(config.isHUDCrafting()) {
					player.sendChatToPlayer(ChatMessageComponent.func_111066_d("Enabled Crafting."));
				} else {
					player.sendChatToPlayer(ChatMessageComponent.func_111066_d("Disabled Crafting."));
				}
				break;
			case 2:
				config.setHUDInvSysCon(state);
				if(config.isHUDInvSysCon()) {
					player.sendChatToPlayer(ChatMessageComponent.func_111066_d("Enabled InvSysCon."));
				} else {
					player.sendChatToPlayer(ChatMessageComponent.func_111066_d("Disabled InvSysCon."));
				}
				break;
			case 3:
				config.setHUDPowerJunction(state);
				if(config.isHUDPowerJunction()) {
					player.sendChatToPlayer(ChatMessageComponent.func_111066_d("Enabled Power Junction."));
				} else {
					player.sendChatToPlayer(ChatMessageComponent.func_111066_d("Disabled Power Junction."));
				}
				break;
			case 4:
				config.setHUDProvider(state);
				if(config.isHUDProvider()) {
					player.sendChatToPlayer(ChatMessageComponent.func_111066_d("Enabled Provider."));
				} else {
					player.sendChatToPlayer(ChatMessageComponent.func_111066_d("Disabled Provider."));
				}
				break;
			case 5:
				config.setHUDSatellite(state);
				if(config.isHUDSatellite()) {
					player.sendChatToPlayer(ChatMessageComponent.func_111066_d("Enabled Satellite."));
				} else {
					player.sendChatToPlayer(ChatMessageComponent.func_111066_d("Disabled Satellite."));
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

