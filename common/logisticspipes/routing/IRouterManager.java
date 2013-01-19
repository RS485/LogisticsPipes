/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.routing;

import java.util.Map;
import java.util.UUID;

import net.minecraftforge.common.ForgeDirection;


public interface IRouterManager {
	public IRouter getOrCreateRouter(UUID id, int dimension, int xCoord, int yCoord, int zCoord);
	public IRouter getOrCreateFirewallRouter(UUID id, int dimension, int xCoord, int yCoord, int zCoord, ForgeDirection dir);
	public IRouter getRouter(UUID id);
	public boolean isRouter(UUID id);
	public void removeRouter(UUID id);
	public Map<UUID, IRouter> getRouters();
	public void serverStopClean();
	public boolean routerAddingDone();
	public void clearClientRouters();
}
