package logisticspipes.network.packets.gui;

import java.io.IOException;

import logisticspipes.asm.ClientSideOnlyMethodContent;
import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;
import logisticspipes.network.abstractpackets.ModernPacket;

import net.minecraft.client.gui.GuiChat;
import net.minecraft.entity.player.EntityPlayer;

import cpw.mods.fml.client.FMLClientHandler;

public class OpenChatGui extends ModernPacket {

	public OpenChatGui(int id) {
		super(id);
	}

	@Override
	public void readData(LPDataInputStream data) throws IOException {}

	@Override
	@ClientSideOnlyMethodContent
	public void processPacket(EntityPlayer player) {
		FMLClientHandler.instance().getClient().displayGuiScreen(new GuiChat());
	}

	@Override
	public void writeData(LPDataOutputStream data) throws IOException {}

	@Override
	public ModernPacket template() {
		return new OpenChatGui(getId());
	}
}
