package logisticspipes.transport;

import logisticspipes.pipes.PipeItemsCraftingLogisticsMk3;
import buildcraft.transport.EntityData;

public class CraftingPipeMk3Transport extends PipeTransportLogistics {
	
	public PipeItemsCraftingLogisticsMk3 pipe;
	
	public CraftingPipeMk3Transport() {
		super();
	}

	@Override
	protected void logisticsReverseItem(EntityData data) {
		data.item.getItemStack().stackSize = pipe.inv.addCompressed(data.item.getItemStack());
		if(data.item.getItemStack().stackSize > 0) {
			super.logisticsReverseItem(data);
		}
	}
}
