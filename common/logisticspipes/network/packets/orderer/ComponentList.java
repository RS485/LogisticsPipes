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
public class ComponentList extends ModernPacket {

	@Getter
	@Setter
	private Collection<ItemIdentifierStack> used = new ArrayList<ItemIdentifierStack>();
	
	@Getter
	@Setter
	private Collection<ItemIdentifierStack> missing = new ArrayList<ItemIdentifierStack>();
	
	public ComponentList(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new ComponentList(getId());
	}

	@Override
	@ClientSideOnlyMethodContent
	public void processPacket(EntityPlayer player) {
		if (Configs.DISPLAY_POPUP && FMLClientHandler.instance().getClient().currentScreen instanceof GuiOrderer) {
			((GuiOrderer)FMLClientHandler.instance().getClient().currentScreen).handleSimulateAnswer(used, missing, (GuiOrderer)FMLClientHandler.instance().getClient().currentScreen, player);
		} else {
			for(ItemIdentifierStack item:used) {
				player.addChatMessage("Component: " + item.getFriendlyName());
			}
			for(ItemIdentifierStack item:missing) {
				player.addChatMessage("Missing: " + item.getFriendlyName());
			}
		}
	}
	
	@Override
	public void writeData(DataOutputStream data) throws IOException {
		for(ItemIdentifierStack item:used) {
			data.write(1);
			item.write(data);
		}
		data.write(0);
		for(ItemIdentifierStack item:missing) {
			data.write(1);
			item.write(data);
		}
		data.write(0);
	}

	@Override
	public void readData(DataInputStream data) throws IOException {
		while(data.read() != 0) {
			used.add(ItemIdentifierStack.read(data));
		}
		while(data.read() != 0) {
			missing.add(ItemIdentifierStack.read(data));
		}
	}
}

