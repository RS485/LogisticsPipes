package logisticspipes.network.packets;

import net.minecraft.entity.player.EntityPlayer;

import lombok.Getter;
import lombok.Setter;

import logisticspipes.LogisticsPipes;
import logisticspipes.config.PlayerConfig;
import logisticspipes.network.abstractpackets.ModernPacket;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

public class PlayerConfigToClientPacket extends ModernPacket {

	@Getter
	@Setter
	private PlayerConfig config;

	public PlayerConfigToClientPacket(int id) {
		super(id);
	}

	@Override
	public void readData(LPDataInput input) {
		LogisticsPipes.getClientPlayerConfig().readData(input);
	}

	@Override
	public void processPacket(EntityPlayer player) {}

	@Override
	public void writeData(LPDataOutput output) {
		config.writeData(output);
	}

	@Override
	public ModernPacket template() {
		return new PlayerConfigToClientPacket(getId());
	}
}
