package logisticspipes.routing.order;

import java.util.List;

import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.item.ItemIdentifierStack;
import network.rs485.logisticspipes.util.LPDataOutput;
import network.rs485.logisticspipes.util.LPFinalSerializable;
import network.rs485.logisticspipes.world.DoubleCoordinates;

public interface IOrderInfoProvider extends LPFinalSerializable {

	boolean isFinished();

	ItemIdentifierStack getAsDisplayItem();

	ResourceType getType();

	int getRouterId();

	boolean isInProgress();

	boolean isWatched();

	void setWatched();

	List<Float> getProgresses();

	byte getMachineProgress();

	ItemIdentifier getTargetType();

	DoubleCoordinates getTargetPosition();

	@Override
	default void write(LPDataOutput output) {
		output.writeItemIdentifierStack(getAsDisplayItem());
		output.writeInt(getRouterId());
		output.writeBoolean(isFinished());
		output.writeBoolean(isInProgress());
		output.writeEnum(getType());
		output.writeCollection(getProgresses(), LPDataOutput::writeFloat);
		output.writeByte(getMachineProgress());
		if (getTargetPosition() != null) {
			output.writeBoolean(true);
			output.writeSerializable(getTargetPosition());
			output.writeItemIdentifier(getTargetType());
		} else {
			output.writeBoolean(false);
		}
	}

	enum ResourceType {
		PROVIDER,
		CRAFTING,
		EXTRA
	}
}
