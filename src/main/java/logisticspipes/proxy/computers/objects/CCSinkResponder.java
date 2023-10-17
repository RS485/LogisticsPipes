package logisticspipes.proxy.computers.objects;

import lombok.Getter;

import logisticspipes.interfaces.IQueueCCEvent;
import logisticspipes.proxy.computers.interfaces.CCCommand;
import logisticspipes.proxy.computers.interfaces.CCType;
import logisticspipes.proxy.computers.interfaces.ILPCCTypeHolder;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.item.ItemIdentifierStack;

@CCType(name = "CCItemSinkRequest")
public class CCSinkResponder implements ILPCCTypeHolder {

	private final Object[] ccTypeHolder = new Object[1];

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
		routerId = id;
		this.queuer = queuer;
	}

	@CCCommand(description = "Returns the ItemIdentifier for the item that should be sinked")
	public ItemIdentifier getItemIdentifier() {
		return stack.getItem();
	}

	@CCCommand(description = "Returns the amount of items that should be sinked")
	public int getAmount() {
		return stack.getStackSize();
	}

	@CCCommand(description = "Sends the response to the CC QuickSort module to deny the sink")
	public void denySink() {
		done = true;
		canSink = -1;
	}

	@CCCommand(description = "Sends the response to the CC QuickSort module to accept the sink for the given amount with the given priority")
	public void acceptSink(Double amount, Double priority) {
		canSink = ((Double) (amount > 0 ? amount : 0D)).intValue();
		this.priority = priority.intValue();
		done = true;
	}

	@Override
	public Object[] getTypeHolder() {
		return ccTypeHolder;
	}

}
