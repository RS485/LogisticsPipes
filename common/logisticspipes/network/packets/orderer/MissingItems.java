package logisticspipes.network.packets.orderer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import cpw.mods.fml.client.FMLClientHandler;

import lombok.Getter;
import lombok.Setter;

import logisticspipes.asm.ClientSideOnlyMethodContent;
import logisticspipes.config.Configs;
import logisticspipes.gui.orderer.GuiOrderer;
import logisticspipes.gui.orderer.GuiRequestTable;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.request.resources.IResource;
import logisticspipes.request.resources.IResource.ColorCode;
import logisticspipes.utils.string.ChatColor;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

public class MissingItems extends ModernPacket {

	@Getter
	@Setter
	private Collection<IResource> items = new ArrayList<>();

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
			((GuiOrderer) FMLClientHandler.instance().getClient().currentScreen)
					.handleRequestAnswer(getItems(), isFlag(), (GuiOrderer) FMLClientHandler.instance().getClient().currentScreen, player);
		} else if (Configs.DISPLAY_POPUP && FMLClientHandler.instance().getClient().currentScreen instanceof GuiRequestTable) {
			((GuiRequestTable) FMLClientHandler.instance().getClient().currentScreen)
					.handleRequestAnswer(getItems(), isFlag(), (GuiRequestTable) FMLClientHandler.instance().getClient().currentScreen, player);
		} else if (isFlag()) {
			for (IResource item : items) {
				player.addChatComponentMessage(new ChatComponentText(ChatColor.RED + "Missing: " + item.getDisplayText(ColorCode.MISSING)));
			}
		} else {
			for (IResource item : items) {
				player.addChatComponentMessage(new ChatComponentText(ChatColor.GREEN + "Requested: " + item.getDisplayText(ColorCode.SUCCESS)));
			}
			player.addChatComponentMessage(new ChatComponentText(ChatColor.GREEN + "Request successful!"));
		}
	}

	@Override
	public void writeData(LPDataOutput output) throws IOException {
		output.writeCollection(items, LPDataOutput::writeResource);
		output.writeBoolean(isFlag());
	}

	@Override
	public void readData(LPDataInput input) throws IOException {
		items = input.readArrayList(LPDataInput::readResource);
		setFlag(input.readBoolean());
	}
}
