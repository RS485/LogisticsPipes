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
	private final ArrayList<IRouter> _routersServer = new ArrayList<IRouter>();
	
	private final ArrayList<DirectConnection> connectedPipes = new ArrayList<DirectConnection>();

	private long lastRouterAdded = -1;
	private static int DELAY_TIME = 2 * 1000;

	@Override
	public IRouter getRouter(int id){
		if(MainProxy.isClient()) {
			synchronized (_routersClient) {
				for(IRouter router:_routersClient) {
					if(router.getSimpleID() == id) {
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
	public void removeRouter(int id) {
		if(MainProxy.isClient()) {
			IRouter remove = null;
			synchronized (_routersClient) {
				for(IRouter router:_routersClient) {
					if(router.getSimpleID() == id) {
						remove = router;
					}
				}
				if (remove != null){
					_routersClient.remove(remove);
				}
			}
		} else {
			if (_routersServer.get(id) != null){
				_routersServer.remove(id);
			}
		}
	}

	@Override
	public IRouter getOrCreateRouter(int id, int dimension, int xCoord, int yCoord, int zCoord) {
		IRouter r = this.getRouter(id);
		if (r == null){
			if(MainProxy.isClient()) {
				r = new ClientRouter(id, dimension, xCoord, yCoord, zCoord);
				synchronized (_routersClient) {
					_routersClient.add(r);
				}
			} else {
				r = new ServerRouter(id, dimension, xCoord, yCoord, zCoord);
				_routersServer.set(id, r);
			}
			lastRouterAdded = System.currentTimeMillis();
		}
		return r;
	}
	
	@Override
	public boolean isRouter(int id) {
		if(MainProxy.isClient()) {
			synchronized (_routersClient) {
				for(IRouter router:_routersClient) {
					if(router.getSimpleID() == id) {
						return true;
					}
				}
			}
			return false;
		} else {
			return _routersServer.get(id)!=null;
		}
	}
	
	@Override
	public List<IRouter> getRouters() {
		if(MainProxy.isClient()) {
			return Collections.unmodifiableList(_routersClient);
		} else {
			return Collections.unmodifiableList(_routersServer);
		}
	}

	@Override
	public boolean hasDirectConnection(IRouter router) {
		for(DirectConnection con:connectedPipes) {
			if(con.Router1 >= 0 && con.Router2 >= 0) {
				if(con.Router1 == router.getSimpleID()) {
					return true;
				} else if(con.Router2 == router.getSimpleID()) {
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
			if(ident != con.identifier) {
				if(con.Router1 >= 0 && con.Router1 == router.getSimpleID()) {
					con.Router1 = -1;
				} else if(con.Router2 >= 0 && con.Router2 == router.getSimpleID()) {
					con.Router2 = -1;
				}
			} else {
				if(con.Router1 <0  || con.Router1 == router.getSimpleID()) {
					con.Router1 = router.getSimpleID();
					added = true;
					break;
				} else if(con.Router2 < 0 || con.Router2 == router.getSimpleID()) {
					con.Router2 = router.getSimpleID();
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
			Dc.Router1 = router.getSimpleID();
		}
		return true;
	}
	
	@Override
	public CoreRoutedPipe getConnectedPipe(IRouter router) {
		int id=-1;
		for(DirectConnection con:connectedPipes) {
			if(con.Router1 >= 0 && con.Router2 >= 0) {
				if(con.Router1 == router.getSimpleID()) {
					id = con.Router2;
					break;
				} else if(con.Router2 == router.getSimpleID()) {
					id = con.Router1;
					break;
				}
			}
		}
		if(id < 0) {
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
			if(con.Router1 >= 0 && con.Router1 == router.getSimpleID()) {
				con.Router1 = -1;
			} else if(con.Router2 >= 0 && con.Router2 == router.getSimpleID()) {
				con.Router2 = -1;
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
