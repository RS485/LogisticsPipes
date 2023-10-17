package logisticspipes.proxy.computers.objects;

import logisticspipes.proxy.computers.interfaces.CCCommand;
import logisticspipes.proxy.computers.interfaces.CCType;
import logisticspipes.proxy.computers.interfaces.ICCTypeWrapped;
import logisticspipes.proxy.computers.interfaces.ILPCCTypeDefinition;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.item.ItemIdentifierStack;

public class CCItemIdentifierStack implements ILPCCTypeDefinition {

	@Override
	public ICCTypeWrapped getTypeFor(Object stack) {
		return new CCItemIdentifierStackImplementation((ItemIdentifierStack) stack);
	}

	@CCType(name = "ItemIdentifierStack")
	public static class CCItemIdentifierStackImplementation implements ICCTypeWrapped {

		final ItemIdentifierStack stack;

		public CCItemIdentifierStackImplementation(ItemIdentifierStack stack2) {
			stack = stack2;
		}

		@CCCommand(description = "Returns the ItemIdentifier from this ItemIdentifierStack")
		public ItemIdentifier getItem() {
			return stack.getItem();
		}

		@CCCommand(description = "Returns the size of this ItemIdentifierStack")
		public int getStackSize() {
			return stack.getStackSize();
		}

		@CCCommand(description = "Returns the name of this ItemIdentifierStack")
		public String getName() {
			return stack.getFriendlyName();
		}

		@CCCommand(description = "Compares this ItemIdentifierStack to another one")
		public boolean equals(ItemIdentifierStack stack) {
			return this.stack.equals(stack);
		}

		@Override
		public ItemIdentifierStack getObject() {
			return stack;
		}
	}
}
