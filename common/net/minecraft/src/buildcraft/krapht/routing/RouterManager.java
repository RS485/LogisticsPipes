/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.krapht.routing;

import java.util.HashMap;
import java.util.UUID;

import net.minecraft.src.World;


public class RouterManager implements IRouterManager{
	private final static HashMap<UUID, Router> _routers = new HashMap<UUID, Router>();
	
	//DO ONLY USE FROM PURE ROUTER OBJECTS!
	public static Router get(UUID id){
		return _routers.get(id);
	}
	
	static void removeRouter(UUID id) {
		if (_routers.containsKey(id)){
			_routers.remove(id);
		}
	}

	@Override
	public IRouter getOrCreateRouter(UUID id, int dimension, int xCoord, int yCoord, int zCoord) {
		IRouter r = this.getRouter(id);
		if (r == null){
			r = new Router(id, dimension, xCoord, yCoord, zCoord);
			_routers.put(id, (Router)r);
		}/* else if (r instanceof Router) {
			((Router)r).reloadPipe(worldObj, xCoord, yCoord, zCoord);
		}*/
		return r;
	}
	
	@Override
	public IRouter getRouter(UUID id){
		return _routers.get(id);
	}



	@Override
	public boolean isRouter(UUID id) {
		return _routers.containsKey(id);
	}
}
