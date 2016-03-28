package logisticspipes.request.resources;

import java.io.IOException;

import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;

public enum ResourceNetwork {
	DictResource(DictResource.class) {

		@Override
		protected IResource readData(LPDataInputStream data) throws IOException {
			return new DictResource(data);
		}
	},
	ItemResource(ItemResource.class) {

		@Override
		protected IResource readData(LPDataInputStream data) throws IOException {
			return new ItemResource(data);
		}
	},
	FluidResource(FluidResource.class) {

		@Override
		protected IResource readData(LPDataInputStream data) throws IOException {
			return new FluidResource(data);
		}
	};

	private final Class<? extends IResource> clazz;

	private ResourceNetwork(Class<? extends IResource> clazz) {
		this.clazz = clazz;
	}

	public static void writeResource(LPDataOutputStream data, IResource resource) throws IOException {
		if(resource == null) {
			data.writeInt(-1);
			return;
		}
		ResourceNetwork[] values = ResourceNetwork.values();
		for (ResourceNetwork value : values) {
			if (value.clazz.isAssignableFrom(resource.getClass())) {
				data.writeInt(value.ordinal());
				resource.writeData(data);
				return;
			}
		}
		throw new UnsupportedOperationException(resource.getClass().toString());
	}

	public static IResource readResource(LPDataInputStream data) throws IOException {
		int id = data.readInt();
		if(id == -1) {
			return null;
		}
		return ResourceNetwork.values()[id].readData(data);
	}

	protected abstract IResource readData(LPDataInputStream data) throws IOException;
}
