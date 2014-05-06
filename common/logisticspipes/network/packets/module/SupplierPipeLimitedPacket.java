package logisticspipes.network.packets.module;


import java.io.IOException;

import logisticspipes.asm.ClientSideOnlyMethodContent;
import logisticspipes.gui.GuiSupplierPipe;
import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractpackets.CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipes.PipeItemsSupplierLogistics;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.MainProxy;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.entity.player.EntityPlayer;
import cpw.mods.fml.client.FMLClientHandler;

@Accessors(chain=true)
public class SupplierPipeLimitedPacket extends CoordinatesPacket {

	@Getter
	@Setter
	private boolean isLimited;
	
	public SupplierPipeLimitedPacket(int id) {
		super(id);
	}

	@Override
	public void processPacket(EntityPlayer player) {
		LogisticsTileGenericPipe pipe = this.getPipe(player.worldObj);
		if(pipe.pipe instanceof PipeItemsSupplierLogistics) {
			((PipeItemsSupplierLogistics)pipe.pipe).setLimited(isLimited());
		}
		if(MainProxy.isClient(player.worldObj)) {
			refresh();
		} else {
			MainProxy.sendPacketToPlayer(PacketHandler.getPacket(SupplierPipeLimitedPacket.class).setLimited(isLimited()).setPosX(getPosX()).setPosY(getPosY()).setPosZ(getPosZ()), player);
		}
	}
	
	@ClientSideOnlyMethodContent
	private void refresh() {
		if (FMLClientHandler.instance().getClient().currentScreen instanceof GuiSupplierPipe) {
			((GuiSupplierPipe) FMLClientHandler.instance().getClient().currentScreen).refreshMode();
		}
	}
	
	@Override
	public ModernPacket template() {
		return new SupplierPipeLimitedPacket(getId());
	}

	@Override
	public void writeData(LPDataOutputStream data) throws IOException {
		super.writeData(data);
		data.writeBoolean(isLimited);
	}

	@Override
	public void readData(LPDataInputStream data) throws IOException {
		super.readData(data);
		isLimited = data.readBoolean();
	}
}
