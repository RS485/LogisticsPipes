/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.routing.pathfinder;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

import logisticspipes.api.ILogisticsPowerProvider;
import logisticspipes.interfaces.ISubSystemPowerProvider;
import logisticspipes.interfaces.routing.IDirectRoutingConnection;
import logisticspipes.interfaces.routing.IFilter;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.routing.ExitRoute;
import logisticspipes.routing.IPaintPath;
import logisticspipes.routing.LaserData;
import logisticspipes.routing.PipeRoutingConnectionType;
import logisticspipes.utils.OneList;
import logisticspipes.utils.OrientationsUtil;
import logisticspipes.utils.tuples.LPPosition;
import logisticspipes.utils.tuples.Pair;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;


/**
 * Examines all pipe connections and their forks to locate all connected routers
 */
public class PathFinder {
	/**
	 * Recurse through all exists of a pipe to find instances of PipeItemsRouting. maxVisited and maxLength are safeguards for
	 * recursion runaways.
	 * 
	 * @param startPipe - The TileEntity to start the search from
	 * @param maxVisited - The maximum number of pipes to visit, regardless of recursion level
	 * @param maxLength - The maximum recurse depth, i.e. the maximum length pipe that is supported
	 * @return
	 */
	
	public static HashMap<CoreRoutedPipe, ExitRoute> paintAndgetConnectedRoutingPipes(TileEntity startPipe, ForgeDirection startOrientation, int maxVisited, int maxLength, IPaintPath pathPainter, EnumSet<PipeRoutingConnectionType> connectionType) {
		IPipeInformationProvider startProvider = SimpleServiceLocator.pipeInformaitonManager.getInformationProviderFor(startPipe);
		if(startProvider == null) {
			return new HashMap<CoreRoutedPipe, ExitRoute>();
		}
		PathFinder newSearch = new PathFinder(maxVisited, maxLength, pathPainter);
		LPPosition p = new LPPosition(startProvider);
		newSearch.setVisited.add(p);
		p.moveForward(startOrientation);
		TileEntity entity = p.getTileEntity(startProvider.getWorld());
		IPipeInformationProvider provider = SimpleServiceLocator.pipeInformaitonManager.getInformationProviderFor(entity);
		if (provider == null) {
			return new HashMap<CoreRoutedPipe, ExitRoute>();
		}
		return newSearch.getConnectedRoutingPipes(provider, connectionType, startOrientation);
	}
	
	public PathFinder(IPipeInformationProvider startPipe, int maxVisited, int maxLength) {
		this(maxVisited, maxLength, null);
		if(startPipe == null) {
			result = new HashMap<CoreRoutedPipe, ExitRoute>();
			return;
		}
		result = this.getConnectedRoutingPipes(startPipe, EnumSet.allOf(PipeRoutingConnectionType.class), ForgeDirection.UNKNOWN);
	}
	
	public PathFinder(LogisticsTileGenericPipe startPipe, int maxVisited, int maxLength) {
		this(SimpleServiceLocator.pipeInformaitonManager.getInformationProviderFor(startPipe), maxVisited, maxLength);
	}
	
	public PathFinder(IPipeInformationProvider startPipe, int maxVisited, int maxLength, ForgeDirection side) {
		this(maxVisited, maxLength, null);
		result=this.getConnectedRoutingPipes(startPipe, EnumSet.allOf(PipeRoutingConnectionType.class), side);
	}
	
	
	private PathFinder(int maxVisited, int maxLength, IPaintPath pathPainter) {
		this.maxVisited = maxVisited;
		this.maxLength = maxLength;
		this.setVisited = new HashSet<LPPosition>();
		this.distances = new HashMap<LPPosition, Integer>();
		this.pathPainter = pathPainter;
	}

	private final int maxVisited;
	private final int maxLength;
	private final HashSet<LPPosition> setVisited;
	private final HashMap<LPPosition, Integer> distances;
	private final IPaintPath pathPainter;
	private int pipesVisited;

	public List<Pair<ILogisticsPowerProvider,List<IFilter>>> powerNodes;
	public List<Pair<ISubSystemPowerProvider,List<IFilter>>> subPowerProvider;
	public HashMap<CoreRoutedPipe, ExitRoute> result;
	
	private HashMap<CoreRoutedPipe, ExitRoute> getConnectedRoutingPipes(IPipeInformationProvider startPipe, EnumSet<PipeRoutingConnectionType> connectionFlags, ForgeDirection side) {
		HashMap<CoreRoutedPipe, ExitRoute> foundPipes = new HashMap<CoreRoutedPipe, ExitRoute>();
		
		boolean root = setVisited.size() == 0;
		
		//Reset visited count at top level
		if (setVisited.size() == 1) {
			pipesVisited = 0;
		}
		
		//Break recursion if we have visited a set number of pipes, to prevent client hang if pipes are weirdly configured
		if (++pipesVisited > maxVisited) {
			return foundPipes;
		}
		
		//Break recursion after certain amount of nodes visited
		if (setVisited.size() > maxLength) {
			return foundPipes;
		}
		
		if (!startPipe.isInitialised()) {
			return foundPipes;
		}
		
		//Break recursion if we end up on a routing pipe, unless its the first one. Will break if matches the first call
		if (startPipe.isRoutingPipe() && setVisited.size() != 0) {
			CoreRoutedPipe rp = startPipe.getRoutingPipe();
			if(rp.stillNeedReplace()) {
				return foundPipes;
			}
			int size = 0;
			for(Integer dis:distances.values()) {
				size += dis;
			}
			
			if(!rp.getUpgradeManager().hasPowerPassUpgrade()) {
				connectionFlags.remove(PipeRoutingConnectionType.canPowerSubSystemFrom);
			}
			
			foundPipes.put(rp, new ExitRoute(null,rp.getRouter(), ForgeDirection.UNKNOWN, side.getOpposite(), Math.max(1, size), connectionFlags, distances.size()));
			
			return foundPipes;
		}
		
		//Visited is checked after, so we can reach the same target twice to allow to keep the shortest path
		setVisited.add(new LPPosition(startPipe));
		distances.put(new LPPosition(startPipe), startPipe.getDistance());
		
		// first check specialPipeConnections (tesseracts, teleports, other connectors)
		List<IPipeInformationProvider> pipez = SimpleServiceLocator.specialpipeconnection.getConnectedPipes(startPipe);
		for (IPipeInformationProvider specialpipe : pipez){
			if (setVisited.contains(new LPPosition(specialpipe))) {
				//Don't go where we have been before
				continue;
			}
			HashMap<CoreRoutedPipe, ExitRoute> result = getConnectedRoutingPipes(specialpipe, connectionFlags, side);
			for (Entry<CoreRoutedPipe, ExitRoute> pipe : result.entrySet()) {
				pipe.getValue().exitOrientation = ForgeDirection.UNKNOWN;
				ExitRoute foundPipe=foundPipes.get(pipe.getKey());
				if (foundPipe == null || (pipe.getValue().distanceToDestination < foundPipe.distanceToDestination)) {
					// New path OR 	If new path is better, replace old path
					foundPipes.put(pipe.getKey(), pipe.getValue());
				}
			}
		}
		
		ArrayDeque<Pair<TileEntity,ForgeDirection>> connections = new ArrayDeque<Pair<TileEntity,ForgeDirection>>();
		
		//Recurse in all directions
		for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
			if(root && !ForgeDirection.UNKNOWN.equals(side) && !direction.equals(side)) continue;

			// tile may be up to 1 second old, but any neighbour pipe change will cause an immidiate update here, so we know that if it has changed, it isn't a pipe that has done so.
			TileEntity tile = startPipe.getTile(direction);
			
			if (tile == null) continue;
			if(OrientationsUtil.isSide(direction)) {
				if (root && tile instanceof ILogisticsPowerProvider) {
					if(this.powerNodes==null) {
						powerNodes = new ArrayList<Pair<ILogisticsPowerProvider,List<IFilter>>>();
					}
					//If we are a FireWall pipe add our filter to the pipes
					if(startPipe.isFirewallPipe()) {
						powerNodes.add(new Pair<ILogisticsPowerProvider,List<IFilter>>((ILogisticsPowerProvider) tile, new OneList<IFilter>(startPipe.getFirewallFilter())));
					} else {
						powerNodes.add(new Pair<ILogisticsPowerProvider,List<IFilter>>((ILogisticsPowerProvider) tile, Collections.unmodifiableList(new ArrayList<IFilter>(0))));
					}
				}
				if(root && tile instanceof ISubSystemPowerProvider) {
					if(this.subPowerProvider==null) {
						subPowerProvider = new ArrayList<Pair<ISubSystemPowerProvider,List<IFilter>>>();
					}
					//If we are a FireWall pipe add our filter to the pipes
					if(startPipe.isFirewallPipe()) {
						subPowerProvider.add(new Pair<ISubSystemPowerProvider,List<IFilter>>((ISubSystemPowerProvider) tile, new OneList<IFilter>(startPipe.getFirewallFilter())));
					} else {
						subPowerProvider.add(new Pair<ISubSystemPowerProvider,List<IFilter>>((ISubSystemPowerProvider) tile, Collections.unmodifiableList(new ArrayList<IFilter>(0))));
					}
				}
			}
			connections.add(new Pair<TileEntity, ForgeDirection>(tile, direction));
		}
		
		while(!connections.isEmpty()) {
			Pair<TileEntity,ForgeDirection> pair = connections.pollFirst();
			TileEntity tile = pair.getValue1();
			ForgeDirection direction = pair.getValue2();
			EnumSet<PipeRoutingConnectionType> nextConnectionFlags = EnumSet.copyOf(connectionFlags);
			boolean isDirectConnection = false;
			int resistance = 0;
			
			if(root) {
				Collection<TileEntity> list = SimpleServiceLocator.specialtileconnection.getConnectedPipes(tile);
				if(!list.isEmpty()) {
					for(TileEntity pipe:list) {
						connections.add(new Pair<TileEntity, ForgeDirection>(pipe, direction));
					}
					continue;
				}
				if(!startPipe.getRoutingPipe().getUpgradeManager().hasPowerPassUpgrade()) {
					nextConnectionFlags.remove(PipeRoutingConnectionType.canPowerSubSystemFrom);
				}
			}
			
			if(tile instanceof IInventory && startPipe.isRoutingPipe() && startPipe.getRoutingPipe() instanceof IDirectRoutingConnection) {
				if(SimpleServiceLocator.connectionManager.hasDirectConnection(startPipe.getRoutingPipe().getRouter())) {
					CoreRoutedPipe CRP = SimpleServiceLocator.connectionManager.getConnectedPipe(startPipe.getRoutingPipe().getRouter());
					if(CRP != null) {
						tile = CRP.container;
						isDirectConnection = true;
						resistance = ((IDirectRoutingConnection)startPipe.getRoutingPipe()).getConnectionResistance();
					}
				}
			}
			
			if (tile == null) continue;
			
			IPipeInformationProvider currentPipe = SimpleServiceLocator.pipeInformaitonManager.getInformationProviderFor(tile);
			
			if (currentPipe != null && currentPipe.isInitialised() && (isDirectConnection || SimpleServiceLocator.pipeInformaitonManager.canConnect(startPipe, currentPipe, direction, true))) {
				//TileGenericPipe currentPipe = (TileGenericPipe) tile;
				if (setVisited.contains(new LPPosition(tile))) {
					//Don't go where we have been before
					continue;
				}
				if(side != pair.getValue2() && !root) { //Only straight connections for subsystem power
					nextConnectionFlags.remove(PipeRoutingConnectionType.canPowerSubSystemFrom);
				}
				if(isDirectConnection) {  //ISC doesn't pass power
					nextConnectionFlags.remove(PipeRoutingConnectionType.canPowerFrom);
					nextConnectionFlags.remove(PipeRoutingConnectionType.canPowerSubSystemFrom);
				}
				//Iron, obsidean and liquid pipes will separate networks
				if(currentPipe.divideNetwork()) {
					continue;
				}
				if(currentPipe.powerOnly()) {
					nextConnectionFlags.remove(PipeRoutingConnectionType.canRouteTo);
					nextConnectionFlags.remove(PipeRoutingConnectionType.canRequestFrom);
				}
				if(startPipe.isOnewayPipe()) {
					if(!startPipe.isOutputOpen(direction)) {
						nextConnectionFlags.remove(PipeRoutingConnectionType.canRouteTo);
					}
				}
				if(currentPipe.isOnewayPipe()) {
					nextConnectionFlags.remove(PipeRoutingConnectionType.canPowerSubSystemFrom);
					if(!currentPipe.isOutputOpen(direction.getOpposite())) {
						nextConnectionFlags.remove(PipeRoutingConnectionType.canRequestFrom);
						nextConnectionFlags.remove(PipeRoutingConnectionType.canPowerFrom);
					}
				}

				if(nextConnectionFlags.isEmpty()) {	//don't bother going somewhere we can't do anything with
					continue;
				}

				int beforeRecurseCount = foundPipes.size();
				HashMap<CoreRoutedPipe, ExitRoute> result = getConnectedRoutingPipes(currentPipe, nextConnectionFlags, direction);
				for(Entry<CoreRoutedPipe, ExitRoute> pipeEntry : result.entrySet()) {
					//Update Result with the direction we took
					pipeEntry.getValue().exitOrientation = direction;
					ExitRoute foundPipe = foundPipes.get(pipeEntry.getKey());
					if (foundPipe==null) {
						// New path
						foundPipes.put(pipeEntry.getKey(), pipeEntry.getValue());
						//Add resistance
						pipeEntry.getValue().distanceToDestination += resistance;
					}
					else if (pipeEntry.getValue().distanceToDestination + resistance < foundPipe.distanceToDestination) {
						//If new path is better, replace old path, otherwise do nothing
						foundPipes.put(pipeEntry.getKey(), pipeEntry.getValue());
						//Add resistance
						pipeEntry.getValue().distanceToDestination += resistance;
					}
				}
				if (foundPipes.size() > beforeRecurseCount && pathPainter != null) {
					pathPainter.addLaser(startPipe.getWorld(), new LaserData(startPipe.getX(), startPipe.getY(), startPipe.getZ(), direction, connectionFlags));
				}
			}
		}
		setVisited.remove(new LPPosition(startPipe));
		distances.remove(new LPPosition(startPipe));
		if(startPipe.isRoutingPipe()) { // ie, has the recursion returned to the pipe it started from?
			for(ExitRoute e:foundPipes.values()) {
				e.root = (startPipe.getRoutingPipe()).getRouter();
			}
		}
		//If we are a FireWall pipe add our filter to the pipes
		if(startPipe.isFirewallPipe() && root) {
			for(ExitRoute e:foundPipes.values()) {
				e.filters = new OneList<IFilter>(startPipe.getFirewallFilter());
			}
		}
		return foundPipes;
	}

	public static int messureDistanceToNextRoutedPipe(LPPosition lpPosition, ForgeDirection exitOrientation, World world) {
		int dis = 1;
		TileEntity tile = lpPosition.getTileEntity(world);
		if(tile instanceof LogisticsTileGenericPipe) {
			tile = ((LogisticsTileGenericPipe)tile).getTile(exitOrientation);
		}
		if(tile == null) return 0;
		IPipeInformationProvider info = SimpleServiceLocator.pipeInformaitonManager.getInformationProviderFor(tile);
		while(info != null && !info.isRoutingPipe()) {
			tile = info.getTile(exitOrientation);
			if(tile == null) {
				info = null;
				continue;
			}
			info = SimpleServiceLocator.pipeInformaitonManager.getInformationProviderFor(tile);
			dis++;
		}
		return dis;
	}
}
