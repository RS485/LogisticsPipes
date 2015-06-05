package logisticspipes.routing.order;

import java.util.List;

import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.item.ItemIdentifierStack;
import logisticspipes.utils.tuples.LPPosition;

public interface IOrderInfoProvider {

	public enum ResourceType {
		PROVIDER,
		CRAFTING,
		EXTRA
	};

	boolean isFinished();

	ItemIdentifierStack getAsDisplayItem();

	ResourceType getType();

	int getRouterId();

	boolean isInProgress();

	boolean isWatched();

	void setWatched();

	List<Float> getProgresses();

	byte getMachineProgress();

	ItemIdentifier getTargetType();

	LPPosition getTargetPosition();

}
