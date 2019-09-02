package logisticspipes.network.packets;

import java.util.Optional;
import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;

import lombok.Getter;
import lombok.Setter;

import logisticspipes.interfaces.routing.IChannelManager;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.routing.channels.ChannelInformation;
import logisticspipes.utils.StaticResolve;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

@StaticResolve
public class EditChannelPacket extends AddNewChannelPacket {

	@Getter
	@Setter
	private UUID channelIdentifier;

	public EditChannelPacket(int id) {
		super(id);
	}

	@Override
	public void readData(LPDataInput input) {
		super.readData(input);
		channelIdentifier = input.readUUID();
	}

	@Override
	public void writeData(LPDataOutput output) {
		super.writeData(output);
		output.writeUUID(channelIdentifier);
	}

	@Override
	public void processPacket(EntityPlayer player) {
		IChannelManager manager = SimpleServiceLocator.channelManagerProvider.getChannelManager(player.getEntityWorld());
		Optional<ChannelInformation> channelOpt = manager.getChannels().stream()
				.filter(chan -> chan.getChannelIdentifier().equals(channelIdentifier))
				.findFirst();
		if (channelOpt.isPresent()) {
			ChannelInformation channel = channelOpt.get();
			if (!channel.getName().equals(getName())) {
				manager.updateChannelName(channelIdentifier, getName());
			}
			if (!channel.getRights().equals(getRights()) || (channel.getResponsibleSecurityID() != null && getSecurityStationID() != null && !channel.getResponsibleSecurityID().equals(getSecurityStationID())) || channel.getResponsibleSecurityID() != getSecurityStationID()) {
				manager.updateChannelRights(channelIdentifier, getRights(), getSecurityStationID());
			}
		}
	}

	@Override
	public ModernPacket template() {
		return new EditChannelPacket(getId());
	}
}
