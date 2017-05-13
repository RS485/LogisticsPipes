package logisticspipes.proxy.enderio;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import crazypants.enderio.conduit.ConnectionMode;
import crazypants.enderio.conduit.IConduitBundle;
import crazypants.enderio.conduit.item.IItemConduit;
import crazypants.enderio.conduit.liquid.ILiquidConduit;
import logisticspipes.proxy.interfaces.IEnderIOProxy;

import net.minecraft.tileentity.TileEntity;

import crazypants.enderio.machine.transceiver.Channel;
import crazypants.enderio.machine.transceiver.ChannelType;
import crazypants.enderio.machine.transceiver.ServerChannelRegister;
import crazypants.enderio.machine.transceiver.TileTransceiver;
import net.minecraft.util.EnumFacing;

public class EnderIOProxy implements IEnderIOProxy {

	@Override
	public boolean isTransceiver(TileEntity tile) {
		return tile instanceof TileTransceiver;
	}

	@Override
	public List<TileEntity> getConnectedTransceivers(TileEntity tile) {
		TileTransceiver transceiver = (TileTransceiver) tile;
		List<TileEntity> tiles = new ArrayList<>();
		Object channel = transceiver.getRecieveChannels(ChannelType.ITEM).toArray()[0];
		for (TileTransceiver t : ServerChannelRegister.instance.getIterator((Channel) channel)) {
			if (t == transceiver) {
				continue;
			}
			Set<Channel> receiveChannels = t.getRecieveChannels(ChannelType.ITEM);
			Set<Channel> sendChannels = t.getSendChannels(ChannelType.ITEM);
			if (receiveChannels.size() == 1 && sendChannels.size() == 1 && channel.equals(receiveChannels.toArray()[0]) && channel.equals(sendChannels.toArray()[0])) {
				tiles.add(t);
			}
		}
		return tiles;
	}

	@Override
	public boolean isSendAndReceive(TileEntity tile) {
		if (tile instanceof TileTransceiver) {
			Set<Channel> receiveChannels = ((TileTransceiver) tile).getRecieveChannels(ChannelType.ITEM);
			Set<Channel> sendChannels = ((TileTransceiver) tile).getSendChannels(ChannelType.ITEM);
			return receiveChannels.size() == 1 && sendChannels.size() == 1 && receiveChannels.toArray()[0].equals(sendChannels.toArray()[0]);
		}
		return false;
	}

	@Override
	public boolean isEnderIO() {
		return true;
	}

	@Override
	public boolean isItemConduit(TileEntity tile, EnumFacing dir) {
		if(tile instanceof IConduitBundle) {
			if(((IConduitBundle)tile).hasType(IItemConduit.class)) {
				if(dir != null) {
					return ((IConduitBundle) tile).getConduit(IItemConduit.class).getConnectionMode(dir) != ConnectionMode.DISABLED;
				}
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isFluidConduit(TileEntity tile, EnumFacing dir) {
		if(tile instanceof IConduitBundle) {
			if(((IConduitBundle)tile).hasType(ILiquidConduit.class)) {
				if(dir != null) {
					return ((IConduitBundle) tile).getConduit(ILiquidConduit.class).getConnectionMode(dir) != ConnectionMode.DISABLED;
				}
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isBundledPipe(TileEntity tile) {
		return tile instanceof IConduitBundle;
	}
}
