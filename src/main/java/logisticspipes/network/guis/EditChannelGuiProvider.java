package logisticspipes.network.guis;

import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;

import lombok.Getter;
import lombok.Setter;

import logisticspipes.gui.popup.GuiEditChannelPopup;
import logisticspipes.network.abstractguis.GuiProvider;
import logisticspipes.network.abstractguis.PopupGuiProvider;
import logisticspipes.routing.channels.ChannelInformation;
import logisticspipes.utils.StaticResolve;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

@StaticResolve
public class EditChannelGuiProvider extends PopupGuiProvider {

	@Getter
	@Setter
	private ChannelInformation channel;

	@Getter
	@Setter
	private UUID responsibleSecurityID;

	public EditChannelGuiProvider(int id) {
		super(id);
	}

	@Override
	public void writeData(LPDataOutput output) {
		super.writeData(output);
		output.writeChannelInformation(channel);
		output.writeUUID(responsibleSecurityID);
	}

	@Override
	public void readData(LPDataInput input) {
		super.readData(input);
		channel = input.readChannelInformation();
		responsibleSecurityID = input.readUUID();
	}

	@Override
	public Object getClientGui(EntityPlayer player) {
		return new GuiEditChannelPopup(responsibleSecurityID, channel);
	}

	@Override
	public GuiProvider template() {
		return new EditChannelGuiProvider(getId());
	}
}
