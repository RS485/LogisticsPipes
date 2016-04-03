package logisticspipes.network.packets.pipe;

import logisticspipes.asm.ClientSideOnlyMethodContent;
import logisticspipes.gui.GuiLogisticsCraftingTable;
import logisticspipes.gui.orderer.GuiRequestTable;
import logisticspipes.gui.popup.GuiRecipeImport;
import logisticspipes.gui.popup.SelectItemOutOfList;
import logisticspipes.network.IReadListObject;
import logisticspipes.network.IWriteListObject;
import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.utils.gui.LogisticsBaseGuiScreen;
import logisticspipes.utils.gui.SubGuiScreen;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;

import java.io.IOException;
import java.util.List;

@Accessors(chain = true)
public class MostLikelyRecipeComponentsResponse extends ModernPacket {

	@Getter
	@Setter
	List<Integer> response;

	public MostLikelyRecipeComponentsResponse(int id) {
		super(id);
	}

	@Override
	public void readData(LPDataInputStream data) throws IOException {
		response = data.readList(new IReadListObject<Integer>() {
			@Override
			public Integer readObject(LPDataInputStream data) throws IOException {
				return data.readInt();
			}
		});
	}

	@Override
	@ClientSideOnlyMethodContent
	public void processPacket(EntityPlayer player) {
		GuiScreen firstGui = Minecraft.getMinecraft().currentScreen;
		LogisticsBaseGuiScreen gui;
		if (firstGui instanceof GuiLogisticsCraftingTable) {
			gui = (GuiLogisticsCraftingTable) firstGui;
		} else if (firstGui instanceof GuiRequestTable) {
			gui = (GuiRequestTable) firstGui;
		} else {
			return;
		}
		GuiRecipeImport importGui = null;
		SubGuiScreen sub = gui.getSubGui();
		while(sub != null) {
			if (sub instanceof GuiRecipeImport) {
				importGui = (GuiRecipeImport) sub;
				break;
			}
			sub = sub.getSubGui();
		}
		if(importGui == null) return;
		importGui.handleProposePacket(response);
	}

	@Override
	public void writeData(LPDataOutputStream data) throws IOException {
		data.writeList(response, new IWriteListObject<Integer>() {
			@Override
			public void writeObject(LPDataOutputStream data, Integer object) throws IOException {
				data.writeInt(object);
			}
		});
	}

	@Override
	public ModernPacket template() {
		return new MostLikelyRecipeComponentsResponse(getId());
	}
}
