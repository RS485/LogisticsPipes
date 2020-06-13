package logisticspipes.request.resources;

import javax.annotation.Nonnull;

import logisticspipes.interfaces.routing.IRequestItems;
import logisticspipes.routing.IRouter;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.item.ItemIdentifierStack;
import logisticspipes.utils.string.ChatColor;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

public class ItemResource implements IResource {

	private final ItemIdentifierStack stack;
	private final IRequestItems requester;

	public ItemResource(ItemIdentifierStack stack, IRequestItems requester) {
		this.stack = stack;
		this.requester = requester;
	}

	public ItemResource(LPDataInput input) {
		stack = input.readItemIdentifierStack();
		requester = null;
	}

	@Override
	public void writeData(LPDataOutput output) {
		output.writeItemIdentifierStack(stack);
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
	@Nonnull
	public IRouter getRouter() {
		return requester.getRouter();
	}

	@Override
	public boolean matches(ItemIdentifier itemType, MatchSettings settings) {
		switch (settings) {
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
