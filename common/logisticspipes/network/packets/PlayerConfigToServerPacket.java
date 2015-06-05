package logisticspipes.network.packets;

import java.io.IOException;

import logisticspipes.LogisticsEventListener;
import logisticspipes.config.PlayerConfig;
import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.utils.PlayerIdentifier;

import net.minecraft.entity.player.EntityPlayer;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
public class PlayerConfigToServerPacket extends ModernPacket {

	@Getter
	@Setter
	private PlayerConfig config;

	public PlayerConfigToServerPacket(int id) {
		super(id);
	}

	@Override
	public void readData(LPDataInputStream data) throws IOException {
		config = new PlayerConfig(null);
		config.readData(data);
	}

	@Override
	public void processPacket(EntityPlayer player) {
		config.applyTo(LogisticsEventListener.getPlayerConfig(PlayerIdentifier.get(player)));
	}

	@Override
	public void writeData(LPDataOutputStream data) throws IOException {
		config.writeData(data);
	}

	@Override
	public ModernPacket template() {
		return new PlayerConfigToServerPacket(getId());
	}
}
