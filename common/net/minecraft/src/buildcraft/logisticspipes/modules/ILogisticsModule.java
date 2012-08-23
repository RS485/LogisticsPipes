package net.minecraft.src.buildcraft.logisticspipes.modules;

import net.minecraft.src.ItemStack;
import net.minecraft.src.World;
import net.minecraft.src.buildcraft.krapht.ISaveState;
import net.minecraft.src.buildcraft.logisticspipes.IInventoryProvider;

public interface ILogisticsModule extends ISaveState {
	/**
	 * Registers the Inventory and ItemSender to the module
	 * @param invProvider The connected inventory
	 * @param itemSender the handler to send items into the logistics system
	 * @param world that the module is in.
	 */
	public void registerHandler(IInventoryProvider invProvider, ISendRoutedItem itemSender, IWorldProvider world);
	
	/**
	 * 
	 * @return The gui id of the given module; 
	 */
	public int getGuiHandlerID();
	
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
