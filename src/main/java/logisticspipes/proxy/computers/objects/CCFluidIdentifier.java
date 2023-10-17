package logisticspipes.proxy.computers.objects;

import net.minecraft.nbt.NBTTagCompound;

import logisticspipes.proxy.computers.interfaces.CCCommand;
import logisticspipes.proxy.computers.interfaces.CCType;
import logisticspipes.proxy.computers.interfaces.ICCTypeWrapped;
import logisticspipes.proxy.computers.interfaces.ILPCCTypeDefinition;
import logisticspipes.utils.FluidIdentifier;
import logisticspipes.utils.item.ItemIdentifier;

public class CCFluidIdentifier implements ILPCCTypeDefinition {

	@Override
	public ICCTypeWrapped getTypeFor(Object ident) {
		return new CCFluidIdentifierImplementation((FluidIdentifier) ident);
	}

	@CCType(name = "FluidIdentifier")
	public static class CCFluidIdentifierImplementation implements ICCTypeWrapped {

		final FluidIdentifier ident;

		public CCFluidIdentifierImplementation(FluidIdentifier ident2) {
			ident = ident2;
		}

		@CCCommand(description = "Returns the fluidIdentifier of this FluidIdentifier")
		public String getId() {
			return ident.fluidID;
		}

		@CCCommand(description = "Returns true if this FluidIdentifier has an tag")
		public boolean hasTagCompound() {
			return ident.tag != null;
		}

		@CCCommand(description = "Returns the tag of this FluidIdentifier")
		public NBTTagCompound getTagCompound() {
			return ident.tag;
		}

		@CCCommand(description = "Returns the Name of this FluidIdentifier")
		public String getName() {
			return ident.getName();
		}

		@CCCommand(description = "Returns an ItemIdentifier if one exists for this FluidIdentifier")
		public ItemIdentifier getItemIdentifier() {
			return ident.getItemIdentifier();
		}

		@Override
		public FluidIdentifier getObject() {
			return ident;
		}
	}
}
