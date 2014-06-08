package logisticspipes.modules;

import java.util.Map;

import logisticspipes.pipes.PipeItemsCraftingLogisticsMk3;
import logisticspipes.utils.SinkReply;
import logisticspipes.utils.SinkReply.BufferMode;
import logisticspipes.utils.item.ItemIdentifier;

public class ModuleCrafterMK3 extends ModuleCrafter {

	private final PipeItemsCraftingLogisticsMk3	pipe;
	
	public ModuleCrafterMK3(PipeItemsCraftingLogisticsMk3 parent) {
		super(parent);
		this.pipe = parent;
	}
	
	@Override
	public SinkReply sinksItem(ItemIdentifier item, int bestPriority, int bestCustomPriority, boolean allowDefault, boolean includeInTransit) {
		if(bestPriority > _sinkReply.fixedPriority.ordinal() || (bestPriority == _sinkReply.fixedPriority.ordinal() && bestCustomPriority >= _sinkReply.customPriority)) return null;
		return new SinkReply(_sinkReply, spaceFor(item, includeInTransit, true), isForBuffer(item, includeInTransit) ? BufferMode.BUFFERED : areAllOrderesToBuffer() ? BufferMode.DESTINATION_BUFFERED : BufferMode.NONE);
	}
	
	protected int spaceFor(ItemIdentifier item, boolean includeInTransit, boolean addBufferSpace) {
		int invSpace = super.spaceFor(item, includeInTransit);
		if(addBufferSpace) {
			for(int i=0;i<pipe.inv.getSizeInventory();i++) {
				if(pipe.inv.getIDStackInSlot(i) == null) {
					invSpace += pipe.inv.getInventoryStackLimit();
				} else if(pipe.inv.getIDStackInSlot(i).getItem() == item) {
					invSpace += (pipe.inv.getInventoryStackLimit() - pipe.inv.getIDStackInSlot(i).getStackSize());
				}
			}
		} else {
			Map<ItemIdentifier, Integer> items = pipe.inv.getItemsAndCount();
			if(items.containsKey(item)) {
				invSpace -= items.get(item);
			}
		}
		return invSpace;
	}
	
	private boolean isForBuffer(ItemIdentifier item, boolean includeInTransit) {
		return spaceFor(item, includeInTransit, false) <= 0;
	}
	

	@Override
	protected int neededEnergy() {
		return 20;
	}

	@Override
	protected int itemsToExtract() {
		return 128;
	}
	
	@Override
	protected int stacksToExtract() {
		return 8;
	}
	

}
