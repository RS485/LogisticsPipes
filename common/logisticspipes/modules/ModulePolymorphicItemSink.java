package logisticspipes.modules;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;

import logisticspipes.interfaces.IInventoryUtil;
import logisticspipes.interfaces.IPipeServiceProvider;
import logisticspipes.interfaces.ISlotUpgradeManager;
import logisticspipes.pipes.PipeLogisticsChassis.ChassiTargetInformation;
import logisticspipes.utils.SinkReply;
import logisticspipes.utils.SinkReply.FixedPriority;
import logisticspipes.utils.item.ItemIdentifier;
import network.rs485.logisticspipes.module.PipeServiceProviderUtilKt;
import network.rs485.logisticspipes.property.Property;

public class ModulePolymorphicItemSink extends LogisticsModule {

	private SinkReply _sinkReply;

	public static String getName() {
		return "item_sink_polymorphic";
	}

	@Nonnull
	@Override
	public String getLPName() {
		return getName();
	}

	@NotNull
	@Override
	public List<Property<?>> getProperties() {
		return Collections.emptyList();
	}

	@Override
	public void registerPosition(@Nonnull ModulePositionType slot, int positionInt) {
		super.registerPosition(slot, positionInt);
		_sinkReply = new SinkReply(FixedPriority.ItemSink,
				0,
				true,
				false,
				3,
				0,
				new ChassiTargetInformation(getPositionInt()));
	}

	@Override
	public SinkReply sinksItem(@Nonnull ItemStack stack, ItemIdentifier item, int bestPriority, int bestCustomPriority,
			boolean allowDefault, boolean includeInTransit, boolean forcePassive) {
		if (bestPriority > _sinkReply.fixedPriority.ordinal() || (bestPriority == _sinkReply.fixedPriority.ordinal()
				&& bestCustomPriority >= _sinkReply.customPriority)) {
			return null;
		}
		final IPipeServiceProvider service = _service;
		if (service == null) return null;
		final ISlotUpgradeManager upgradeManager = service.getUpgradeManager(slot, positionInt);
		IInventoryUtil targetInventory = PipeServiceProviderUtilKt.availableSneakyInventories(service, upgradeManager)
				.stream().findFirst().orElse(null);
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
	public void tick() {}

	@Override
	public boolean hasGenericInterests() {
		return false;
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
