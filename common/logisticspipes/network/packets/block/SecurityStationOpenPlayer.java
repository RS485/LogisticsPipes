package logisticspipes.network.packets.block;

import logisticspipes.gui.GuiSecurityStation;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.abstractpackets.NBTCoordinatesPacket;
import logisticspipes.proxy.MainProxy;
import logisticspipes.security.SecuritySettings;
import lombok.experimental.Accessors;
import net.minecraft.entity.player.EntityPlayer;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@Accessors(chain=true)
public class SecurityStationOpenPlayer extends NBTCoordinatesPacket {

	public SecurityStationOpenPlayer(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new SecurityStationOpenPlayer(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		if(MainProxy.isClient(player.worldObj)) {
			handleClientSide(player);
		} else {
			
		}
	}
	
	@SideOnly(Side.CLIENT)
	private void handleClientSide(EntityPlayer player) {
		if(FMLClientHandler.instance().getClient().currentScreen instanceof GuiSecurityStation) {
			SecuritySettings setting = new SecuritySettings(null);
			setting.readFromNBT(getTag());
			((GuiSecurityStation) FMLClientHandler.instance().getClient().currentScreen).handlePlayerSecurityOpen(setting);
		}
	}
}

