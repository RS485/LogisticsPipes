package logisticspipes.interfaces;

import logisticspipes.interfaces.routing.ISaveState;
import logisticspipes.logisticspipes.IInventoryProvider;
import logisticspipes.utils.SinkReply;
import net.minecraft.item.ItemStack;

public interface ILogisticsModule extends ISaveState {
	/**
	 * Registers the Inventory and ItemSender to the module
	 * @param invProvider The connected inventory
	 * @param itemSender the handler to send items into the logistics system
	 * @param world that the module is in.
	 */
	public void registerHandler(IInventoryProvider invProvider, ISendRoutedItem itemSender, IWorldProvider world, IChassiePowerProvider powerProvider);
	
	/**
	 * Registers the position to the module
	 */
	public void registerPosition(int xCoord, int yCoord, int zCoord, int slot);
	
	/**
	 * Gives an sink answer on the given itemstack 
	 * @param item to sink
	 * @return SinkReply wether the module sinks the item or not
	 */
	public SinkReply sinksItem(ItemStack item);
	
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
}
