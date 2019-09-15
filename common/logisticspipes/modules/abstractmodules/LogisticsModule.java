package logisticspipes.modules.abstractmodules;

import java.util.Collection;
import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.BlockPos;

import lombok.Getter;

import logisticspipes.interfaces.IPipeServiceProvider;
import logisticspipes.interfaces.IWorldProvider;
import logisticspipes.interfaces.SlotUpgradeManager;
import logisticspipes.utils.SinkReply;
import network.rs485.logisticspipes.module.ModuleType;
import network.rs485.logisticspipes.util.ItemVariant;

public abstract class LogisticsModule {

	public final ModuleType<?> type;

	protected IWorldProvider world;
	protected IPipeServiceProvider service;

	@Getter
	protected ModulePositionType slot;
	@Getter
	protected int positionInt;

	public LogisticsModule(ModuleType<?> type) {
		this.type = type;
	}

	/**
	 * Registers the Inventory and ItemSender to the module
	 *
	 * @param world   that the module is in.
	 * @param service Inventory access, power and utility functions provided by the
	 *                pipe
	 */
	public void registerHandler(IWorldProvider world, IPipeServiceProvider service) {
		this.world = world;
		this.service = service;
	}

	/**
	 * Registers the slot type the module is in
	 */
	public void registerPosition(ModulePositionType slot, int positionInt) {
		this.slot = slot;
		this.positionInt = positionInt;
	}

	public BlockPos getPos() {
		return service.getPos();
	}

	/**
	 * Gives an sink answer on the given itemstack
	 *
	 * @param stack              to sink
	 * @param bestPriority       best priority seen so far
	 * @param bestCustomPriority best custom subpriority
	 * @param allowDefault       is a default only sink allowed to sink this?
	 * @param includeInTransit   inclide the "in transit" items? -- true for a destination
	 *                           search, false for a sink check.
	 * @param forcePassive       check for passive routing only, in case this method is redirected to other sinks
	 * @return SinkReply whether the module sinks the item or not
	 */
	public abstract SinkReply sinksItem(ItemStack stack, int bestPriority, int bestCustomPriority, boolean allowDefault, boolean includeInTransit,
			boolean forcePassive);

	/**
	 * Returns submodules. Normal modules don't have submodules
	 *
	 * @param slot of the requested module
	 * @return
	 */
	public LogisticsModule getSubModule(int slot) {
		return null;
	}

	/**
	 * Is this module interested in all items, or just some specific ones?
	 *
	 * @return true: this module will be checked against every item request
	 * false: only requests involving items returned by
	 * getSpecificInterests() will be checked
	 */
	public abstract boolean hasGenericInterests();

	/**
	 * the list of items which this module is capable of providing or supplying
	 * (or is otherwise interested in) the size of the list here does not
	 * influence the ongoing computational cost.
	 *
	 * @return
	 */
	public abstract Collection<ItemVariant> getSpecificInterests();

	public abstract boolean interestedInAttachedInventory();

	/**
	 * is this module interested in receiving any damage variant of items in the
	 * attached inventory?
	 */
	public abstract boolean interestedInUndamagedID();

	/**
	 * is this module a valid destination for bounced items.
	 */
	public abstract boolean receivePassive();

	public boolean hasGui() {
		return false;
	}

	@Nullable
	protected SlotUpgradeManager getUpgradeManager() {
		if (service == null) {
			return null;
		}
		return service.getUpgradeManager(slot, positionInt);
	}

	/**
	 * typically used when the neighboring block changes
	 */
	public void clearCache() {}

	public CompoundTag toTag(CompoundTag tag) {
		return tag;
	}

	public void fromTag(CompoundTag tag) {}

	@Override
	public String toString() {
		return String.format("%s@(%s)", getClass().getSimpleName(), getPos());
	}

	public enum ModulePositionType {
		SLOT(true),
		IN_HAND(false),
		IN_PIPE(true);

		@Getter
		private final boolean inWorld;

		ModulePositionType(boolean inWorld) {
			this.inWorld = inWorld;
		}
	}

}
