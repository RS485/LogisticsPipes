package logisticspipes.interfaces.routing;

import alexiil.mc.lib.attributes.fluid.volume.FluidKey;

public interface FluidRequester extends Requester {

	void sendFailed(FluidKey fluid, int amount);

}
