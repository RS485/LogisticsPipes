package logisticspipes.network.packets.pipe;

import java.io.IOException;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;

import lombok.Getter;
import lombok.Setter;

import logisticspipes.asm.ClientSideOnlyMethodContent;
import logisticspipes.gui.GuiLogisticsCraftingTable;
import logisticspipes.gui.orderer.GuiRequestTable;
import logisticspipes.gui.popup.GuiRecipeImport;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.utils.gui.LogisticsBaseGuiScreen;
import logisticspipes.utils.gui.SubGuiScreen;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

public class MostLikelyRecipeComponentsResponse extends ModernPacket {

	@Getter
	@Setter
	List<Integer> response;

	public MostLikelyRecipeComponentsResponse(int id) {
		super(id);
	}

	@Override
	public void readData(LPDataInput input) throws IOException {
		response = input.readList(LPDataInput::readInt);
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
		while (sub != null) {
			if (sub instanceof GuiRecipeImport) {
				importGui = (GuiRecipeImport) sub;
				break;
			}
			sub = sub.getSubGui();
		}
		if (importGui == null) return;
		importGui.handleProposePacket(response);
	}

	@Override
	public void writeData(LPDataOutput output) throws IOException {
		output.writeCollection(response, LPDataOutput::writeInt);
	}

	@Override
	public ModernPacket template() {
		return new MostLikelyRecipeComponentsResponse(getId());
	}
}
