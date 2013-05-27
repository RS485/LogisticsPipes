package logisticspipes.interfaces;

import java.util.Collection;

import logisticspipes.api.IRoutedPowerProvider;
import logisticspipes.interfaces.routing.ISaveState;
import logisticspipes.logisticspipes.IInventoryProvider;
import logisticspipes.utils.ItemIdentifier;
import logisticspipes.utils.SinkReply;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.util.Icon;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public interface ILogisticsModule extends ISaveState {
	/**
	 * Registers the Inventory and ItemSender to the module
	 * @param invProvider The connected inventory
	 * @param itemSender the handler to send items into the logistics system
	 * @param world that the module is in.
	 */
	public void registerHandler(IInventoryProvider invProvider, ISendRoutedItem itemSender, IWorldProvider world, IRoutedPowerProvider powerProvider);
	
	/**
	 * Registers the slot the module is in
	 * Negative numbers indicate an in-hand inventory, not in a gui or chassi 
	 */
	public void registerSlot(int slot);
	
	/**
	 * typically returns the coord of the pipe that holds it.
	 */
	public int getX();
	/**
	 * typically returns the coord of the pipe that holds it.
	 */
	public int getY();
	/**
	 * typically returns the coord of the pipe that holds it.
	 */
	public int getZ();
	
	/**
	 * Gives an sink answer on the given itemstack 
	 * @param stack to sink
	 * @param bestPriority best priority seen so far
	 * @param bestCustomPriority best custom subpriority
	 * @param allowDefault is a default only sink allowed to sink this?
	 * @param includeInTransit inclide the "in transit" items? -- true for a destination search, false for a sink check.
	 * @return SinkReply whether the module sinks the item or not
	 */
	public SinkReply sinksItem(ItemIdentifier stack, int bestPriority, int bestCustomPriority, boolean allowDefault, boolean includeInTransit);
	
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
	
	/**
	 * get The Icon for this Module Class
	 * @return
	 */
	@SideOnly(Side.CLIENT)
	public Icon getIconTexture(IconRegister register);
}
