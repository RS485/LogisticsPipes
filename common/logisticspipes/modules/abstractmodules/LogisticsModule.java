package logisticspipes.modules.abstractmodules;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import logisticspipes.interfaces.IPipeServiceProvider;
import logisticspipes.interfaces.IQueueCCEvent;
import logisticspipes.interfaces.IWorldProvider;
import logisticspipes.interfaces.routing.ISaveState;
import logisticspipes.proxy.computers.interfaces.CCCommand;
import logisticspipes.proxy.computers.interfaces.CCType;
import logisticspipes.proxy.computers.interfaces.ILPCCTypeHolder;
import logisticspipes.proxy.computers.objects.CCSinkResponder;
import logisticspipes.utils.SinkReply;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.item.ItemIdentifierStack;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import lombok.Getter;

@CCType(name = "LogisticsModule")
public abstract class LogisticsModule implements ISaveState, ILPCCTypeHolder {

	private Object ccType;

	protected IWorldProvider _world;
	protected IPipeServiceProvider _service;

	/**
	 * Registers the Inventory and ItemSender to the module
	 * 
	 * @param world
	 *            that the module is in.
	 * @param service
	 *            Inventory access, power and utility functions provided by the
	 *            pipe
	 */
	public void registerHandler(IWorldProvider world, IPipeServiceProvider service) {
		_world = world;
		_service = service;
	}

	@Getter
	protected ModulePositionType slot;
	@Getter
	protected int positionInt;

	/**
	 * Registers the slot type the module is in
	 */
	public void registerPosition(ModulePositionType slot, int positionInt) {
		this.slot = slot;
		this.positionInt = positionInt;
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

	/**
	 * typically returns the coord of the pipe that holds it.
	 */
	public abstract int getX();

	/**
	 * typically returns the coord of the pipe that holds it.
	 */
	public abstract int getY();

	/**
	 * typically returns the coord of the pipe that holds it.
	 */
	public abstract int getZ();

	/**
	 * Gives an sink answer on the given itemstack
	 * 
	 * @param stack
	 *            to sink
	 * @param bestPriority
	 *            best priority seen so far
	 * @param bestCustomPriority
	 *            best custom subpriority
	 * @param allowDefault
	 *            is a default only sink allowed to sink this?
	 * @param includeInTransit
	 *            inclide the "in transit" items? -- true for a destination
	 *            search, false for a sink check.
	 * @return SinkReply whether the module sinks the item or not
	 */
	public abstract SinkReply sinksItem(ItemIdentifier stack, int bestPriority, int bestCustomPriority, boolean allowDefault, boolean includeInTransit);

	/**
	 * Returns submodules. Normal modules don't have submodules
	 * 
	 * @param slotnumber
	 *            of the requested module
	 * @return
	 */
	public abstract LogisticsModule getSubModule(int slot);

	/**
	 * A tick for the Module
	 */
	public abstract void tick();

	/**
	 * Is this module interested in all items, or just some specific ones?
	 * 
	 * @return true: this module will be checked against every item request
	 *         false: only requests involving items returned by
	 *         getSpecificInterestes() will be checked
	 */
	public abstract boolean hasGenericInterests();

	/**
	 * the list of items which this module is capable of providing or supplying
	 * (or is otherwise interested in) the size of the list here does not
	 * influence the ongoing computational cost.
	 * 
	 * @return
	 */
	public abstract Collection<ItemIdentifier> getSpecificInterests();

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
	 * get The Icon for this Module Class
	 * 
	 * @return
	 */
	@SideOnly(Side.CLIENT)
	public abstract IIcon getIconTexture(IIconRegister register);

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
		return new ArrayList<CCSinkResponder>(0);
	}

	public void registerCCEventQueuer(IQueueCCEvent eventQueuer) {}

	@CCCommand(description = "Returns if the Pipe has a gui")
	public boolean hasGui() {
		return false;
	}

	@Override
	public String toString() {
		return (new StringBuilder()).append(getClass().getSimpleName()).append("@").append("(").append(getX()).append(", ").append(getY()).append(", ").append(getZ()).append(")").toString();
	}

	/**
	 * typically used when the neighboring block changes
	 */
	public void clearCache() {}

	@Override
	public void setCCType(Object type) {
		ccType = type;
	}

	@Override
	public Object getCCType() {
		return ccType;
	}
}
