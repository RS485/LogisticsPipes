package logisticspipes.network.packets.pipe;

import net.minecraft.entity.player.EntityPlayer;

import logisticspipes.interfaces.routing.IChannelManager;
import logisticspipes.network.NewGuiHandler;
import logisticspipes.network.abstractpackets.CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.guis.pipe.InvSysConSelectChannelPopupGUIProvider;
import logisticspipes.pipes.PipeItemsInvSysConnector;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.utils.StaticResolve;

@StaticResolve
public class InvSysConOpenSelectChannelPopupPacket extends CoordinatesPacket {

	public InvSysConOpenSelectChannelPopupPacket(int id) {
		super(id);
	}

	@Override
	public void processPacket(EntityPlayer player) {
		LogisticsTileGenericPipe pipe = this.getPipe(player.getEntityWorld(), LTGPCompletionCheck.PIPE);
		if (pipe.pipe instanceof PipeItemsInvSysConnector) {
			IChannelManager manager = SimpleServiceLocator.channelManagerProvider.getChannelManager(player.getEntityWorld());
			NewGuiHandler.getGui(InvSysConSelectChannelPopupGUIProvider.class).setChannelInformations(manager.getAllowedChannels(player)).setTilePos(pipe).open(player);
		}
	}

	@Override
	public ModernPacket template() {
		return new InvSysConOpenSelectChannelPopupPacket(getId());
	}
}
