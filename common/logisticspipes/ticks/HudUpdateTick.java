package logisticspipes.ticks;

import java.util.BitSet;

import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.routing.IRouter;
import logisticspipes.routing.IRouterManager;

public class HudUpdateTick {

	private static BitSet routersNeedingUpdate = new BitSet(4096);
	private static int firstRouter = -1;
	private static int inventorySlotsToUpdatePerTick = 90;

	public HudUpdateTick() {
	}

	public static void add(IRouter run) {
		int index = run.getSimpleID();
		if(index <0)
			return;
		routersNeedingUpdate.set(index); //expands the bit-set when out of bounds.
		if(firstRouter == -1) {
			firstRouter = index;
		}
	}

	public static void tick() {
		if(firstRouter == -1) return;
		IRouterManager rm = SimpleServiceLocator.routerManager;
		int slotSentCount = 0;
		while(firstRouter != -1 && slotSentCount < inventorySlotsToUpdatePerTick){
			routersNeedingUpdate.clear(firstRouter);
			IRouter currentRouter = rm.getRouterUnsafe(firstRouter, false);
			if(currentRouter != null) {
				CoreRoutedPipe pipe = currentRouter.getCachedPipe();
				if(pipe!=null)
					slotSentCount += pipe.sendQueueChanged(true);
			}
			firstRouter = routersNeedingUpdate.nextSetBit(firstRouter);
		}
	}
}
