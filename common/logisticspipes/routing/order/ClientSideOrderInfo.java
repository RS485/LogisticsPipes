package logisticspipes.routing.order;

import java.util.List;

import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.item.ItemIdentifierStack;
import logisticspipes.utils.tuples.LPPosition;

import lombok.Getter;

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
	private final LPPosition targetPosition;
	@Getter
	private final ItemIdentifier targetType;

	public ClientSideOrderInfo(ItemIdentifierStack item, boolean isFinished, ResourceType type, boolean inProgress, int routerId, List<Float> progresses, byte machineProgress, LPPosition pos, ItemIdentifier targetType) {
		asDisplayItem = item;
		this.isFinished = isFinished;
		this.type = type;
		this.inProgress = inProgress;
		this.routerId = routerId;
		this.progresses = progresses;
		this.machineProgress = machineProgress;
		targetPosition = pos;
		this.targetType = targetType;
	}

	//Ignore this call
	@Override
	public void setWatched() {}
}
