package logisticspipes.network.packets;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;

import lombok.Getter;
import lombok.Setter;

import logisticspipes.LogisticsEventListener;
import logisticspipes.config.PlayerConfig;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.utils.PlayerIdentifier;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

public class PlayerConfigToServerPacket extends ModernPacket {

	@Getter
	@Setter
	private PlayerConfig config;

	public PlayerConfigToServerPacket(int id) {
		super(id);
	}

	@Override
	public void readData(LPDataInput input) throws IOException {
		config = new PlayerConfig(null);
		config.readData(input);
	}

	@Override
	public void processPacket(EntityPlayer player) {
		config.applyTo(LogisticsEventListener.getPlayerConfig(PlayerIdentifier.get(player)));
	}

	@Override
	public void writeData(LPDataOutput output) throws IOException {
		config.writeData(output);
	}

	@Override
	public ModernPacket template() {
		return new PlayerConfigToServerPacket(getId());
	}
}
