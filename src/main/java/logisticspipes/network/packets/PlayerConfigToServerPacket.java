package logisticspipes.network.packets;

import net.minecraft.entity.player.EntityPlayer;

import lombok.Getter;
import lombok.Setter;

import logisticspipes.LogisticsPipes;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.utils.PlayerIdentifier;
import logisticspipes.utils.StaticResolve;
import network.rs485.logisticspipes.config.ClientConfiguration;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

@StaticResolve
public class PlayerConfigToServerPacket extends ModernPacket {

	@Getter
	@Setter
	private ClientConfiguration config;

	public PlayerConfigToServerPacket(int id) {
		super(id);
	}

	@Override
	public void readData(LPDataInput input) {
		config = new ClientConfiguration();
		config.read(input);
	}

	@Override
	public void processPacket(EntityPlayer player) {
		LogisticsPipes.getServerConfigManager().setClientConfiguration(
				PlayerIdentifier.get(player), config);
	}

	@Override
	public void writeData(LPDataOutput output) {
		config.write(output);
	}

	@Override
	public ModernPacket template() {
		return new PlayerConfigToServerPacket(getId());
	}
}
