package logisticspipes.proxy.cc.objects;

import logisticspipes.proxy.cc.interfaces.CCCommand;
import logisticspipes.proxy.cc.interfaces.CCType;
import logisticspipes.proxy.cc.interfaces.ICCTypeWrapped;
import logisticspipes.proxy.cc.interfaces.ILPCCTypeDefinition;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.item.ItemIdentifierStack;

public class CCItemIdentifierStack implements ILPCCTypeDefinition {

	@Override
	public ICCTypeWrapped getTypeFor(Object stack) {
		return new CCItemIdentifierStackImplementation((ItemIdentifierStack) stack);
	}

	@CCType(name="ItemIdentifierStack")
	public static class CCItemIdentifierStackImplementation implements ICCTypeWrapped {
		final ItemIdentifierStack stack;
		
		public CCItemIdentifierStackImplementation(ItemIdentifierStack stack2) {
			stack = stack2;
		}

		@CCCommand(description="Returns the ItemIdentifier from this ItemIdentifierStack")
		public ItemIdentifier getItem() {
			return stack.getItem();
		}

		@CCCommand(description="Returns the size of this ItemIdentifierStack")
		public int getStackSize() {
			return stack.getStackSize();
		}
		
		@CCCommand(description="Returns the name of this ItemIdentifierStack")
		public String getName() {
			return stack.getFriendlyName();
		}

		@CCCommand(description="Compares this ItemIdentifierStack to another one")
		public boolean equals(ItemIdentifierStack stack) {
			return stack.equals(stack);
		}

		@Override
		public Object getObject() {
			return stack;
		}
	}
}
