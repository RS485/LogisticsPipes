package logisticspipes.network.packets.gui;

import net.minecraft.entity.player.EntityPlayer;

import lombok.Getter;
import lombok.Setter;

import logisticspipes.interfaces.IGUIChannelInformationReceiver;
import logisticspipes.network.abstractpackets.GuiPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.exception.TargetNotFoundException;
import logisticspipes.routing.channels.ChannelInformation;
import logisticspipes.utils.StaticResolve;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

@StaticResolve
public class ChannelInformationPacket extends GuiPacket {

	@Getter
	@Setter
	private ChannelInformation information;

	@Getter
	@Setter
	private boolean targeted;

	public ChannelInformationPacket(int id) {
		super(id);
	}

	@Override
	public void readData(LPDataInput input) {
		super.readData(input);
		information = input.readChannelInformation();
		targeted = input.readBoolean();
	}

	@Override
	public void writeData(LPDataOutput output) {
		super.writeData(output);
		output.writeChannelInformation(information);
		output.writeBoolean(targeted);
	}

	@Override
	public void processPacket(EntityPlayer player) {
		IGUIChannelInformationReceiver screen = this.getGui(IGUIChannelInformationReceiver.class);
		if (screen != null) {
			screen.handleChannelInformation(information, targeted);
		} else if (targeted) {
			throw new TargetNotFoundException("GuiDoesNotWantPacket", this);
		}
	}

	@Override
	public ModernPacket template() {
		return new ChannelInformationPacket(getId());
	}
}
