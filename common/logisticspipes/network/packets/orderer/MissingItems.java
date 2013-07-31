package logisticspipes.network.packets.orderer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import logisticspipes.asm.ClientSideOnlyMethodContent;
import logisticspipes.gui.orderer.GuiOrderer;
import logisticspipes.network.SendNBTTagCompound;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.utils.ItemMessage;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.entity.player.EntityPlayer;
import cpw.mods.fml.client.FMLClientHandler;

@Accessors(chain=true)
public class MissingItems extends ModernPacket {

	@Getter
	@Setter
	private List<ItemMessage> items = new ArrayList<ItemMessage>();
	
	@Setter
	@Getter
	private boolean flag;
	
	public MissingItems(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new MissingItems(getId());
	}

	@Override
	@ClientSideOnlyMethodContent
	public void processPacket(EntityPlayer player) {
		if (FMLClientHandler.instance().getClient().currentScreen instanceof GuiOrderer) {
			((GuiOrderer)FMLClientHandler.instance().getClient().currentScreen).handleRequestAnswer(getItems(),!isFlag(), (GuiOrderer)FMLClientHandler.instance().getClient().currentScreen, player);
		} else if(isFlag()) {
			for (final ItemMessage items : getItems()) {
				player.addChatMessage("Missing: " + items);
			}
		} else {
			for (final ItemMessage items : getItems()) {
				player.addChatMessage("Requested: " + items);
			}
			player.addChatMessage("Request successful!");
		}
	}
	@Override
	public void writeData(DataOutputStream data) throws IOException {
		for(ItemMessage msg:items) {
			data.write(1);
			data.writeInt(msg.id);
			data.writeInt(msg.data);
			data.writeInt(msg.amount);
			SendNBTTagCompound.writeNBTTagCompound(msg.tag, data);
		}
		data.write(0);
		data.writeBoolean(isFlag());
	}

	@Override
	public void readData(DataInputStream data) throws IOException {
		while(data.read() != 0) {
			ItemMessage msg = new ItemMessage();
			msg.id = data.readInt();
			msg.data = data.readInt();
			msg.amount = data.readInt();
			msg.tag = SendNBTTagCompound.readNBTTagCompound(data);
			items.add(msg);
		}
		setFlag(data.readBoolean());
	}
}

