package logisticspipes.modules;

import java.util.List;
import javax.annotation.Nonnull;

import net.minecraft.nbt.NBTTagCompound;

import logisticspipes.interfaces.IQueueCCEvent;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.proxy.computers.objects.CCSinkResponder;
import logisticspipes.utils.OneList;
import logisticspipes.utils.SinkReply;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.item.ItemIdentifierStack;

public class ModuleCCBasedItemSink extends LogisticsModule {

	private IQueueCCEvent eventQueuer;

	@Override
	public void readFromNBT(@Nonnull NBTTagCompound nbttagcompound) {}

	@Override
	public void writeToNBT(@Nonnull NBTTagCompound nbttagcompound) {}

	@Override
	public void registerCCEventQueuer(IQueueCCEvent eventQueuer) {
		this.eventQueuer = eventQueuer;
	}

	@Override
	public SinkReply sinksItem(ItemIdentifier stack, int bestPriority, int bestCustomPriority, boolean allowDefault, boolean includeInTransit,
			boolean forcePassive) {
		return null;
	}

	@Override
	public void tick() {}

	@Override
	public boolean hasGenericInterests() {
		return true;
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
	public List<CCSinkResponder> queueCCSinkEvent(ItemIdentifierStack item) {
		CCSinkResponder resonse = new CCSinkResponder(item, _service.getSourceID(), eventQueuer);
		eventQueuer.queueEvent("ItemSink", new Object[] { SimpleServiceLocator.ccProxy.getAnswer(resonse) });
		return new OneList<>(resonse);
	}

}
