package logisticspipes.routing.order;

public interface IDistanceTracker {
	public void setCurrentDistanceToTarget(int value);
	public int getCurrentDistanceToTarget();
	public int getInitialDistanceToTarget();
	public void setDestinationReached();
	public boolean hasReachedDestination();
}
