package logisticspipes.network.packets.orderer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import logisticspipes.Configs;
import logisticspipes.asm.ClientSideOnlyMethodContent;
import logisticspipes.gui.orderer.GuiOrderer;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.utils.item.ItemIdentifierStack;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.entity.player.EntityPlayer;
import cpw.mods.fml.client.FMLClientHandler;

@Accessors(chain=true)
public class MissingItems extends ModernPacket {

	@Getter
	@Setter
	private ProcessedItem[] items;
	
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
			((GuiOrderer)FMLClientHandler.instance().getClient().currentScreen).handleRequestAnswer(getItems(), (GuiOrderer)FMLClientHandler.instance().getClient().currentScreen, player);
		} else {
			StringBuilder sucessful = new StringBuilder(), unsucessful = new StringBuilder();
			
			for(ProcessedItem item : items){
				if (item.isSuccessful()){
					if (sucessful.length() == 0){
						sucessful.append(item.getStack().getFriendlyName());
					}else{
						sucessful.append(", ").append(item.getStack().getFriendlyName());
					}
				}else{
					if (unsucessful.length() == 0){
						unsucessful.append(item.getStack().getFriendlyName());
					}else{
						unsucessful.append(", ").append(item.getStack().getFriendlyName());
					}
				}
			}
			
			if (sucessful.length() > 0){
				player.addChatMessage("Sucessful: " + sucessful.toString());
			}
			
			if (unsucessful.length() > 0){
				player.addChatMessage("Missing: " + unsucessful.toString());
			}
		}
	}
	@Override
	public void writeData(DataOutputStream data) throws IOException {
		data.writeShort(items.length);
		
		for(ProcessedItem item : items) {
			item.getStack().write(data);
			data.writeBoolean(item.isSuccessful());
		}
	}

	@Override
	public void readData(DataInputStream data) throws IOException {
		ProcessedItem[] items = new ProcessedItem[data.readUnsignedShort()];
		
		for(int i = 0; i < items.length; i++) {
			items[i] = new ProcessedItem(ItemIdentifierStack.read(data), data.readBoolean());
		}
		
		this.items = items;
	}
	
	public static class ProcessedItem {
		private final boolean successful;
		private ItemIdentifierStack stack;
		
		public ProcessedItem (ItemIdentifierStack stack, boolean successful){
			this.successful = successful;
			this.stack = stack;
		}
		
		public ItemIdentifierStack getStack() {
			return stack;
		}
		
		public void setStack(ItemIdentifierStack stack) {
			this.stack = stack;
		}
		
		public boolean isSuccessful() {
			return successful;
		}
	}
}

