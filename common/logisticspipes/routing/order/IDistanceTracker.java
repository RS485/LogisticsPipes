package logisticspipes.routing.order;

public interface IDistanceTracker {

	void setCurrentDistanceToTarget(int value);

	int getCurrentDistanceToTarget();

	int getInitialDistanceToTarget();

	void setDestinationReached();

	boolean hasReachedDestination();

	void setDelay(long delay);

	boolean isTimeout();
}
