package logisticspipes.network.packets;

import java.io.IOException;

import logisticspipes.LogisticsPipes;
import logisticspipes.config.PlayerConfig;
import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;
import logisticspipes.network.abstractpackets.ModernPacket;

import net.minecraft.entity.player.EntityPlayer;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
public class PlayerConfigToClientPacket extends ModernPacket {

	@Getter
	@Setter
	private PlayerConfig config;

	public PlayerConfigToClientPacket(int id) {
		super(id);
	}

	@Override
	public void readData(LPDataInputStream data) throws IOException {
		LogisticsPipes.getClientPlayerConfig().readData(data);
	}

	@Override
	public void processPacket(EntityPlayer player) {}

	@Override
	public void writeData(LPDataOutputStream data) throws IOException {
		config.writeData(data);
	}

	@Override
	public ModernPacket template() {
		return new PlayerConfigToClientPacket(getId());
	}
}
