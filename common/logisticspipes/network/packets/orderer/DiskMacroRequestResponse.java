package logisticspipes.network.packets.orderer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import logisticspipes.gui.orderer.GuiOrderer;
import logisticspipes.gui.popup.GuiDiskPopup;
import logisticspipes.network.abstractpackets.ItemPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.utils.ItemMessage;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.entity.player.EntityPlayer;
import cpw.mods.fml.client.FMLClientHandler;

@Accessors(chain=true)
public class DiskMacroRequestResponse extends ItemPacket {

	@Getter
	@Setter
	private boolean flag;
	
	public DiskMacroRequestResponse(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new DiskMacroRequestResponse(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		if (FMLClientHandler.instance().getClient().currentScreen instanceof GuiOrderer) {
			if(((GuiOrderer) FMLClientHandler.instance().getClient().currentScreen).getSubGui() instanceof GuiDiskPopup) {
				((GuiOrderer) FMLClientHandler.instance().getClient().currentScreen).handleRequestAnswer(new ItemMessage(getStack().itemID, getStack().getItemDamage(), 1, null), isFlag(), ((GuiOrderer) FMLClientHandler.instance().getClient().currentScreen).getSubGui(),FMLClientHandler.instance().getClient().thePlayer);
			}
		}
	}

	@Override
	public void writeData(DataOutputStream data) throws IOException {
		super.writeData(data);
		data.writeBoolean(isFlag());
	}

	@Override
	public void readData(DataInputStream data) throws IOException {
		super.readData(data);
		setFlag(data.readBoolean());
	}
}

