package logisticspipes.routing.order;

import java.util.List;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import lombok.Getter;

import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.item.ItemIdentifierStack;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.world.DoubleCoordinates;

@SideOnly(Side.CLIENT)
public class ClientSideOrderInfo implements IOrderInfoProvider {

	@Getter
	private final ItemIdentifierStack asDisplayItem;
	@Getter
	private final boolean isFinished;
	@Getter
	private final ResourceType type;
	@Getter
	private final boolean inProgress;
	@Getter
	private final int routerId;
	@Getter
	private final boolean isWatched = false;
	@Getter
	private final List<Float> progresses;
	@Getter
	private final byte machineProgress;
	@Getter
	private final DoubleCoordinates targetPosition;
	@Getter
	private final ItemIdentifier targetType;

	public ClientSideOrderInfo(LPDataInput input) {
		asDisplayItem = input.readItemIdentifierStack();
		routerId = input.readInt();
		isFinished = input.readBoolean();
		inProgress = input.readBoolean();
		type = input.readEnum(IOrderInfoProvider.ResourceType.class);
		progresses = input.readArrayList(LPDataInput::readFloat);
		machineProgress = input.readByte();
		if (input.readBoolean()) {
			targetPosition = new DoubleCoordinates(input);
			targetType = input.readItemIdentifier();
		} else {
			targetPosition = null;
			targetType = null;
		}
	}

	//Ignore this call
	@Override
	public void setWatched() {}
}
