package logisticspipes.proxy.computers.objects;

import logisticspipes.proxy.computers.interfaces.CCCommand;
import logisticspipes.proxy.computers.interfaces.CCType;
import logisticspipes.proxy.computers.interfaces.ICCTypeWrapped;
import logisticspipes.proxy.computers.interfaces.ILPCCTypeDefinition;
import logisticspipes.request.resources.DictResource;
import logisticspipes.request.resources.FluidResource;
import logisticspipes.request.resources.IResource;
import logisticspipes.request.resources.ItemResource;
import logisticspipes.utils.FluidIdentifier;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.item.ItemIdentifierStack;

public class CCResource implements ILPCCTypeDefinition {

	@Override
	public ICCTypeWrapped getTypeFor(Object input) {
		if (input instanceof ItemResource) {
			return new CCItemResourceImplementation((ItemResource) input);
		}
		if (input instanceof DictResource) {
			return new CCDictResourceImplementation((DictResource) input);
		}
		if (input instanceof FluidResource) {
			return new CCFluidResourceImplementation((FluidResource) input);
		}
		return new CCResourceImplementation((IResource) input);
	}

	@CCType(name = "ItemResource")
	public static class CCItemResourceImplementation extends CCResourceImplementation {

		private ItemResource resource;

		protected CCItemResourceImplementation(ItemResource resource) {
			super(resource);
			this.resource = resource;
		}

		@CCCommand(description = "Returns the resource's item")
		public ItemIdentifier getItemIdentifier() {
			return resource.getItem();
		}

		@CCCommand(description = "Returns the resource's item")
		public ItemIdentifierStack getItemIdentifierStack() {
			return resource.getItemStack();
		}
	}

	@CCType(name = "DictResource")
	public static class CCDictResourceImplementation extends CCResourceImplementation {

		private DictResource resource;

		protected CCDictResourceImplementation(DictResource resource) {
			super(resource);
			this.resource = resource;
		}

		@CCCommand(description = "Returns the resource's item")
		public ItemIdentifier getItemIdentifier() {
			return resource.getItem();
		}

		@CCCommand(description = "Returns the resource's item")
		public ItemIdentifierStack getItemIdentifierStack() {
			return resource.getItemStack();
		}

		@CCCommand(description = "Returns whether the OreDictionary should be used to compare this resource")
		public Boolean isUseOreDictionarySet() {
			return resource.use_od;
		}

		@CCCommand(description = "Returns whether the OreDictionary's entry category should be used to compare this resource")
		public Boolean isUseOreDictionaryCategorySet() {
			return resource.use_category;
		}

		@CCCommand(description = "Returns whether the damage should be ignored to compare this resource")
		public Boolean isIgnoreDamageSet() {
			return resource.ignore_dmg;
		}

		@CCCommand(description = "Returns whether the NBT data should be ignored to compare this resource")
		public Boolean isIgnoreNBTSet() {
			return resource.ignore_nbt;
		}
	}

	@CCType(name = "FluidResource")
	public static class CCFluidResourceImplementation extends CCResourceImplementation {

		private FluidResource resource;

		protected CCFluidResourceImplementation(FluidResource resource) {
			super(resource);
			this.resource = resource;
		}

		@CCCommand(description = "Returns the resource's fluid")
		public FluidIdentifier getFluidIdentifier() {
			return resource.getFluid();
		}
	}

	@CCType(name = "Resource")
	public static class CCResourceImplementation implements ICCTypeWrapped {

		private IResource resource;

		protected CCResourceImplementation(IResource resource) {
			this.resource = resource;
		}

		@CCCommand(description = "Returns the type of resource")
		public String getResourceType() {
			if (resource instanceof ItemResource) {
				return "ItemResource";
			}
			if (resource instanceof DictResource) {
				return "DictResource";
			}
			if (resource instanceof FluidResource) {
				return "FluidResource";
			}
			return "UNKNOWN";
		}

		@CCCommand(description = "Returns the amount of the resource")
		public int getResourceAmount() {
			return resource.getRequestedAmount();
		}

		/* Lagacy Support for old scripts */

		@CCCommand(description = "Returns the first value")
		public Object getValue1() {
			if (resource instanceof ItemResource) {
				return ((ItemResource) resource).getItem();
			}
			if (resource instanceof DictResource) {
				return ((ItemResource) resource).getItem();
			}
			return null;
		}

		@CCCommand(description = "Returns the type of the first value")
		public String getType1() {
			if (getValue1() != null) {
				return getValue1().getClass().toString();
			} else {
				return "null";
			}
		}

		@CCCommand(description = "Returns the second value")
		public Object getValue2() {
			return resource.getRequestedAmount();
		}

		@CCCommand(description = "Returns the type of the second value")
		public String getType2() {
			return Integer.class.toString();
		}

		@Override
		public Object getObject() {
			return resource;
		}
	}
}
