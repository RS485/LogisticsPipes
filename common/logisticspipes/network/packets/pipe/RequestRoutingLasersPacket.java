package logisticspipes.network.packets.pipe;


import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import logisticspipes.Configs;
import logisticspipes.network.abstractpackets.CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.routing.ExitRoute;
import logisticspipes.routing.IPaintPath;
import logisticspipes.routing.IRouter;
import logisticspipes.routing.LaserData;
import logisticspipes.routing.PipeRoutingConnectionType;
import logisticspipes.routing.pathfinder.PathFinder;
import logisticspipes.utils.tuples.LPPosition;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class RequestRoutingLasersPacket extends CoordinatesPacket {
	
	private boolean firstPipe = false;
	
	public RequestRoutingLasersPacket(int id) {
		super(id);
	}
	
	@Override
	public void processPacket(EntityPlayer player) {
		return;
		//TODO
		/*
		LogisticsTileGenericPipe tile = this.getPipe(player.worldObj);
		if(tile == null) return;
		if(tile.pipe instanceof CoreRoutedPipe) {
			IRouter router = ((CoreRoutedPipe)tile.pipe).getRouter();

			//this is here to allow players to manually trigger a network-wide LSA update
			router.forceLsaUpdate();

			List<List<ExitRoute>> exits = router.getRouteTable();
			HashMap<ForgeDirection, ArrayList<IRouter>> routers = new HashMap<ForgeDirection, ArrayList<IRouter>>();
			for(List<ExitRoute> exit:exits) {
				if(exit == null) continue;
				for(ExitRoute e:exit) {
					if(!routers.containsKey(e.exitOrientation)) {
						routers.put(e.exitOrientation, new ArrayList<IRouter>());
					}
					if(!routers.get(e.exitOrientation).contains(e.destination)) {
						routers.get(e.exitOrientation).add(e.destination);
					}
				}
			}
			ArrayList<LaserData> lasers = new ArrayList<LaserData>();
			firstPipe = true;
			for(ForgeDirection dir: routers.keySet()) {
				handleRouteInDirection(tile, dir, routers.get(dir), lasers, EnumSet.allOf(PipeRoutingConnectionType.class));
			}
			lasers = compressLasers(lasers);
			MainProxy.sendPacketToPlayer(PacketHandler.getPacket(RoutingLaserPacket.class).setLasers(lasers), player);
		}
		*/
	}
/*
	private void handleRouteInDirection(final TileEntity pipe, ForgeDirection dir, ArrayList<IRouter> connectedRouters, final List<LaserData> lasers, EnumSet<PipeRoutingConnectionType> connectionType) {
		System.out.println("Size: " + connectedRouters.size());
		lasers.add(new LaserData(pipe.xCoord, pipe.yCoord, pipe.zCoord, dir, connectionType).setStartPipe(firstPipe));
		firstPipe = false;
		HashMap<CoreRoutedPipe, ExitRoute> map = PathFinder.paintAndgetConnectedRoutingPipes(pipe, dir, Configs.LOGISTICS_DETECTION_COUNT, Configs.LOGISTICS_DETECTION_LENGTH, new IPaintPath() {
			@Override
			public void addLaser(World worldObj, LaserData laser) {
				if(pipe.getWorld() == worldObj) {
					lasers.add(laser);
				}
			}
		}, connectionType);
		for(CoreRoutedPipe connectedPipe: map.keySet()) {
			IRouter newRouter = connectedPipe.getRouter();
			if(!connectedRouters.contains(newRouter)) continue;
			connectedRouters.remove(newRouter);
			HashMap<ForgeDirection, ArrayList<IRouter>> routers = new HashMap<ForgeDirection, ArrayList<IRouter>>();
			Iterator<IRouter> iRouter = connectedRouters.iterator();
			while(iRouter.hasNext()) {
				IRouter router = iRouter.next();
				List<ExitRoute> exit = newRouter.getDistanceTo(router);
				if(exit == null) continue;
				iRouter.remove();
				for(ExitRoute e:exit) {
					if(e.exitOrientation.equals(map.get(connectedPipe).insertOrientation)) continue;
					if(!routers.containsKey(e.exitOrientation)) {
						routers.put(e.exitOrientation, new ArrayList<IRouter>());
					}
					if(!routers.get(e.exitOrientation).contains(e.destination)) {
						routers.get(e.exitOrientation).add(e.destination);
					}
				}
			}
			for(ForgeDirection exitDir: routers.keySet()) {
				//handleRouteInDirection(connectedPipe.container, exitDir, routers.get(exitDir), lasers, map.get(connectedPipe).connectionDetails);
			}
		}
	}

	private ArrayList<LaserData> compressLasers(ArrayList<LaserData> lasers) {
		ArrayList<LaserData> options = new ArrayList<LaserData>();
		options.addAll(lasers);
		Iterator<LaserData> iLasers = lasers.iterator();
		while(iLasers.hasNext()) {
			boolean compressed = false;
			LaserData data = iLasers.next();
			LPPosition next = new LPPosition(data.getPosX(), data.getPosY(), data.getPosZ());
			next.moveForward(data.getDir(), data.getLength());
			boolean found = false;
			do {
				found = false;
				Iterator<LaserData> iOptions = options.iterator();
				while(iOptions.hasNext()) {
					LaserData d = iOptions.next();
					if(d.getPosX() == next.getX() && d.getPosY() == next.getY() && d.getPosZ() == next.getZ()) {
						if(data.getDir().equals(d.getDir()) && data.getConnectionType().equals(d.getConnectionType())) {
							data.setLength(data.getLength() + d.getLength());
							next.moveForward(data.getDir(), d.getLength());
							found = true;
							iOptions.remove();
							lasers.remove(d);
							compressed = true;
						} else if(data.getDir().equals(d.getDir())) {
							data.setFinalPipe(false);
						}
					}
				}
			} while (found);
			if(compressed) {
				iLasers = lasers.iterator();
			}
		}
		return lasers;
	}
*/
	@Override
	public ModernPacket template() {
		return new RequestRoutingLasersPacket(getId());
	}
}
