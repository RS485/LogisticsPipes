package logisticspipes.network.packets.orderer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import logisticspipes.asm.ClientSideOnlyMethodContent;
import logisticspipes.config.Configs;
import logisticspipes.gui.orderer.GuiOrderer;
import logisticspipes.gui.orderer.GuiRequestTable;
import logisticspipes.network.IReadListObject;
import logisticspipes.network.IWriteListObject;
import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.request.resources.IResource;
import logisticspipes.request.resources.IResource.ColorCode;
import logisticspipes.utils.string.ChatColor;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;

import cpw.mods.fml.client.FMLClientHandler;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
public class MissingItems extends ModernPacket {

	@Getter
	@Setter
	private Collection<IResource> items = new ArrayList<IResource>();

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
			((GuiOrderer) FMLClientHandler.instance().getClient().currentScreen).handleRequestAnswer(getItems(), isFlag(), (GuiOrderer) FMLClientHandler.instance().getClient().currentScreen, player);
		} else if (Configs.DISPLAY_POPUP && FMLClientHandler.instance().getClient().currentScreen instanceof GuiRequestTable) {
			((GuiRequestTable) FMLClientHandler.instance().getClient().currentScreen).handleRequestAnswer(getItems(), isFlag(), (GuiRequestTable) FMLClientHandler.instance().getClient().currentScreen, player);
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
	public void writeData(LPDataOutputStream data) throws IOException {
		data.writeCollection(items, new IWriteListObject<IResource>() {

			@Override
			public void writeObject(LPDataOutputStream data, IResource object) throws IOException {
				data.writeIResource(object);
			}
		});
		data.writeBoolean(isFlag());
	}

	@Override
	public void readData(LPDataInputStream data) throws IOException {
		items = data.readList(new IReadListObject<IResource>() {

			@Override
			public IResource readObject(LPDataInputStream data) throws IOException {
				return data.readIResource();
			}
		});
		setFlag(data.readBoolean());
	}
}
