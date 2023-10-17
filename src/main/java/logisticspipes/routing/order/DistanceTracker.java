package logisticspipes.routing.order;

import logisticspipes.proxy.MainProxy;

public class DistanceTracker implements IDistanceTracker {

	private int currentDistance = 0;
	private int initialDistance = 0;
	private boolean endReached = false;
	private long delay = 0;

	@Override
	public void setCurrentDistanceToTarget(int value) {
		if (initialDistance == 0) {
			initialDistance = value;
		}
		currentDistance = value;
	}

	@Override
	public int getCurrentDistanceToTarget() {
		return currentDistance;
	}

	@Override
	public int getInitialDistanceToTarget() {
		return initialDistance;
	}

	@Override
	public void setDestinationReached() {
		endReached = true;
	}

	@Override
	public boolean hasReachedDestination() {
		return endReached;
	}

	@Override
	public void setDelay(long delay) {
		this.delay = delay;
	}

	@Override
	public boolean isTimeout() {
		return delay != 0 && delay <= MainProxy.getGlobalTick();
	}
}
