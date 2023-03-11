package logisticspipes.modules;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;

import logisticspipes.pipes.PipeLogisticsChassis.ChassiTargetInformation;
import logisticspipes.utils.SinkReply;
import logisticspipes.utils.SinkReply.FixedPriority;
import logisticspipes.utils.item.ItemIdentifier;
import network.rs485.logisticspipes.property.Property;

public class ModuleEnchantmentSink extends LogisticsModule {

	private SinkReply _sinkReply;

	public static String getName() {
		return "enchantment_sink";
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
		_sinkReply = new SinkReply(FixedPriority.EnchantmentItemSink,
				0,
				true,
				false,
				1,
				0,
				new ChassiTargetInformation(getPositionInt()));
	}

	@Override
	public SinkReply sinksItem(@Nonnull ItemStack stack, ItemIdentifier item, int bestPriority, int bestCustomPriority,
			boolean allowDefault, boolean includeInTransit, boolean forcePassive) {
		// check to see if a better route is already found
		// Note: Higher MKs are higher priority
		if (bestPriority > _sinkReply.fixedPriority.ordinal() || (bestPriority == _sinkReply.fixedPriority.ordinal()
				&& bestCustomPriority >= _sinkReply.customPriority)) {
			return null;
		}

		//check to see if item is enchanted
		if (stack.isItemEnchanted()) {
			return _sinkReply;
		}
		return null;
	}

	@Override
	public void tick() {}

	@Override
	/*
	 * We will check every item return true
	 * @see logisticspipes.modules.LogisticsModule#hasGenericInterests()
	 */
	public boolean hasGenericInterests() {
		return true;
	}

	@Override
	public boolean interestedInAttachedInventory() {
		return false;
	}

	@Override
	public boolean interestedInUndamagedID() {
		return true;
	}

	@Override
	public boolean receivePassive() {
		return true;
	}

	@Override
	public boolean hasEffect() {
		return true;
	}

}
