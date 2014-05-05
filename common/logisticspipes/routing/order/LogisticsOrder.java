package logisticspipes.routing.order;

import java.util.ArrayList;
import java.util.List;

import logisticspipes.interfaces.routing.IRequestItems;
import logisticspipes.utils.item.ItemIdentifierStack;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain=true)
public class LogisticsOrder implements IOrderInfoProvider {
	private static final int MIN_DISTANCE_TO_DISPLAY = 4;
	
	@Getter
	private final ItemIdentifierStack item;
	@Getter
	private final IRequestItems destination;
	@Getter
	@Setter
	private boolean isFinished = false;
	
	/*
	 * Display Information
	 */
	@Getter
	private final RequestType type;
	@Getter
	@Setter
	private boolean inProgress;
	@Getter
	private boolean isWatched = false;
	private List<IDistanceTracker> trackers = new ArrayList<IDistanceTracker>();
	
	public LogisticsOrder(ItemIdentifierStack item, IRequestItems destination, RequestType type) {
		this.item = item;
		this.destination = destination;
		this.type = type;
	}

	@Override
	public int getRouterId() {
		return destination.getRouter().getSimpleID();
	}

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
		for(IDistanceTracker tracker:trackers) {
			if(!tracker.hasReachedDestination()) {
				float f;
				if(tracker.getInitialDistanceToTarget() != 0) {
					f = ((float)tracker.getCurrentDistanceToTarget()) / ((float)tracker.getInitialDistanceToTarget());
				} else {
					f = 1.0F;
				}
				if(!progresses.contains(f)) {
					if(tracker.getInitialDistanceToTarget() > MIN_DISTANCE_TO_DISPLAY || tracker.getInitialDistanceToTarget() == 0) {
						progresses.add(f);
					}
				}
			}
		}
		return progresses;
	}
}
