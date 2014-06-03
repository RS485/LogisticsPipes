package logisticspipes.transport;

import buildcraft.transport.TravelingItem;
import logisticspipes.pipes.PipeItemsCraftingLogisticsMk3;
import logisticspipes.transport.LPTravelingItem.LPTravelingItemServer;
import logisticspipes.utils.item.ItemIdentifierStack;

public class CraftingPipeMk3Transport extends PipeTransportLogistics {
	
	public PipeItemsCraftingLogisticsMk3 pipe;
	
	public CraftingPipeMk3Transport() {
		super();
	}

	@Override
	protected void reverseItem(LPTravelingItemServer data, ItemIdentifierStack itemStack) {
		itemStack.setStackSize(pipe.inv.addCompressed(itemStack.makeNormalStack(), true));
		if(itemStack.getStackSize() > 0) {
			super.reverseItem(data, itemStack);
		}
	}
}
