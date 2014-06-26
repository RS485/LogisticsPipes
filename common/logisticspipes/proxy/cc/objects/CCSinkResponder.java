package logisticspipes.proxy.cc.objects;

import logisticspipes.interfaces.IQueueCCEvent;
import logisticspipes.proxy.cc.interfaces.CCCommand;
import logisticspipes.proxy.cc.interfaces.CCType;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.item.ItemIdentifierStack;
import lombok.Getter;

@CCType(name="CCItemSinkRequest")
public class CCSinkResponder {
	@Getter
	private final ItemIdentifierStack stack;
	@Getter
	private final int routerId;
	@Getter
	private boolean done = false;
	@Getter
	private final IQueueCCEvent queuer;

	@Getter
	private int canSink;
	@Getter
	private int priority;
	
	public CCSinkResponder(ItemIdentifierStack stack, int id, IQueueCCEvent queuer) {
		this.stack = stack;
		this.routerId = id;
		this.queuer = queuer;
	}
	
	@CCCommand
	public ItemIdentifier getItemIdentifier() {
		return stack.getItem();
	}
	
	@CCCommand
	public int getAmount() {
		return stack.getStackSize();
	}
	
	@CCCommand
	public void denySink() {
		done = true;
		this.canSink = -1;
	}
	
	@CCCommand
	public void acceptSink(Double amount, Double priority) {
		this.canSink = ((Double)(amount > 0 ? amount : 0D)).intValue();
		this.priority = priority.intValue();
		done = true;
	}
}
