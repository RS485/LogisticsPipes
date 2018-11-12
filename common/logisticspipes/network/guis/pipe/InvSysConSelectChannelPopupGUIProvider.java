package logisticspipes.network.guis.pipe;

import net.minecraft.entity.player.EntityPlayer;

import logisticspipes.gui.popup.GuiSelectChannelPopup;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractguis.GuiProvider;
import logisticspipes.network.abstractpackets.ChannelInformationListCoordinatesPopupGuiProvider;
import logisticspipes.network.packets.pipe.InvSysConSetChannelOnPipePacket;
import logisticspipes.pipes.PipeItemsInvSysConnector;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.StaticResolve;

@StaticResolve
public class InvSysConSelectChannelPopupGUIProvider extends ChannelInformationListCoordinatesPopupGuiProvider {

	public InvSysConSelectChannelPopupGUIProvider(int id) {
		super(id);
	}

	@Override
	public Object getClientGui(EntityPlayer player) {
		LogisticsTileGenericPipe bPipe = getPipe(player.getEntityWorld());
		if(bPipe != null && bPipe.pipe instanceof PipeItemsInvSysConnector) {

			return new GuiSelectChannelPopup(getChannelInformations(), bPipe.getBlockPos(), sel -> {
				if(sel != null) {
					MainProxy.sendPacketToServer(PacketHandler.getPacket(InvSysConSetChannelOnPipePacket.class).setString(sel.getChannelIdentifier().toString()).setTilePos(bPipe));
				}
			});
		}
		return null;
	}

	@Override
	public GuiProvider template() {
		return new InvSysConSelectChannelPopupGUIProvider(getId());
	}
}
