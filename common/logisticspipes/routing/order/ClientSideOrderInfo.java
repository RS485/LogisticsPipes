package logisticspipes.routing.order;

import java.util.List;

import logisticspipes.utils.item.ItemIdentifierStack;
import lombok.Getter;

public class ClientSideOrderInfo implements IOrderInfoProvider {

	@Getter
	private final ItemIdentifierStack item;
	@Getter
	private final boolean isFinished;
	@Getter
	private final RequestType type;
	@Getter
	private final boolean inProgress;
	@Getter
	private final int routerId;
	@Getter
	private final boolean isWatched = false;
	@Getter
	private final List<Float> progresses;
	
	public ClientSideOrderInfo(ItemIdentifierStack item, boolean isFinished, RequestType type, boolean inProgress, int routerId, List<Float> progresses) {
		this.item = item;
		this.isFinished = isFinished;
		this.type = type;
		this.inProgress = inProgress;
		this.routerId = routerId;
		this.progresses = progresses;
	}
	
	//Ignore this call
	@Override public void setWatched() {}
}
