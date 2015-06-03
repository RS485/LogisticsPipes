package logisticspipes.proxy.buildcraft.subproxies;

import logisticspipes.pipes.basic.LogisticsTileGenericPipe;

public interface IBCRenderTESR {
	void renderWires(LogisticsTileGenericPipe pipe, double x, double y, double z);
	void renderPluggables(LogisticsTileGenericPipe pipe, double x, double y, double z);
}
