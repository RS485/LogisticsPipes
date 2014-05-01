package logisticspipes.routing;

import logisticspipes.interfaces.routing.IRequestItems;
import logisticspipes.utils.item.ItemIdentifierStack;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain=true)
public class LogisticsOrder {
	public enum RequestType {PROVIDER, CRAFTING, EXTRA};
	
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
	private int routerId;
	@Getter
	@Setter
	private boolean inProgress;
	
	public LogisticsOrder(ItemIdentifierStack item, IRequestItems destination, RequestType type) {
		this.item = item;
		this.destination = destination;
		this.type = type;
	}
}
