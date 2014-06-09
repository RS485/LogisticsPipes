package logisticspipes.network.abstractguis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;
import logisticspipes.network.NewGuiHandler;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.gui.DummyContainer;
import logisticspipes.utils.gui.LogisticsBaseGuiScreen;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.minecraft.entity.player.EntityPlayer;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@Accessors(chain=true)
public abstract class GuiProvider {
	
	@Getter
	private final int id;
	
	private List<ModernPacket> additinalPackets = new ArrayList<ModernPacket>();
	
	public GuiProvider(int id) {
		this.id = id;
	}
	
	public void writeData(LPDataOutputStream data) throws IOException {}

	public void readData(LPDataInputStream data) throws IOException {}
	
	@SideOnly(Side.CLIENT)
	public abstract LogisticsBaseGuiScreen getClientGui(EntityPlayer player);
	public abstract DummyContainer getContainer(EntityPlayer player);
	
	public abstract GuiProvider template();
	
	public final void open(EntityPlayer player) {
		for(ModernPacket packet:additinalPackets) {
			MainProxy.sendPacketToPlayer(packet, (Player) player);
		}
		NewGuiHandler.openGui(this, player);
	}
	
	public final GuiProvider addPacket(ModernPacket packet) {
		additinalPackets.add(packet);
		return this;
	}
}
