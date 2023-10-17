package logisticspipes.routing.channels;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldSavedData;

import logisticspipes.LPConstants;
import logisticspipes.blocks.LogisticsSecurityTileEntity;
import logisticspipes.interfaces.routing.IChannelManager;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.gui.ChannelInformationPacket;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.security.SecuritySettings;
import logisticspipes.utils.PlayerIdentifier;

public class ChannelManager implements IChannelManager {

	private static final String DATA_NAME = LPConstants.LP_MOD_ID + "_ChannelManager_SavedData";
	private SavedData savedData;

	public ChannelManager(@Nonnull World world) {
		savedData = (SavedData) Objects.requireNonNull(world.getMapStorage()).getOrLoadData(SavedData.class, DATA_NAME);
		if (savedData == null) {
			savedData = new SavedData();
			world.getMapStorage().setData(DATA_NAME, savedData);
		}
	}

	@Override
	public List<ChannelInformation> getChannels() {
		return Collections.unmodifiableList(savedData.channels);
	}

	private boolean isChannelAllowedFor(ChannelInformation channel, EntityPlayer player) {
		switch (channel.getRights()) {
			case PUBLIC:
				return true;
			case SECURED:
				final UUID secUUID = channel.getResponsibleSecurityID();
				final LogisticsSecurityTileEntity station = SimpleServiceLocator.securityStationManager.getStation(secUUID);
				if (station != null) {
					final SecuritySettings settings = station.getSecuritySettingsForPlayer(player, false);
					if (settings != null) {
						return settings.accessRoutingChannels;
					}
				}
			case PRIVATE:
				return channel.getOwner().equals(PlayerIdentifier.get(player));
		}
		return false;
	}

	@Override
	public List<ChannelInformation> getAllowedChannels(EntityPlayer player) {
		return Collections.unmodifiableList(savedData.channels.stream().filter(channel -> isChannelAllowedFor(channel, player)).collect(Collectors.toList()));
	}

	@Override
	public ChannelInformation createNewChannel(String name, PlayerIdentifier owner, ChannelInformation.AccessRights rights, UUID responsibleSecurityID) {
		ChannelInformation channel = new ChannelInformation(name, UUID.randomUUID(), owner, rights, responsibleSecurityID);
		savedData.channels.add(channel);
		savedData.markDirty();
		sendUpdatePacketToClients(channel);
		return channel;
	}

	@Override
	public void updateChannelName(UUID channelIdentifier, String newName) {
		savedData.channels.stream().filter(channel -> channel.getChannelIdentifier().equals(channelIdentifier)).forEach(channel -> {
			channel.setName(newName);
			sendUpdatePacketToClients(channel);
		});
		savedData.markDirty();

	}

	@Override
	public void updateChannelRights(UUID channelIdentifier, ChannelInformation.AccessRights rights, UUID responsibleSecurityID) {
		savedData.channels.stream().filter(channel -> channel.getChannelIdentifier().equals(channelIdentifier)).forEach(channel -> {
			channel.setRights(rights);
			channel.setResponsibleSecurityID(responsibleSecurityID);
			sendUpdatePacketToClients(channel);
		});
		savedData.markDirty();
	}

	@Override
	public void removeChannel(UUID channelIdentifier) {
		Optional<ChannelInformation> optChannel = savedData.channels.stream().filter(channel -> channel.getChannelIdentifier().equals(channelIdentifier)).findFirst();
		savedData.channels.removeIf(channel -> channel.getChannelIdentifier().equals(channelIdentifier));
		optChannel.ifPresent(channelInformation -> sendUpdatePacketToClients(new ChannelInformation(null, channelIdentifier, channelInformation.getOwner(), channelInformation.getRights(), null)));
		savedData.markDirty();
	}

	public void markDirty() {
		savedData.markDirty();
	}

	private void sendUpdatePacketToClients(ChannelInformation channel) {
		MainProxy.sendToAllPlayers(PacketHandler.getPacket(ChannelInformationPacket.class).setInformation(channel).setTargeted(false).setCompressable(true));
	}

	public static class SavedData extends WorldSavedData {

		List<ChannelInformation> channels = new ArrayList<>();

		public SavedData(String name) {
			super(name);
		}

		public SavedData() {
			this(DATA_NAME);
		}

		@Override
		public void readFromNBT(NBTTagCompound nbt) {
			channels = new ArrayList<>();
			for (int i = 0; i < nbt.getInteger("dataSize"); i++) {
				channels.add(i, new ChannelInformation(nbt.getCompoundTag("data" + i)));
			}
		}

		@Nonnull
		@Override
		public NBTTagCompound writeToNBT(NBTTagCompound compound) {
			compound.setInteger("dataSize", channels.size());
			for (int i = 0; i < channels.size(); i++) {
				ChannelInformation channel = channels.get(i);
				NBTTagCompound nbt = new NBTTagCompound();
				channel.writeToNBT(nbt);
				compound.setTag("data" + i, nbt);
			}
			return compound;
		}

		public List<ChannelInformation> getChannels() {
			return this.channels;
		}

		public SavedData setChannels(List<ChannelInformation> channels) {
			this.channels = channels;
			return this;
		}

		public boolean equals(final Object o) {
			if (o == this) return true;
			if (!(o instanceof SavedData)) return false;
			final SavedData other = (SavedData) o;
			if (!other.canEqual(this)) return false;
			final Object this$channels = this.getChannels();
			final Object other$channels = other.getChannels();
			return Objects.equals(this$channels, other$channels);
		}

		protected boolean canEqual(final Object other) {
			return other instanceof SavedData;
		}

		public int hashCode() {
			final int PRIME = 59;
			int result = 1;
			final Object $channels = this.getChannels();
			result = result * PRIME + ($channels == null ? 43 : $channels.hashCode());
			return result;
		}

		public String toString() {
			return "ChannelManager.SavedData(channels=" + this.getChannels() + ")";
		}
	}
}
