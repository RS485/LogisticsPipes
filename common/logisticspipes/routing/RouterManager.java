/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.routing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import logisticspipes.interfaces.routing.IDirectConnectionManager;
import logisticspipes.utils.Pair;


public class RouterManager implements IRouterManager, IDirectConnectionManager {
	private final static HashMap<UUID, Router> _routers = new HashMap<UUID, Router>();
	private final static ArrayList<DirectConnection> connectedPipes = new ArrayList<DirectConnection>();
	
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
		}
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

	@Override
	public boolean hasDirectConnection(IRouter router) {
		for(DirectConnection con:connectedPipes) {
			if(con.Router1 != null && con.Router2 != null) {
				if(con.Router1.equals(router.getId())) {
					return true;
				} else if(con.Router2.equals(router.getId())) {
					return true;
				}
			}
		}
		return false;
	}
	
	@Override
	public boolean addDirectConnection(String ident, IRouter router) {
		boolean added = false;
		for(DirectConnection con:connectedPipes) {
			if(!ident.equals(con.identifier)) {
				if(con.Router1.equals(router.getId())) {
					con.Router1 = null;
				} else if(con.Router2.equals(router.getId())) {
					con.Router2 = null;
				}
			} else {
				if(con.Router1 == null) {
					con.Router1 = router.getId();
					added = true;
				} else if(con.Router2 == null) {
					con.Router2 = router.getId();
					added = true;
				} else {
					return false;
				}
			}
		}
		if(!added) {
			DirectConnection Dc = new DirectConnection();
			connectedPipes.add(Dc);
			Dc.identifier = ident;
			Dc.Router1 = router.getId();
		}
		return true;
	}
}
