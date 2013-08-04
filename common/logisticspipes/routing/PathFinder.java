/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.routing;

import java.util.ArrayDeque;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

import logisticspipes.interfaces.routing.IDirectRoutingConnection;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.fluid.LogisticsFluidConnectorPipe;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.utils.Pair;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.api.core.Position;
import buildcraft.transport.TileGenericPipe;
import buildcraft.transport.pipes.PipeItemsDiamond;
import buildcraft.transport.pipes.PipeItemsIron;
import buildcraft.transport.pipes.PipeItemsObsidian;
import buildcraft.transport.pipes.PipeStructureCobblestone;


/**
 * Examines all pipe connections and their forks to locate all connected routers
 */
public class PathFinder {
	/**
	 * Recurse through all exists of a pipe to find instances of PipeItemsRouting. maxVisited and maxLength are safeguards for
	 * recursion runaways.
	 * 
	 * @param startPipe - The TileGenericPipe to start the search from
	 * @param maxVisited - The maximum number of pipes to visit, regardless of recursion level
	 * @param maxLength - The maximum recurse depth, i.e. the maximum length pipe that is supported
	 * @return
	 */
	
	public static HashMap<CoreRoutedPipe, ExitRoute> getConnectedRoutingPipes(TileGenericPipe startPipe, int maxVisited, int maxLength) {
		PathFinder newSearch = new PathFinder(maxVisited, maxLength, null);
		return newSearch.getConnectedRoutingPipes(startPipe, EnumSet.allOf(PipeRoutingConnectionType.class), ForgeDirection.UNKNOWN);
	}
	
	public static HashMap<CoreRoutedPipe, ExitRoute> getConnectedRoutingPipes(TileGenericPipe startPipe, int maxVisited, int maxLength, ForgeDirection side) {
		PathFinder newSearch = new PathFinder(maxVisited, maxLength, null);
		return newSearch.getConnectedRoutingPipes(startPipe, EnumSet.allOf(PipeRoutingConnectionType.class), side);
	}
	
	public static HashMap<CoreRoutedPipe, ExitRoute> paintAndgetConnectedRoutingPipes(TileGenericPipe startPipe, ForgeDirection startOrientation, int maxVisited, int maxLength, IPaintPath pathPainter, EnumSet<PipeRoutingConnectionType> connectionType) {
		PathFinder newSearch = new PathFinder(maxVisited, maxLength, pathPainter);
		newSearch.setVisited.add(startPipe);
		Position p = new Position(startPipe.xCoord, startPipe.yCoord, startPipe.zCoord, startOrientation);
		p.moveForwards(1);
		TileEntity entity = startPipe.getWorld().getBlockTileEntity((int)p.x, (int)p.y, (int)p.z);
		if (!(entity instanceof TileGenericPipe && ((TileGenericPipe)entity).pipe.canPipeConnect(startPipe, startOrientation))){
			return new HashMap<CoreRoutedPipe, ExitRoute>();
		}
		
		return newSearch.getConnectedRoutingPipes((TileGenericPipe) entity, connectionType, startOrientation);
	}
	
	private PathFinder(int maxVisited, int maxLength, IPaintPath pathPainter) {
		this.maxVisited = maxVisited;
		this.maxLength = maxLength;
		this.setVisited = new HashSet<TileGenericPipe>();
		this.pathPainter = pathPainter;
	}
	
	private final int maxVisited;
	private final int maxLength;
	private final HashSet<TileGenericPipe> setVisited;
	private final IPaintPath pathPainter;
	private int pipesVisited;
	
	private HashMap<CoreRoutedPipe, ExitRoute> getConnectedRoutingPipes(TileGenericPipe startPipe, EnumSet<PipeRoutingConnectionType> connectionFlags, ForgeDirection side) {
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
		
		if (!startPipe.initialized) {
			return foundPipes;
		}
		
		//Break recursion if we end up on a routing pipe, unless its the first one. Will break if matches the first call
		if (startPipe.pipe instanceof CoreRoutedPipe && setVisited.size() != 0) {
			CoreRoutedPipe rp = (CoreRoutedPipe) startPipe.pipe;
			if(rp.stillNeedReplace()) {
				return foundPipes;
			}
			foundPipes.put(rp, new ExitRoute(null,rp.getRouter(), ForgeDirection.UNKNOWN, side.getOpposite(),  setVisited.size(), connectionFlags));
			
			return foundPipes;
		}
		
		//Iron, obsidean and liquid pipes will separate networks
		if (startPipe.pipe instanceof LogisticsFluidConnectorPipe) {
			return foundPipes;
		}		
		
		//Visited is checked after, so we can reach the same target twice to allow to keep the shortest path
		setVisited.add(startPipe);
		
		if(startPipe.pipe != null) {
			List<TileGenericPipe> pipez = SimpleServiceLocator.specialpipeconnection.getConnectedPipes(startPipe);
			for (TileGenericPipe specialpipe : pipez){
				if (setVisited.contains(specialpipe)) {
					//Don't go where we have been before
					continue;
				}
				HashMap<CoreRoutedPipe, ExitRoute> result = getConnectedRoutingPipes(specialpipe,connectionFlags, side);
				for (Entry<CoreRoutedPipe, ExitRoute> pipe : result.entrySet()) {
					pipe.getValue().exitOrientation = ForgeDirection.UNKNOWN;
					ExitRoute foundPipe=foundPipes.get(pipe.getKey());
					if (foundPipe==null || (pipe.getValue().distanceToDestination < foundPipe.distanceToDestination)) {
						// New path OR 	If new path is better, replace old path
						foundPipes.put(pipe.getKey(), pipe.getValue());
					}
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
				List<TileGenericPipe> list = SimpleServiceLocator.specialtileconnection.getConnectedPipes(tile);
				if(!list.isEmpty()) {
					for(TileGenericPipe pipe:list) {
						connections.add(new Pair<TileEntity,ForgeDirection>(pipe, direction));
					}
					continue;
				}
			}
			
			if(tile instanceof IInventory) {
				if(startPipe.pipe instanceof IDirectRoutingConnection) {
					if(SimpleServiceLocator.connectionManager.hasDirectConnection(((CoreRoutedPipe)startPipe.pipe).getRouter())) {
						CoreRoutedPipe CRP = SimpleServiceLocator.connectionManager.getConnectedPipe(((CoreRoutedPipe)startPipe.pipe).getRouter());
						if(CRP != null) {
							tile = CRP.container;
							isDirectConnection = true;
							resistance = ((IDirectRoutingConnection)startPipe.pipe).getConnectionResistance();
						}
					}
				}
			}
			
			if (tile == null) continue;
			
			if (tile instanceof TileGenericPipe && (isDirectConnection || SimpleServiceLocator.buildCraftProxy.checkPipesConnections(startPipe, tile, direction, true))) {
				TileGenericPipe currentPipe = (TileGenericPipe) tile;
				if (setVisited.contains(tile)) {
					//Don't go where we have been before
					continue;
				}
				if(isDirectConnection) {  //ISC doesn't pass power
					nextConnectionFlags.remove(PipeRoutingConnectionType.canPowerFrom);
				}
				if(currentPipe.pipe instanceof PipeItemsObsidian){	//Obsidian seperates networks
					continue;
				}
				if(currentPipe.pipe instanceof PipeStructureCobblestone){	//don't recurse onto structure pipes.
					continue;
				}
				if(currentPipe.pipe instanceof PipeItemsDiamond){	//Diamond only allows power through
					nextConnectionFlags.remove(PipeRoutingConnectionType.canRouteTo);
					nextConnectionFlags.remove(PipeRoutingConnectionType.canRequestFrom);
				}
				if(startPipe.pipe instanceof PipeItemsIron){	//Iron requests and power can come from closed sides
					if(!startPipe.pipe.outputOpen(direction)){
						nextConnectionFlags.remove(PipeRoutingConnectionType.canRouteTo);
					}
				}
				if(currentPipe.pipe instanceof PipeItemsIron){	//and can only go to the open side
					if(!currentPipe.pipe.outputOpen(direction.getOpposite())){
						nextConnectionFlags.remove(PipeRoutingConnectionType.canRequestFrom);
						nextConnectionFlags.remove(PipeRoutingConnectionType.canPowerFrom);
					}
				}

				if(nextConnectionFlags.isEmpty()) {	//don't bother going somewhere we can't do anything with
					continue;
				}

				int beforeRecurseCount = foundPipes.size();
				HashMap<CoreRoutedPipe, ExitRoute> result = getConnectedRoutingPipes(((TileGenericPipe)tile), nextConnectionFlags, direction);
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
					pathPainter.addLaser(startPipe.getWorld(), new LaserData(startPipe.xCoord, startPipe.yCoord, startPipe.zCoord, direction, connectionFlags));
				}
			}
		}
		setVisited.remove(startPipe);
		if(startPipe.pipe instanceof CoreRoutedPipe){ // ie, has the recursion returned to the pipe it started from?
			for(ExitRoute e:foundPipes.values())
				e.root=((CoreRoutedPipe)startPipe.pipe).getRouter();
		}
				
		return foundPipes;
	}
}
