package logisticspipes.proxy.computers.objects;

import logisticspipes.proxy.computers.interfaces.CCCommand;
import logisticspipes.proxy.computers.interfaces.CCType;
import logisticspipes.proxy.computers.interfaces.ICCTypeWrapped;
import logisticspipes.proxy.computers.interfaces.ILPCCTypeDefinition;
import logisticspipes.utils.FluidIdentifier;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.item.ItemIdentifierStack;

import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;

public class CCItemIdentifier implements ILPCCTypeDefinition {

	@Override
	public ICCTypeWrapped getTypeFor(Object ident) {
		return new CCItemIdentifierImplementation((ItemIdentifier) ident);
	}

	@CCType(name = "ItemIdentifier")
	public static class CCItemIdentifierImplementation implements ICCTypeWrapped {

		final ItemIdentifier ident;

		public CCItemIdentifierImplementation(ItemIdentifier ident2) {
			ident = ident2;
		}

		@CCCommand(description = "Returns the itemID (int) of this ItemIdentifier")
		public int getId() {
			return Item.getIdFromItem(ident.item);
		}

		@CCCommand(description = "Returns the itemID (String) of this ItemIdentifier")
		public String getIdName() {
			return Item.itemRegistry.getNameForObject(ident.item);
		}

		@CCCommand(description = "Returns the data/damage of this ItemIdentifier")
		public int getData() {
			return ident.itemDamage;
		}

		@CCCommand(description = "Returns true if this ItemIdentifier is damagable")
		public boolean isDamageable() {
			return ident.isDamageable();
		}

		@CCCommand(description = "Returns true if this ItemIdentifier has an tag")
		public boolean hasTagCompound() {
			return ident.tag != null;
		}

		@CCCommand(description = "Returns the tag of this ItemIdentifier")
		public NBTTagCompound getTagCompound() {
			return ident.tag;
		}

		@CCCommand(description = "Returns this ItemIdentifier in it's undamaged version")
		public ItemIdentifier getUndamaged() {
			return ident.getUndamaged();
		}

		@CCCommand(description = "Returns this ItemIdentifier without NBT tag information")
		public ItemIdentifier getIgnoringNBT() {
			return ident.getIgnoringNBT();
		}

		@CCCommand(description = "Returns the Name of this ItemIdentifier")
		public String getName() {
			return ident.getFriendlyNameCC();
		}

		@CCCommand(description = "Returns the name of the mod this ItemIdentifier belongs to")
		public String getModName() {
			return ident.getModName();
		}

		@CCCommand(description = "Returns a new ItemIdentifierStack")
		public ItemIdentifierStack makeStack(Double stackSize) {
			return ident.makeStack(stackSize.intValue());
		}

		@CCCommand(description = "Returns true if this ItemIdentifier represents an FluidIdentifier")
		public boolean isFluidContainer() {
			return ident.isFluidContainer();
		}

		@CCCommand(description = "Returns an FluidIdentifier if one exists for this ItemIdentifier")
		public FluidIdentifier getFluidContainer() {
			return FluidIdentifier.get(ident);
		}

		@Override
		public ItemIdentifier getObject() {
			return ident;
		}
	}
}
