package logisticspipes.modules;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import logisticspipes.pipes.PipeLogisticsChassi.ChassiTargetInformation;
import logisticspipes.utils.SinkReply;
import logisticspipes.utils.SinkReply.FixedPriority;
import logisticspipes.utils.item.ItemIdentifier;

public class ModuleEnchantmentSink extends LogisticsModule {

	public static String getName() {
		return "enchantment_sink";
	}

	@Override
	public void readFromNBT(@Nonnull NBTTagCompound nbttagcompound) {}

	@Override
	public void writeToNBT(@Nonnull NBTTagCompound nbttagcompound) {}

	private SinkReply _sinkReply;

	@Override
	public void registerPosition(ModulePositionType slot, int positionInt) {
		super.registerPosition(slot, positionInt);
		_sinkReply = new SinkReply(FixedPriority.EnchantmentItemSink, 0, true, false, 1, 0, new ChassiTargetInformation(getPositionInt()));
	}

	@Override
	public SinkReply sinksItem(@Nonnull ItemStack stack, ItemIdentifier item, int bestPriority, int bestCustomPriority, boolean allowDefault, boolean includeInTransit, boolean forcePassive) {
		// check to see if a better route is already found
		// Note: Higher MKs are higher priority
		if (bestPriority > _sinkReply.fixedPriority.ordinal() || (bestPriority == _sinkReply.fixedPriority.ordinal() && bestCustomPriority >= _sinkReply.customPriority)) {
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
	public boolean recievePassive() {
		return true;
	}

	@Override
	public boolean hasEffect() {
		return true;
	}
}
