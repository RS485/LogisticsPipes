package logisticspipes.interfaces.routing;

import java.util.List;
import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;

import logisticspipes.routing.channels.ChannelInformation;
import logisticspipes.utils.PlayerIdentifier;

public interface IChannelManager {

	List<ChannelInformation> getChannels();

	List<ChannelInformation> getAllowedChannels(EntityPlayer playerIdentifier);

	ChannelInformation createNewChannel(String name, PlayerIdentifier owner, ChannelInformation.AccessRights rights, UUID responsibleSecurityID);

	void updateChannelName(UUID channelIdentifier, String newName);

	void updateChannelRights(UUID channelIdentifier, ChannelInformation.AccessRights rights, UUID responsibleSecurityID);

	void removeChannel(UUID channelIdentifier);
}
