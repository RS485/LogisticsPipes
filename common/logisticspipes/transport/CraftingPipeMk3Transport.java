package logisticspipes.transport;

import logisticspipes.pipes.PipeItemsCraftingLogisticsMk3;
import buildcraft.transport.TravelingItem;

public class CraftingPipeMk3Transport extends PipeTransportLogistics {
	
	public PipeItemsCraftingLogisticsMk3 pipe;
	
	public CraftingPipeMk3Transport() {
		super();
	}

	@Override
	protected void reverseItem(TravelingItem data) {
		data.getItemStack().stackSize = pipe.inv.addCompressed(data.getItemStack());
		if(data.getItemStack().stackSize > 0) {
			super.reverseItem(data);
		}
	}
}
