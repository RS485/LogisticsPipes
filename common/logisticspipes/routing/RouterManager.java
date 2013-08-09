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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

import logisticspipes.blocks.LogisticsSecurityTileEntity;
import logisticspipes.interfaces.ISecurityStationManager;
import logisticspipes.interfaces.routing.IDirectConnectionManager;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.block.SecurityStationAuthorizedList;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.proxy.MainProxy;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.ForgeDirection;
import cpw.mods.fml.common.network.Player;


public class RouterManager implements IRouterManager, IDirectConnectionManager, ISecurityStationManager {
	
	private final ArrayList<IRouter> _routersClient = new ArrayList<IRouter>();
	private final ArrayList<IRouter> _routersServer = new ArrayList<IRouter>();
	private final Map<UUID,Integer> _uuidMap= new HashMap<UUID,Integer>();
	
	private final WeakHashMap<LogisticsSecurityTileEntity, Void> _security= new WeakHashMap<LogisticsSecurityTileEntity, Void>();
	private List<String> _authorized = new LinkedList<String>();
	
	private final ArrayList<DirectConnection> connectedPipes = new ArrayList<DirectConnection>();

	@Override
	public IRouter getRouter(int id){
		//TODO: isClient without a world is expensive
		if(id<=0 || MainProxy.isClient()) {
			return null;
		} else {
			return _routersServer.get(id);
		}
	}
	@Override
	public
	IRouter getRouterUnsafe(Integer id, boolean side) {
		if(side || id<=0) {
			return null;
		} else {
			return _routersServer.get(id);
		}
	}
	@Override
	public int getIDforUUID(UUID id){
		if(id==null)
			return -1;
		Integer iId=_uuidMap.get(id);
		if(iId == null)
			return -1;
		return iId;
	}
	@Override
	public void removeRouter(int id) {
		//TODO: isClient without a world is expensive
		if(!MainProxy.isClient()) {
			_routersServer.set(id,null);
		}
	}

	@Override
	public IRouter getOrCreateRouter(UUID UUid, int dimension, int xCoord, int yCoord, int zCoord, boolean forceCreateDuplicate) {
		IRouter r = null;
		int id=this.getIDforUUID(UUid);
		if(id>0)
			this.getRouter(id);
		if (r == null || !r.isAt(dimension, xCoord, yCoord, zCoord)){
			if(MainProxy.isClient()) {
				synchronized (_routersClient) {
					for (IRouter r2:_routersClient)
						if (r2.isAt(dimension, xCoord, yCoord, zCoord))
							return r2;
					r = new ClientRouter(UUid, dimension, xCoord, yCoord, zCoord);
					_routersClient.add(r);
				}
			} else {
				synchronized (_routersServer) {
					if(!forceCreateDuplicate)
						for (IRouter r2:_routersServer)
							if (r2 != null && r2.isAt(dimension, xCoord, yCoord, zCoord))
								return r2;
					r = new ServerRouter(UUid, dimension, xCoord, yCoord, zCoord);
					
					int rId= r.getSimpleID();
					if(_routersServer.size()>rId)
						_routersServer.set(rId, r);
					else {
						_routersServer.ensureCapacity(rId+1);
						while(_routersServer.size()<=rId)
							_routersServer.add(null);
						_routersServer.set(rId, r);
					}
					this._uuidMap.put(r.getId(), r.getSimpleID());
				}
			}
		}
		return r;
	}

	@Override
	public IRouter getOrCreateFirewallRouter(UUID UUid, int dimension, int xCoord, int yCoord, int zCoord, ForgeDirection dir, IRouter[] otherRouters) {
		IRouter r = null;
		int id=this.getIDforUUID(UUid);
		if(id>0)
			this.getRouter(id);
		if (r == null || !r.isAt(dimension, xCoord, yCoord, zCoord)){
			if(MainProxy.isClient()) {
				r = new ClientRouter(UUid, dimension, xCoord, yCoord, zCoord);
				synchronized (_routersClient) {
					_routersClient.add(r);
				}
			} else {
				synchronized (_routersServer) {
					r = new FilteringRouter(UUid, dimension, xCoord, yCoord, zCoord, dir, otherRouters);
					int rId= r.getSimpleID();
					if(_routersServer.size()>rId)
						_routersServer.set(rId, r);
					else {
						_routersServer.ensureCapacity(rId+1);
						while(_routersServer.size()<=rId)
							_routersServer.add(null);
						_routersServer.set(rId, r);
					}
					this._uuidMap.put(r.getId(), r.getSimpleID());
				}
			}
		}
		return r;
	}
	
	@Override
	public boolean isRouter(int id) {
		if(MainProxy.isClient()) {
			return true;
		} else {
			return _routersServer.get(id)!=null;
		}
	}
	
	/**
	 * This assumes you know what you are doing. expect exceptions to be thrown if you pass the wrong side.
	 * @param id
	 * @param side false for server, true for client. 
	 * @return is this a router for the side.
	 */
	@Override
	public boolean isRouterUnsafe(int id,boolean side) {
		if(side) {
			return true;
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
			if(!ident.equals(con.identifier)) {
				if(con.Router1 >= 0 && con.Router1 == router.getSimpleID()) {
					con.Router1 = -1;
				} else if(con.Router2 >= 0 && con.Router2 == router.getSimpleID()) {
					con.Router2 = -1;
				}
			} else {
				if(con.Router1 < 0 || con.Router1 == router.getSimpleID()) {
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
		_uuidMap.clear();
		_security.clear();
	}

	@Override
	public void clearClientRouters() {
		synchronized (_routersClient) {
			_routersClient.clear();
		}
	}
	
	@Override
	public void add(LogisticsSecurityTileEntity tile) {
		_security.put(tile, null);
		authorizeUUID(tile.getSecId());
	}
	
	@Override
	public LogisticsSecurityTileEntity getStation(UUID id) {
		if(id == null) return null;
		for(LogisticsSecurityTileEntity tile:_security.keySet()) {
			if(id.equals(tile.getSecId())) {
				return tile;
			}
		}
		return null;
	}
	
	@Override
	public void remove(LogisticsSecurityTileEntity tile) {
		_security.remove(tile);
		deauthorizeUUID(tile.getSecId());
	}

	@Override
	public void dimensionUnloaded(int dim) {
		synchronized (_routersServer) {
			for (IRouter r:_routersServer) {
				if(r != null && r.isInDim(dim)) {
					r.clearPipeCache();
					r.clearInterests();
				}
			}
		}
	}
	
	@Override
	public void deauthorizeUUID(UUID id) {
		if (_authorized.contains(id.toString())) {
			_authorized.remove(id.toString());
		}
		sendClientAuthorizationList();
	}
	
	@Override
	public void authorizeUUID(UUID id) {
		if (!_authorized.contains(id.toString())) {
			_authorized.add(id.toString());
		}
		sendClientAuthorizationList();
	}
	
	@Override
	public boolean isAuthorized(UUID id) {
		if (_authorized.isEmpty() || id == null) return false;
		return _authorized.contains(id.toString());
	}
	
	@Override
	public boolean isAuthorized(String id) {
		if (_authorized.isEmpty() || id == null) return false;
		return _authorized.contains(id);
	}
	
	@Override
	public void setClientAuthorizationList(List<String> list) {
		this._authorized = list;
	}
	
	@Override
	public void sendClientAuthorizationList() {
//TODO 	MainProxy.sendToAllPlayers(new PacketStringList(NetworkConstants.SECURITY_AUTHORIZEDLIST_UPDATE, this._authorized).getPacket());		
		MainProxy.sendToAllPlayers(PacketHandler.getPacket(SecurityStationAuthorizedList.class).setStringList(this._authorized));
	}
	
	@Override
	public void sendClientAuthorizationList(EntityPlayer player) {
//TODO 	MainProxy.sendCompressedPacketToPlayer(new PacketStringList(NetworkConstants.SECURITY_AUTHORIZEDLIST_UPDATE, this._authorized).getPacket(), (Player)player);		
		MainProxy.sendPacketToPlayer(PacketHandler.getPacket(SecurityStationAuthorizedList.class).setStringList(this._authorized), (Player)player);
	}

	@Override
	public void printAllRouters() {
		for(IRouter router:_routersServer) {
			if(router != null) {
				System.out.println(router.toString());
			}
		}
	}
}
