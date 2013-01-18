/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.routing;

import java.util.ArrayList;
import java.util.List;


public interface IRouterManager {
	public IRouter getOrCreateRouter(int id, int dimension, int xCoord, int yCoord, int zCoord);
	public IRouter getRouter(int id);
	public boolean isRouter(int id);
	public void removeRouter(int id);
	public List<IRouter> getRouters();
	public void serverStopClean();
	public boolean routerAddingDone();
	public void clearClientRouters();
}
