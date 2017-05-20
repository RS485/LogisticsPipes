package logisticspipes.routing.order;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import logisticspipes.interfaces.routing.IAdditionalTargetInformation;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.routing.IRouter;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.tuples.LPPosition;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
public abstract class LogisticsOrder implements IOrderInfoProvider {

	private static final int MIN_DISTANCE_TO_DISPLAY = 4;

	@Getter
	private final IAdditionalTargetInformation information;
	@Getter
	@Setter
	private boolean isFinished = false;

	/*
	 * Display Information
	 */
	@Getter
	private final ResourceType type;
	@Getter
	@Setter
	private boolean inProgress;
	@Getter
	private boolean isWatched = false;
	@Getter
	@Setter
	private byte machineProgress = 0;
	private List<IDistanceTracker> trackers = new CopyOnWriteArrayList<IDistanceTracker>();

	public LogisticsOrder(ResourceType type, IAdditionalTargetInformation info) {
		if (type == null) {
			throw new NullPointerException();
		}
		this.type = type;
		information = info;
	}

	@Override
	public int getRouterId() {
		if(getRouter() == null) {
			return -1;
		}
		return getRouter().getSimpleID();
	}

	public abstract IRouter getRouter();

	@Override
	public void setWatched() {
		isWatched = true;
	}

	public void addDistanceTracker(IDistanceTracker tracker) {
		trackers.add(tracker);
	}

	@Override
	public List<Float> getProgresses() {
		List<Float> progresses = new ArrayList<Float>();
		for (IDistanceTracker tracker : trackers) {
			if (!tracker.hasReachedDestination() && !tracker.isTimeout()) {
				float f;
				if (tracker.getInitialDistanceToTarget() != 0) {
					f = ((float) tracker.getCurrentDistanceToTarget()) / ((float) tracker.getInitialDistanceToTarget());
				} else {
					f = 1.0F;
				}
				if (!progresses.contains(f)) {
					if (tracker.getInitialDistanceToTarget() > LogisticsOrder.MIN_DISTANCE_TO_DISPLAY || tracker.getInitialDistanceToTarget() == 0) {
						progresses.add(f);
					}
				}
			}
		}
		return progresses;
	}

	public abstract void sendFailed();

	public abstract int getAmount();

	public abstract void reduceAmountBy(int amount);

	@Override
	public ItemIdentifier getTargetType() {
		IRouter router = getRouter();
		if (router == null) return null;
		CoreRoutedPipe pipe = router.getPipe();
		if (pipe == null) return null;
		return ItemIdentifier.get(pipe.item, 0, null);
	}

	@Override
	public LPPosition getTargetPosition() {
		IRouter router = getRouter();
		if(router == null) return null;
		return router.getLPPosition();
	}
}
