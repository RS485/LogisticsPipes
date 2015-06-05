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

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;

import cpw.mods.fml.client.FMLClientHandler;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
public class ComponentList extends ModernPacket {

	@Getter
	@Setter
	private Collection<IResource> used = new ArrayList<IResource>();

	@Getter
	@Setter
	private Collection<IResource> missing = new ArrayList<IResource>();

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
			((GuiOrderer) FMLClientHandler.instance().getClient().currentScreen).handleSimulateAnswer(used, missing, (GuiOrderer) FMLClientHandler.instance().getClient().currentScreen, player);
		} else if (Configs.DISPLAY_POPUP && FMLClientHandler.instance().getClient().currentScreen instanceof GuiRequestTable) {
			((GuiRequestTable) FMLClientHandler.instance().getClient().currentScreen).handleSimulateAnswer(used, missing, (GuiRequestTable) FMLClientHandler.instance().getClient().currentScreen, player);
		} else {
			for (IResource item : used) {
				player.addChatComponentMessage(new ChatComponentText("Component: " + item.getDisplayText(ColorCode.SUCCESS)));
			}
			for (IResource item : missing) {
				player.addChatComponentMessage(new ChatComponentText("Missing: " + item.getDisplayText(ColorCode.MISSING)));
			}
		}
	}

	@Override
	public void writeData(LPDataOutputStream data) throws IOException {
		data.writeCollection(used, new IWriteListObject<IResource>() {

			@Override
			public void writeObject(LPDataOutputStream data, IResource object) throws IOException {
				data.writeIResource(object);
			}
		});
		data.writeCollection(missing, new IWriteListObject<IResource>() {

			@Override
			public void writeObject(LPDataOutputStream data, IResource object) throws IOException {
				data.writeIResource(object);
			}
		});
		data.write(0);
	}

	@Override
	public void readData(LPDataInputStream data) throws IOException {
		used = data.readList(new IReadListObject<IResource>() {

			@Override
			public IResource readObject(LPDataInputStream data) throws IOException {
				return data.readIResource();
			}
		});
		missing = data.readList(new IReadListObject<IResource>() {

			@Override
			public IResource readObject(LPDataInputStream data) throws IOException {
				return data.readIResource();
			}
		});
	}
}
