package logisticspipes.network.packets.orderer;

import java.util.ArrayList;
import java.util.Collection;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentString;

import net.minecraftforge.fml.client.FMLClientHandler;

import lombok.Getter;
import lombok.Setter;

import logisticspipes.asm.ClientSideOnlyMethodContent;
import logisticspipes.config.Configs;
import logisticspipes.gui.orderer.GuiOrderer;
import logisticspipes.gui.orderer.GuiRequestTable;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.request.resources.IResource;
import logisticspipes.request.resources.IResource.ColorCode;
import logisticspipes.request.resources.ResourceNetwork;
import logisticspipes.utils.StaticResolve;
import logisticspipes.utils.string.ChatColor;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

@StaticResolve
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
				player.sendMessage(new TextComponentString(ChatColor.RED + "Missing: " + item.getDisplayText(ColorCode.MISSING)));
			}
		} else {
			for (IResource item : items) {
				player.sendMessage(new TextComponentString(ChatColor.GREEN + "Requested: " + item.getDisplayText(ColorCode.SUCCESS)));
			}
			player.sendMessage(new TextComponentString(ChatColor.GREEN + "Request successful!"));
		}
	}

	@Override
	public void writeData(LPDataOutput output) {
		output.writeCollection(items);
		output.writeBoolean(isFlag());
	}

	@Override
	public void readData(LPDataInput input) {
		items = input.readArrayList(ResourceNetwork::readResource);
		setFlag(input.readBoolean());
	}
}
