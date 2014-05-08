package logisticspipes.routing.order;

import java.util.List;

import logisticspipes.utils.item.ItemIdentifierStack;

public interface IOrderInfoProvider {
	public enum RequestType {PROVIDER, CRAFTING, EXTRA};
	
	boolean isFinished();

	ItemIdentifierStack getItem();

	RequestType getType();

	int getRouterId();

	boolean isInProgress();

	boolean isWatched();
	void setWatched();
	
	List<Float> getProgresses();
	byte getMachineProgress();
}
