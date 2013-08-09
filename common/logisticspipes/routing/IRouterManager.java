/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.routing;

import java.util.List;
import java.util.UUID;

import net.minecraftforge.common.ForgeDirection;


public interface IRouterManager {
	int getIDforUUID(UUID id);
	public IRouter getOrCreateRouter(UUID routerUUid, int dimension, int xCoord, int yCoord, int zCoord, boolean forceCreateDuplicateAtCoordinate);
	public IRouter getOrCreateFirewallRouter(UUID id, int dimension, int xCoord, int yCoord, int zCoord, ForgeDirection dir, IRouter[] otherRouters);
	public IRouter getRouter(int id);
	public boolean isRouter(int id);
	public void removeRouter(int id);
	public List<IRouter> getRouters();
	public void serverStopClean();
	public void clearClientRouters();
	public void dimensionUnloaded(int dim);

	boolean isRouterUnsafe(int id, boolean isClientSide);
	IRouter getRouterUnsafe(Integer value1, boolean isClientSide);
	void printAllRouters();
}
