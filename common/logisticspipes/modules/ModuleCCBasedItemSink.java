package logisticspipes.modules;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.minecraft.nbt.CompoundTag;

import logisticspipes.interfaces.IQueueCCEvent;
import logisticspipes.modules.abstractmodules.LogisticsModule;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.proxy.computers.objects.CCSinkResponder;
import logisticspipes.utils.OneList;
import logisticspipes.utils.SinkReply;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.item.ItemStack;

public class ModuleCCBasedItemSink extends LogisticsModule {

	private IQueueCCEvent eventQueuer;

	@Override
	public void readFromNBT(CompoundTag nbttagcompound) {}

	@Override
	public void writeToNBT(CompoundTag nbttagcompound) {}

	@Override
	public void registerCCEventQueuer(IQueueCCEvent eventQueuer) {
		this.eventQueuer = eventQueuer;
	}

	@Override
	public int getX() {
		return _service.getX();
	}

	@Override
	public int getY() {
		return _service.getY();
	}

	@Override
	public int getZ() {
		return _service.getZ();
	}

	@Override
	public SinkReply sinksItem(ItemIdentifier stack, int bestPriority, int bestCustomPriority, boolean allowDefault, boolean includeInTransit,
			boolean forcePassive) {
		return null;
	}

	@Override
	public LogisticsModule getSubModule(int slot) {
		return null;
	}

	@Override
	public void tick() {}

	@Override
	public boolean hasGenericInterests() {
		return true;
	}

	@Override
	public Collection<ItemIdentifier> getSpecificInterests() {
		return null;
	}

	@Override
	public boolean interestedInAttachedInventory() {
		return false;
	}

	@Override
	public boolean interestedInUndamagedID() {
		return false;
	}

	@Override
	public boolean recievePassive() {
		return false;
	}

	@Override
	public List<CCSinkResponder> queueCCSinkEvent(ItemStack item) {
		CCSinkResponder resonse = new CCSinkResponder(item, _service.getSourceID(), eventQueuer);
		eventQueuer.queueEvent("ItemSink", new Object[] { SimpleServiceLocator.ccProxy.getAnswer(resonse) });
		return Collections.singletonList(resonse);
	}

}
