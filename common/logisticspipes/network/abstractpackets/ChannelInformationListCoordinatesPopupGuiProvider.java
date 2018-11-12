package logisticspipes.network.abstractpackets;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

import logisticspipes.network.abstractguis.CoordinatesPopupGuiProvider;
import logisticspipes.routing.channels.ChannelInformation;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

public abstract class ChannelInformationListCoordinatesPopupGuiProvider extends CoordinatesPopupGuiProvider {

	@Getter
	@Setter
	private List<ChannelInformation> channelInformations = new ArrayList<>();

	public ChannelInformationListCoordinatesPopupGuiProvider(int id) {
		super(id);
	}

	@Override
	public void writeData(LPDataOutput output) {
		super.writeData(output);
		output.writeCollection(channelInformations, LPDataOutput::writeChannelInformation);
	}

	@Override
	public void readData(LPDataInput input) {
		super.readData(input);
		channelInformations = input.readArrayList(LPDataInput::readChannelInformation);
	}

}
