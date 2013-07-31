package logisticspipes.network.packets.orderer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import logisticspipes.asm.ClientSideOnlyMethodContent;
import logisticspipes.config.Configs;
import logisticspipes.gui.orderer.GuiOrderer;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.utils.ItemIdentifierStack;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.entity.player.EntityPlayer;
import cpw.mods.fml.client.FMLClientHandler;

@Accessors(chain=true)
public class MissingItems extends ModernPacket {

	@Getter
	@Setter
	private Collection<ItemIdentifierStack> items = new ArrayList<ItemIdentifierStack>();
	
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
		if (Configs.DISPLAY_POPUP && FMLClientHandler.instance().getClient().currentScreen instanceof GuiOrderer) {
			((GuiOrderer)FMLClientHandler.instance().getClient().currentScreen).handleRequestAnswer(getItems(), isFlag(), (GuiOrderer)FMLClientHandler.instance().getClient().currentScreen, player);
		} else if(isFlag()) {
			for(ItemIdentifierStack item:items){
				player.addChatMessage("Missing: " + item.getFriendlyName());
			}
		} else {
			for(ItemIdentifierStack item:items) {
				player.addChatMessage("Requested: " + item.getFriendlyName());
			}
			player.addChatMessage("Request successful!");
		}
	}
	@Override
	public void writeData(DataOutputStream data) throws IOException {
		for(ItemIdentifierStack item:items) {
			data.write(1);
			item.write(data);
		}
		data.write(0);
		data.writeBoolean(isFlag());
	}

	@Override
	public void readData(DataInputStream data) throws IOException {
		while(data.read() != 0) {
			items.add(ItemIdentifierStack.read(data));
		}
		setFlag(data.readBoolean());
	}
}

