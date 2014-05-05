package logisticspipes.routing.order;

public class DistanceTracker implements IDistanceTracker {

	private int currentDistance = 0;
	private int initialDistance = 0;
	private boolean endReached = false;

	@Override
	public void setCurrentDistanceToTarget(int value) {
		if(initialDistance == 0) {
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
}
