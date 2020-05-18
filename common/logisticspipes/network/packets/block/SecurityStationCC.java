package logisticspipes.network.packets.block;

import net.minecraft.entity.player.EntityPlayer;

import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import logisticspipes.blocks.LogisticsSecurityTileEntity;
import logisticspipes.gui.GuiSecurityStation;
import logisticspipes.network.abstractpackets.IntegerCoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.StaticResolve;

@StaticResolve
public class SecurityStationCC extends IntegerCoordinatesPacket {

	public SecurityStationCC(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new SecurityStationCC(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		LogisticsSecurityTileEntity tile = this.getTileAs(player.world, LogisticsSecurityTileEntity.class);
		if (tile instanceof LogisticsSecurityTileEntity) {
			if (MainProxy.isClient(player.world)) {
				tile.setClientCC(getInteger() == 1);
				handleClientSide(player);
			} else {
				tile.changeCC();
			}
		}
	}

	@SideOnly(Side.CLIENT)
	private void handleClientSide(EntityPlayer player) {
		if (FMLClientHandler.instance().getClient().currentScreen instanceof GuiSecurityStation) {
			((GuiSecurityStation) FMLClientHandler.instance().getClient().currentScreen).refreshCheckBoxes();
		}
	}
}
