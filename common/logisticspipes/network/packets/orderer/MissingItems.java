package logisticspipes.network.packets.orderer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import logisticspipes.asm.ClientSideOnlyMethodContent;
import logisticspipes.config.Configs;
import logisticspipes.gui.orderer.GuiOrderer;
import logisticspipes.gui.orderer.GuiRequestTable;
import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.utils.item.ItemIdentifierStack;
import logisticspipes.utils.string.ChatColor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
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
		} else if (Configs.DISPLAY_POPUP && FMLClientHandler.instance().getClient().currentScreen instanceof GuiRequestTable) {
			((GuiRequestTable)FMLClientHandler.instance().getClient().currentScreen).handleRequestAnswer(getItems(), isFlag(), (GuiRequestTable)FMLClientHandler.instance().getClient().currentScreen, player);
		} else if(isFlag()) {
			for(ItemIdentifierStack item:items){
				player.addChatComponentMessage(new ChatComponentText(ChatColor.RED + "Missing: " + item.getFriendlyName()));
			}
		} else {
			for(ItemIdentifierStack item:items) {
				player.addChatComponentMessage(new ChatComponentText(ChatColor.GREEN + "Requested: " + item.getFriendlyName()));
			}
			player.addChatComponentMessage(new ChatComponentText(ChatColor.GREEN + "Request successful!"));
		}
	}
	@Override
	public void writeData(LPDataOutputStream data) throws IOException {
		for(ItemIdentifierStack item:items) {
			data.write(1);
			data.writeItemIdentifierStack(item);
		}
		data.write(0);
		data.writeBoolean(isFlag());
	}

	@Override
	public void readData(LPDataInputStream data) throws IOException {
		while(data.read() != 0) {
			items.add(data.readItemIdentifierStack());
		}
		setFlag(data.readBoolean());
	}
}

