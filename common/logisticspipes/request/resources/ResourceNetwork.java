package logisticspipes.request.resources;

import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

public enum ResourceNetwork {
	DictResource(DictResource.class) {
		@Override
		protected IResource readData(LPDataInput input) {
			return new DictResource(input);
		}
	},
	ItemResource(ItemResource.class) {
		@Override
		protected IResource readData(LPDataInput input) {
			return new ItemResource(input);
		}
	},
	FluidResource(FluidResource.class) {
		@Override
		protected IResource readData(LPDataInput input) {
			return new FluidResource(input);
		}
	};

	private final Class<? extends IResource> clazz;

	ResourceNetwork(Class<? extends IResource> clazz) {
		this.clazz = clazz;
	}

	public static void writeResource(LPDataOutput output, IResource resource) {
		if (resource == null) {
			output.writeInt(-1);
			return;
		}
		ResourceNetwork[] values = ResourceNetwork.values();
		for (ResourceNetwork value : values) {
			if (value.clazz.isAssignableFrom(resource.getClass())) {
				output.writeInt(value.ordinal());
				resource.writeData(output);
				return;
			}
		}
		throw new UnsupportedOperationException(resource.getClass().toString());
	}

	public static IResource readResource(LPDataInput input) {
		int id = input.readInt();
		if (id == -1) {
			return null;
		}
		return ResourceNetwork.values()[id].readData(input);
	}

	protected abstract IResource readData(LPDataInput input);
}
