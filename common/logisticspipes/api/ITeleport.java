package logisticspipes.api;
 
import java.util.List;
 
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
 
import buildcraft.api.transport.IPipedItem;
import buildcraft.api.transport.IPipeEntry;
import buildcraft.api.transport.IPipeConnection;
 
/** implemented by some Logistics pipes, and anyone else who cares to notice ITeleporters changing next to them.
 * extends IPipeEntry, to aid in efficient finding of destinations.
  */
public interface ICareAboutTeleport extends IPipeEntry  {
        /* notify this pipe that teleport destinations have changed.
         * @param changed the adjacent ITeleport that now links somewhere else.
         */
        void onAdjacentITeleportRouteChange(ITeleport changed);
 
        /* used if the adjacent ITeleport becomes temporarily disabled
         * (full, unpowered, turned off with redstone, etc..)
         * does not (immediately) trigger a routing update. (prolonged deactivation may)
         **** Tentative function, may not exist in final API
         */  
        void onAdjacentITeleportTemporaryDisable(ITeleport changed,boolean isEnabled);
}
 
/**
 * Public interface implemented by TilesEntities which provide Teleport capabilities, and wish
 * Logistics pipes to route items via their network.
 * if you wish to avoid items being sentTo invalid destinations, then you must notify your neigbours of changes.
 * (via the proposed ICareAboutTeleport interface)
 */
public interface ITeleport {
       
        /**
         * For finding the list of places this ITeleport sends items to - used for routing
         * @return the list of locations an item sent to this ITeleport can end up.
         * A receive only ITeleport can return null or an empty list.
         * the list returned WILL NOT be modified by users of the ITeleport.
         * (return an Collection.unmodifiableCollection if you wish to enforce that)
         */
        Collection<ITeleport> sendsTo();
       
        /**
         * For finding the list of places this ITeleport receives items from - used for routing
         * @return the list of locations an item sent to this ITeleport came from.
         * A send only ITeleport can return null or an empty list.
         * the list returned WILL NOT be modified by users of the ITeleport.
         * (return an Collection.unmodifiableCollection if you wish to enforce that)
         *
         */
        Collection<ITeleport> receivesFrom();
       
        /**
         * Is this ITeleport capable of sending to a specific destination out of a list?
         * @return if true, then sendTo(ITeleport ,item) should send the item to that destination.
         */
        boolean canSendToSpecific();
       
        /**
         * sendTo(index,items) should send the items to that index on the sendsTo list.
         * When this function is called the ITeleport takes complete responsibility for the item.
         * The logistics routing code will expect to see this item arrive at the destination expected.
         * Items arriving at an invalid destination will be probably be sent back into the teleport network, if the destination declares that it sendsTo the expected destination.
         * Because the network can change at any time, without the attached pipes knowing, an invalid index is not an error the same tick, or shortly after, an ITeleport network change.
         * If the index is invalid, the item should not be destroyed. Teleporting to "self" (ie, rejecting the item, just spitting it out), or teleporting to a random destination are both acceptable behaviors.
         * items is an IPipedItems, so that routing information doesn't get lost transitioning through the network.
         * @return were you able to accept the item (if you return true the network will expect to see the item come out of the requested destination).
         */
        boolean sendTo(ITeleport destination, IPipedItem items);
//      boolean sendTo(ITeleport destination, ItemStack items);
 
        /** There needs to be some way to get adjacent pipes, so that a logical connection can go
         * pipe - ITeleport ~~~ ITeleport - pipe, and the 2 pipes can find each other.
         * this only needs to return the pipes adjacent to this ITeleport, not any other.
         * the returned list WILL NOT be modified by external code.(return an
         * Collection.unmodifiableCollection if you wish to enforce that)
         */
        Collection<IPipeConnection> getAdjacentPipes();
}