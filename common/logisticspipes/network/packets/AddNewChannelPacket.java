package logisticspipes.network.packets;

import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;

import lombok.Getter;
import lombok.Setter;

import logisticspipes.interfaces.routing.ChannelManagerProvider;
import logisticspipes.interfaces.routing.IChannelManager;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.routing.channels.ChannelInformation;
import logisticspipes.utils.PlayerIdentifier;
import logisticspipes.utils.StaticResolve;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

@StaticResolve
public class AddNewChannelPacket extends ModernPacket {

	@Getter
	@Setter
	private String name;

	@Getter
	@Setter
	private ChannelInformation.AccessRights rights;

	@Getter
	@Setter
	private UUID securityStationID;

	public AddNewChannelPacket(int id) {
		super(id);
	}

	@Override
	public void readData(LPDataInput input) {
		super.readData(input);
		name = input.readUTF();
		rights = input.readEnum(ChannelInformation.AccessRights.class);
		securityStationID = input.readUUID();
	}

	@Override
	public void writeData(LPDataOutput output) {
		super.writeData(output);
		output.writeUTF(name);
		output.writeEnum(rights);
		output.writeUUID(securityStationID);
	}

	@Override
	public void processPacket(EntityPlayer player) {
		IChannelManager manager = ChannelManagerProvider.getInstance().getChannelManager(player.getEntityWorld());
		manager.createNewChannel(name, PlayerIdentifier.get(player), rights, securityStationID);
	}

	@Override
	public ModernPacket template() {
		return new AddNewChannelPacket(getId());
	}
}
