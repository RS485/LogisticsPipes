package logisticspipes.network.packets.block;

import logisticspipes.blocks.LogisticsSecurityTileEntity;
import logisticspipes.gui.GuiSecurityStation;
import logisticspipes.network.abstractpackets.IntegerCoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.proxy.MainProxy;
import lombok.experimental.Accessors;
import net.minecraft.entity.player.EntityPlayer;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@Accessors(chain=true)
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
		LogisticsSecurityTileEntity tile = this.getTile(player.worldObj, LogisticsSecurityTileEntity.class);
		if(tile instanceof LogisticsSecurityTileEntity) {
			if(MainProxy.isClient(player.worldObj)) {
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
			((GuiSecurityStation)FMLClientHandler.instance().getClient().currentScreen).refreshCheckBoxes();
		}
	}
}

