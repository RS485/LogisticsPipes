/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.routing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import logisticspipes.interfaces.routing.IDirectConnectionManager;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.proxy.MainProxy;


public class RouterManager implements IRouterManager, IDirectConnectionManager {
	
	private final List<IRouter> _routersClient = new ArrayList<IRouter>();
	private final HashMap<UUID, IRouter> _routersServer = new HashMap<UUID, IRouter>();
	
	private final ArrayList<DirectConnection> connectedPipes = new ArrayList<DirectConnection>();

	private long lastRouterAdded = -1;
	private static int DELAY_TIME = 2 * 1000;

	@Override
	public IRouter getRouter(UUID id){
		if(MainProxy.isClient()) {
			synchronized (_routersClient) {
				for(IRouter router:_routersClient) {
					if(router.getId().equals(id)) {
						return router;
					}
				}
				
			}
			return null;
		} else {
			return _routersServer.get(id);
		}
	}
	
	@Override
	public void removeRouter(UUID id) {
		if(MainProxy.isClient()) {
			IRouter remove = null;
			synchronized (_routersClient) {
				for(IRouter router:_routersClient) {
					if(router.getId().equals(id)) {
						remove = router;
					}
				}
				if (remove != null){
					_routersClient.remove(remove);
				}
			}
		} else {
			if (_routersServer.containsKey(id)){
				_routersServer.remove(id);
			}
		}
	}

	@Override
	public IRouter getOrCreateRouter(UUID id, int dimension, int xCoord, int yCoord, int zCoord) {
		IRouter r = this.getRouter(id);
		if (r == null){
			if(MainProxy.isClient()) {
				r = new ClientRouter(id, dimension, xCoord, yCoord, zCoord);
				synchronized (_routersClient) {
					_routersClient.add(r);
				}
			} else {
				r = new ServerRouter(id, dimension, xCoord, yCoord, zCoord);
				_routersServer.put(id, r);
			}
			lastRouterAdded = System.currentTimeMillis();
		}
		return r;
	}
	
	@Override
	public boolean isRouter(UUID id) {
		if(MainProxy.isClient()) {
			synchronized (_routersClient) {
				for(IRouter router:_routersClient) {
					if(router.getId().equals(id)) {
						return true;
					}
				}
			}
			return false;
		} else {
			return _routersServer.containsKey(id);
		}
	}
	
	@Override
	public Map<UUID, IRouter> getRouters() {
		if(MainProxy.isClient()) {
			Map<UUID, IRouter> map = new HashMap<UUID, IRouter>();
			synchronized (_routersClient) {
				for(IRouter router:_routersClient) {
					map.put(router.getId(), router);
				}
			}
			return Collections.unmodifiableMap(map);
		} else {
			return Collections.unmodifiableMap(_routersServer);
		}
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
	public boolean addDirectConnection(UUID ident, IRouter router) {
		if(MainProxy.isClient()) return false;
		boolean added = false;
		for(DirectConnection con:connectedPipes) {
			if(!ident.equals(con.identifier)) {
				if(con.Router1 != null && con.Router1.equals(router.getId())) {
					con.Router1 = null;
				} else if(con.Router2 != null && con.Router2.equals(router.getId())) {
					con.Router2 = null;
				}
			} else {
				if(con.Router1 == null || con.Router1.equals(router.getId())) {
					con.Router1 = router.getId();
					added = true;
					break;
				} else if(con.Router2 == null || con.Router2.equals(router.getId())) {
					con.Router2 = router.getId();
					added = true;
					break;
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

	@Override
	public CoreRoutedPipe getConnectedPipe(IRouter router) {
		UUID id=null;
		for(DirectConnection con:connectedPipes) {
			if(con.Router1 != null && con.Router2 != null) {
				if(con.Router1.equals(router.getId())) {
					id = con.Router2;
					break;
				} else if(con.Router2.equals(router.getId())) {
					id = con.Router1;
					break;
				}
			}
		}
		if(id == null) {
			return null;
		}
		IRouter r = getRouter(id);
		if(r == null) return null;
		return r.getPipe();
	}

	@Override
	public void removeDirectConnection(IRouter router) {
		if(MainProxy.isClient()) return;
		for(DirectConnection con:connectedPipes) {
			if(con.Router1 != null && con.Router1.equals(router.getId())) {
				con.Router1 = null;
			} else if(con.Router2 != null && con.Router2.equals(router.getId())) {
				con.Router2 = null;
			}
		}
	}

	@Override
	public void serverStopClean() {
		connectedPipes.clear();
		_routersServer.clear();
		lastRouterAdded = -1;
	}

	@Override
	public boolean routerAddingDone() {
		return lastRouterAdded != -1 && lastRouterAdded + DELAY_TIME < System.currentTimeMillis();
	}

	@Override
	public void clearClientRouters() {
		synchronized (_routersClient) {
			_routersClient.clear();
		}
	}
}
