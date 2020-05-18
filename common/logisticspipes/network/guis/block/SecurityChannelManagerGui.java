package logisticspipes.network.guis.block;

import net.minecraft.entity.player.EntityPlayer;

import logisticspipes.blocks.LogisticsSecurityTileEntity;
import logisticspipes.gui.popup.GuiManageChannelPopup;
import logisticspipes.network.abstractguis.GuiProvider;
import logisticspipes.network.abstractpackets.ChannelInformationListCoordinatesPopupGuiProvider;
import logisticspipes.utils.StaticResolve;

@StaticResolve
public class SecurityChannelManagerGui extends ChannelInformationListCoordinatesPopupGuiProvider {

	public SecurityChannelManagerGui(int id) {
		super(id);
	}

	@Override
	public Object getClientGui(EntityPlayer player) {
		return new GuiManageChannelPopup(getChannelInformations(), getTileAs(player.world, LogisticsSecurityTileEntity.class).getPos());
	}

	@Override
	public GuiProvider template() {
		return new SecurityChannelManagerGui(getId());
	}
}
