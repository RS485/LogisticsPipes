package logisticspipes.modules;

import java.util.Collection;

import logisticspipes.api.IRoutedPowerProvider;
import logisticspipes.interfaces.ISendRoutedItem;
import logisticspipes.interfaces.IWorldProvider;
import logisticspipes.interfaces.routing.ISaveState;
import logisticspipes.logisticspipes.IInventoryProvider;
import logisticspipes.proxy.cc.interfaces.CCCommand;
import logisticspipes.proxy.cc.interfaces.CCType;
import logisticspipes.utils.ItemIdentifier;
import logisticspipes.utils.SinkReply;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.util.Icon;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@CCType(name="LogisticsModule")
public abstract class LogisticsModule implements ISaveState {
	/**
	 * Registers the Inventory and ItemSender to the module
	 * @param invProvider The connected inventory
	 * @param itemSender the handler to send items into the logistics system
	 * @param world that the module is in.
	 */
	public abstract void registerHandler(IInventoryProvider invProvider, ISendRoutedItem itemSender, IWorldProvider world, IRoutedPowerProvider powerProvider);
	
	/**
	 * Registers the slot the module is in
	 * Negative numbers indicate an in-hand inventory, not in a gui or chassi 
	 */
	public abstract void registerSlot(int slot);
	
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
	 * @param stack to sink
	 * @param bestPriority best priority seen so far
	 * @param bestCustomPriority best custom subpriority
	 * @param allowDefault is a default only sink allowed to sink this?
	 * @param includeInTransit inclide the "in transit" items? -- true for a destination search, false for a sink check.
	 * @return SinkReply whether the module sinks the item or not
	 */
	public abstract SinkReply sinksItem(ItemIdentifier stack, int bestPriority, int bestCustomPriority, boolean allowDefault, boolean includeInTransit);
	
	/**
	 * Returns submodules. Normal modules don't have submodules 
	 * @param slotnumber of the requested module
	 * @return
	 */
	public abstract LogisticsModule getSubModule(int slot);

	/**
	 * A tick for the Module
	 */
	public abstract void tick();

	/**
	 * Is this module interested in all items, or just some specific ones?
	 * @return true: this module will be checked against every item request
	 * 		  false: only requests involving items returned by getSpecificInterestes() will be checked
	 */
	public abstract boolean hasGenericInterests();

	/**
	 * the list of items which this module is capable of providing or supplying (or is otherwise interested in)
	 * the size of the list here does not influence the ongoing computational cost.
	 * @return
	 */
	public abstract Collection<ItemIdentifier> getSpecificInterests();

	public abstract boolean interestedInAttachedInventory();

	/**
	 * is this module interested in recieving any damage varient of items in the attached inventory?
	 */
	public abstract boolean interestedInUndamagedID();

	/**
	 * is this module a valid destination for bounced items.
	 */
	public abstract boolean recievePassive();
	
	/**
	 * get The Icon for this Module Class
	 * @return
	 */
	@SideOnly(Side.CLIENT)
	public abstract Icon getIconTexture(IconRegister register);
	
	@CCCommand(description="Returns if the Pipe has a gui")
	public boolean hasGui() {
		return false;
	}
}
