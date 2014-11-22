package logisticspipes.proxy.enderio;

import java.util.ArrayList;
import java.util.List;

import crazypants.enderio.machine.transceiver.Channel;
import crazypants.enderio.machine.transceiver.ChannelType;
import crazypants.enderio.machine.transceiver.ServerChannelRegister;
import crazypants.enderio.machine.transceiver.TileTransceiver;
import logisticspipes.proxy.interfaces.IEnderIOProxy;
import net.minecraft.tileentity.TileEntity;
import crazypants.enderio.machine.hypercube.HyperCubeRegister;
import crazypants.enderio.machine.hypercube.TileHyperCube;
import crazypants.enderio.machine.hypercube.TileHyperCube.IoMode;
import crazypants.enderio.machine.hypercube.TileHyperCube.SubChannel;

public class EnderIOProxy implements IEnderIOProxy {

	@Override
	public boolean isHyperCube(TileEntity tile) {
		return tile instanceof TileHyperCube;
	}

	@Override
	public boolean isTransceiver(TileEntity tile) {
		return tile instanceof TileTransceiver;
	}

	@Override
	public List<TileEntity> getConnectedHyperCubes(TileEntity tile) {
		List<TileHyperCube> cons = HyperCubeRegister.instance.getCubesForChannel(((TileHyperCube)tile).getChannel());
		List<TileEntity> tiles = new ArrayList<TileEntity>();
		for(TileHyperCube cube: cons) {
			if(cube != tile) {
				tiles.add(cube);
			}
		}
		return tiles;
	}

	@Override
	public List<TileEntity> getConnectedTransceivers(TileEntity tile) {
		TileTransceiver transceiver = (TileTransceiver) tile;
		List<TileEntity> tiles = new ArrayList<TileEntity>();
		Channel channel = transceiver.getRecieveChannels(ChannelType.ITEM).get(0);
		for (TileTransceiver t : ServerChannelRegister.instance.getIterator(channel)) {
			if (t == transceiver) {
				continue;
			}
			List<Channel> receiveChannels = t.getRecieveChannels(ChannelType.ITEM);
			List<Channel> sendChannels = t.getSendChannels(ChannelType.ITEM);
			if (receiveChannels.size() == 1 && sendChannels.size() == 1 && channel.equals(receiveChannels.get(0)) && channel.equals(sendChannels.get(0))) {
				tiles.add(t);
			}
		}
		return tiles;
	}

	@Override
	public boolean isSendAndReceive(TileEntity tile) {
		if (tile instanceof TileHyperCube) {
			return IoMode.BOTH == ((TileHyperCube) tile).getModeForChannel(SubChannel.ITEM);
		}
		if (tile instanceof TileTransceiver) {
			List<Channel> receiveChannels = ((TileTransceiver) tile).getRecieveChannels(ChannelType.ITEM);
			List<Channel> sendChannels = ((TileTransceiver) tile).getSendChannels(ChannelType.ITEM);
			return receiveChannels.size() == 1 && sendChannels.size() == 1 && receiveChannels.get(0).equals(sendChannels.get(0));
		}
		return false;
	}
	
	@Override
	public boolean isEnderIO() {
		return true;
	}
}
