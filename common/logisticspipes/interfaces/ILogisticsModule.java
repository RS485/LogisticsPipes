package logisticspipes.interfaces;

import java.util.Collection;

import logisticspipes.api.IRoutedPowerProvider;
import logisticspipes.interfaces.routing.ISaveState;
import logisticspipes.logisticspipes.IInventoryProvider;
import logisticspipes.utils.ItemIdentifier;
import logisticspipes.utils.SinkReply;

public interface ILogisticsModule extends ISaveState {
	/**
	 * Registers the Inventory and ItemSender to the module
	 * @param invProvider The connected inventory
	 * @param itemSender the handler to send items into the logistics system
	 * @param world that the module is in.
	 */
	public void registerHandler(IInventoryProvider invProvider, ISendRoutedItem itemSender, IWorldProvider world, IRoutedPowerProvider powerProvider);
	
	/**
	 * Registers the position to the module
	 */
	public void registerPosition(int xCoord, int yCoord, int zCoord, int slot);
	
	/**
	 * Gives an sink answer on the given itemstack 
	 * @param stack to sink
	 * @param bestPriority best priority seen so far
	 * @param bestCustomPriority best custom subpriority
	 * @return SinkReply whether the module sinks the item or not
	 */
	public SinkReply sinksItem(ItemIdentifier stack, int bestPriority, int bestCustomPriority);
	
	/**
	 * Returns submodules. Normal modules don't have submodules 
	 * @param slotnumber of the requested module
	 * @return
	 */
	public ILogisticsModule getSubModule(int slot);

	/**
	 * A tick for the Module
	 */
	public void tick();

	/**
	 * Is this module interested in all items, or just some specific ones?
	 * @return true: this module will be checked against every item request
	 * 		  false: only requests involving items returned by getSpecificInterestes() will be checked
	 */
	public boolean hasGenericInterests();

	/**
	 * the list of items which this module is capable of providing or supplying (or is otherwise interested in)
	 * the size of the list here does not influence the ongoing computational cost.
	 * @return
	 */
	public Collection<ItemIdentifier> getSpecificInterests();

	public boolean interestedInAttachedInventory();

	/**
	 * is this module interested in recieving any damage varient of items in the attached inventory?
	 */
	public boolean interestedInUndamagedID();

	/**
	 * is this module a valid destination for bounced items.
	 */
	public boolean recievePassive();
}
