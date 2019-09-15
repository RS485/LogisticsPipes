package logisticspipes.modules;

import java.util.List;

import net.minecraft.nbt.CompoundTag;

import logisticspipes.interfaces.WrappedInventory;
import logisticspipes.modules.abstractmodules.LogisticsModule;
import logisticspipes.pipes.PipeLogisticsChassi.ChassiTargetInformation;
import logisticspipes.utils.SinkReply;
import logisticspipes.utils.SinkReply.FixedPriority;
import logisticspipes.utils.item.ItemIdentifier;

public class ModulePolymorphicItemSink extends LogisticsModule {

	public ModulePolymorphicItemSink() {}

	private SinkReply _sinkReply;

	@Override
	public void registerPosition(ModulePositionType slot, int positionInt) {
		super.registerPosition(slot, positionInt);
		_sinkReply = new SinkReply(FixedPriority.ItemSink, 0, true, false, 3, 0, new ChassiTargetInformation(getPositionInt()));
	}

	@Override
	public SinkReply sinksItem(ItemIdentifier item, int bestPriority, int bestCustomPriority, boolean allowDefault, boolean includeInTransit,
			boolean forcePassive) {
		if (bestPriority > _sinkReply.fixedPriority.ordinal() || (bestPriority == _sinkReply.fixedPriority.ordinal() && bestCustomPriority >= _sinkReply.customPriority)) {
			return null;
		}
		WrappedInventory targetInventory = service.getSneakyInventory(slot, positionInt);
		if (targetInventory == null) {
			return null;
		}

		if (!targetInventory.containsUndamagedItem(item.getUndamaged())) {
			return null;
		}

		if (service.canUseEnergy(3)) {
			return _sinkReply;
		}
		return null;
	}

	@Override
	public void readFromNBT(CompoundTag nbttagcompound) {}

	@Override
	public void writeToNBT(CompoundTag nbttagcompound) {}

	@Override
	public void tick() {}

	@Override
	public final int getX() {
		return service.getX();
	}

	@Override
	public final int getY() {
		return service.getY();
	}

	@Override
	public final int getZ() {
		return service.getZ();
	}

	@Override
	public boolean hasGenericInterests() {
		return false;
	}

	// TODO: SINK UNDAMAGED MATCH CORRECTLY!

	@Override
	public List<ItemIdentifier> getSpecificInterests() {
		return null;
	}

	@Override
	public boolean interestedInAttachedInventory() {
		return true; // by definition :)
	}

	@Override
	public boolean interestedInUndamagedID() {
		return true;
	}

	@Override
	public boolean receivePassive() {
		return true;
	}

}
