package logisticspipes.api;

import buildcraft.api.transport.IPipeEntry;

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