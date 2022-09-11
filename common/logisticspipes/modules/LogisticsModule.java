package logisticspipes.modules;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import kotlin.Unit;
import lombok.Getter;

import logisticspipes.LogisticsPipes;
import logisticspipes.interfaces.IPipeServiceProvider;
import logisticspipes.interfaces.ISlotUpgradeManager;
import logisticspipes.interfaces.IWorldProvider;
import logisticspipes.interfaces.routing.ISaveState;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.computers.interfaces.CCCommand;
import logisticspipes.proxy.computers.interfaces.CCType;
import logisticspipes.proxy.computers.interfaces.ILPCCTypeHolder;
import logisticspipes.proxy.computers.objects.CCSinkResponder;
import logisticspipes.utils.SinkReply;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.item.ItemIdentifierStack;
import network.rs485.logisticspipes.module.Gui;
import network.rs485.logisticspipes.property.Property;
import network.rs485.logisticspipes.property.PropertyHolder;
import network.rs485.logisticspipes.property.UtilKt;

@CCType(name = "LogisticsModule")
public abstract class LogisticsModule implements ISaveState, ILPCCTypeHolder, PropertyHolder {

	private final Object[] ccTypeHolder = new Object[1];
	@Nullable
	protected IWorldProvider _world;
	@Nullable
	protected IPipeServiceProvider _service;
	protected ModulePositionType slot;
	protected int positionInt;
	protected boolean initialized;

	/**
	 * Registers the Inventory and ItemSender to the module
	 *
	 * @param world   that the module is in.
	 * @param service Inventory access, power and utility functions provided by the pipe.
	 */
	public void registerHandler(IWorldProvider world, IPipeServiceProvider service) {
		_world = world;
		_service = service;
	}

	/**
	 * Returns the name this module is registered in LP with, as used in
	 * {@link logisticspipes.items.ItemModule#registerModule} and saved in {@link logisticspipes.LPItems#modules}.
	 */
	@Nonnull
	public abstract String getLPName();

	@Nonnull
	@Override
	public List<Property<?>> getProperties() {
		return Collections.emptyList();
	}

	/**
	 * Registers the slot type the module is in
	 */
	public void registerPosition(@Nonnull ModulePositionType slot, int positionInt) {
		this.slot = slot;
		this.positionInt = positionInt;
	}

	@Nonnull
	public BlockPos getBlockPos() {
		final IPipeServiceProvider service = _service;
		if (service == null) {
			if (LogisticsPipes.isDEBUG()) {
				throw new IllegalStateException("Module has no service, but getBlockPos was called");
			}
			return BlockPos.ORIGIN;
		} else if (slot.isInWorld()) {
			return service.getPos();
		} else {
			if (LogisticsPipes.isDEBUG()) {
				throw new IllegalStateException("Module is not in world, but getBlockPos was called");
			}
			return BlockPos.ORIGIN;
		}
	}

	@Nullable
	public World getWorld() {
		final IWorldProvider worldProvider = _world;
		if (worldProvider == null) return null;
		return worldProvider.getWorld();
	}

	public ModulePositionType getSlot() {
		return this.slot;
	}

	public int getPositionInt() {
		return this.positionInt;
	}

	/**
	 * Gives an sink answer on the given itemstack
	 *
	 * @param stack              to sink
	 * @param item               to sink
	 * @param bestPriority       best priority seen so far
	 * @param bestCustomPriority best custom subpriority
	 * @param allowDefault       is a default only sink allowed to sink this?
	 * @param includeInTransit   inclide the "in transit" items? -- true for a destination
	 *                           search, false for a sink check.
	 * @param forcePassive       check for passive routing only, in case this method is redirected to other sinks
	 * @return SinkReply whether the module sinks the item or not
	 */
	public SinkReply sinksItem(@Nonnull ItemStack stack, ItemIdentifier item, int bestPriority, int bestCustomPriority,
			boolean allowDefault, boolean includeInTransit, boolean forcePassive) {
		return null;
	}

	/**
	 * A tick for the Module
	 */
	public abstract void tick();

	/**
	 * Is this module interested in all items, or just some specific ones?
	 *
	 * @return true: this module will be checked against every item request
	 * false: only requests involving items collected by {@link #collectSpecificInterests(Collection)} will be checked
	 */
	public abstract boolean hasGenericInterests();

	/**
	 * Collects the items which this module is capable of providing or supplying
	 * (or is otherwise interested in)
	 *
	 * @param itemidCollection the collection to add the interests to
	 */
	public void collectSpecificInterests(@Nonnull Collection<ItemIdentifier> itemidCollection) {
	}

	public abstract boolean interestedInAttachedInventory();

	/**
	 * is this module interested in receiving any damage variant of items in the
	 * attached inventory?
	 */
	public abstract boolean interestedInUndamagedID();

	/**
	 * is this module a valid destination for bounced items.
	 */
	public abstract boolean recievePassive();

	/**
	 * Returns whether the module should be displayed the effect when as an
	 * item.
	 *
	 * @return True to show effect False to no effect (default)
	 */
	public boolean hasEffect() {
		return false;
	}

	public List<CCSinkResponder> queueCCSinkEvent(ItemIdentifierStack item) {
		return new ArrayList<>(0);
	}

	@CCCommand(description = "Returns true if the Pipe has a gui")
	public boolean hasGui() {
		return this instanceof Gui;
	}

	@Nonnull
	public LogisticsModule getModule() {
		return this;
	}

	@Nonnull
	protected ISlotUpgradeManager getUpgradeManager() {
		return Objects.requireNonNull(_service, "service object was null in " + this)
				.getUpgradeManager(slot, positionInt);
	}

	@Override
	public String toString() {
		String at = "{service is null}";
		if (_service != null) {
			at = Objects.toString(_service.getPos());
		}
		String in = "{world is null}";
		if (_world != null) {
			in = Objects.toString(_world.getWorld());
		}
		return String.format("%s at %s in %s", getClass().getName(), at, in);
	}

	@Override
	public Object[] getTypeHolder() {
		return ccTypeHolder;
	}

	public void finishInit() {
		if (initialized) {
			if (LogisticsPipes.isTesting()) {
				throw new IllegalStateException("finishInit called on initialized " + getClass().getName());
			} else if (LogisticsPipes.isDEBUG()) {
				System.err.println("finishInit called on initialized " + getClass().getName());
				new Exception().printStackTrace();
			}
			return;
		}
		if (_service != null) {
			final IBlockAccess blockAccess = _world == null ? null : _world.getWorld();
			MainProxy.runOnServer(blockAccess, () -> () ->
					UtilKt.addObserver(getProperties(), (prop) -> {
						_service.markTileDirty();
						return Unit.INSTANCE;
					})
			);
		}
		initialized = true;
	}

	public enum ModulePositionType {
		SLOT(true), IN_HAND(false), IN_PIPE(true);

		@Getter
		private final boolean inWorld;

		ModulePositionType(boolean inWorld) {
			this.inWorld = inWorld;
		}
	}

}
