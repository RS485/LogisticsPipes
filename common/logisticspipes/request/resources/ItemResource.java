package logisticspipes.request.resources;

import java.io.IOException;

import logisticspipes.interfaces.routing.IRequestItems;
import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;
import logisticspipes.routing.IRouter;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.item.ItemIdentifierStack;
import logisticspipes.utils.string.ChatColor;

public class ItemResource implements IResource {

	private final ItemIdentifierStack stack;
	private final IRequestItems requester;

	public ItemResource(ItemIdentifierStack stack, IRequestItems requester) {
		this.stack = stack;
		this.requester = requester;
	}

	public ItemResource(LPDataInputStream data) throws IOException {
		stack = data.readItemIdentifierStack();
		requester = null;
	}

	@Override
	public void writeData(LPDataOutputStream data) throws IOException {
		data.writeItemIdentifierStack(stack);
	}

	@Override
	public ItemIdentifier getAsItem() {
		return stack.getItem();
	}

	@Override
	public int getRequestedAmount() {
		return stack.getStackSize();
	}

	public ItemIdentifier getItem() {
		return stack.getItem();
	}

	public ItemIdentifierStack getItemStack() {
		return stack;
	}

	public IRequestItems getTarget() {
		return requester;
	}

	@Override
	public IRouter getRouter() {
		return requester.getRouter();
	}

	@Override
	public boolean matches(ItemIdentifier itemType, MatchSettings settings) {
		switch(settings) {
			case NORMAL:
				return stack.getItem().equals(itemType);
			case WITHOUT_NBT:
				return stack.getItem().equalsWithoutNBT(itemType);
		}
		return stack.getItem().equals(itemType);
	}

	@Override
	public IResource clone(int multiplier) {
		ItemIdentifierStack stack = this.stack.clone();
		stack.setStackSize(stack.getStackSize() * multiplier);
		return new ItemResource(stack, requester);
	}

	@Override
	public boolean mergeForDisplay(IResource resource, int withAmount) {
		if (resource instanceof ItemResource) {
			if (((ItemResource) resource).stack.getItem().equals(stack.getItem())) {
				stack.setStackSize(stack.getStackSize() + withAmount);
				return true;
			}
		}
		return false;
	}

	@Override
	public IResource copyForDisplayWith(int amount) {
		ItemIdentifierStack stack = this.stack.clone();
		stack.setStackSize(amount);
		return new ItemResource(stack, requester);
	}

	private Object ccObject;

	@Override
	public void setCCType(Object type) {
		ccObject = type;
	}

	@Override
	public Object getCCType() {
		return ccObject;
	}

	@Override
	public String getDisplayText(ColorCode code) {
		StringBuilder builder = new StringBuilder();
		if (code != ColorCode.NONE) {
			builder.append(code == ColorCode.MISSING ? ChatColor.RED : ChatColor.GREEN);
		}
		builder.append(stack.getFriendlyName());
		if (code != ColorCode.NONE) {
			builder.append(ChatColor.WHITE);
		}
		return builder.toString();
	}

	@Override
	public ItemIdentifierStack getDisplayItem() {
		return stack;
	}
}
