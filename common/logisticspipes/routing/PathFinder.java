/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.routing;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import logisticspipes.interfaces.routing.IDirectRoutingConnection;
import logisticspipes.pipes.PipeItemsInvSysConnector;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.RoutedPipe;
import logisticspipes.pipes.basic.liquid.LogisticsLiquidConnectorPipe;
import logisticspipes.proxy.SimpleServiceLocator;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.api.core.Position;
import buildcraft.transport.TileGenericPipe;
import buildcraft.transport.pipes.PipeItemsDiamond;
import buildcraft.transport.pipes.PipeItemsIron;
import buildcraft.transport.pipes.PipeItemsObsidian;


/**
 * Examines all pipe connections and their forks to locate all connected routers
 */
class PathFinder {
	/**
	 * Recurse through all exists of a pipe to find instances of PipeItemsRouting. maxVisited and maxLength are safeguards for
	 * recursion runaways.
	 * 
	 * @param startPipe - The TileGenericPipe to start the search from
	 * @param maxVisited - The maximum number of pipes to visit, regardless of recursion level
	 * @param maxLength - The maximum recurse depth, i.e. the maximum length pipe that is supported
	 * @return
	 */
	
	public static HashMap<RoutedPipe, ExitRoute> getConnectedRoutingPipes(TileGenericPipe startPipe, int maxVisited, int maxLength) {
		PathFinder newSearch = new PathFinder(maxVisited, maxLength, null);
		return newSearch.getConnectedRoutingPipes(startPipe,EnumSet.noneOf(PipeRoutingConnectionType.class));
	}
	
	public static HashMap<RoutedPipe, ExitRoute> paintAndgetConnectedRoutingPipes(TileGenericPipe startPipe, ForgeDirection startOrientation, int maxVisited, int maxLength, IPaintPath pathPainter) {
		PathFinder newSearch = new PathFinder(maxVisited, maxLength, pathPainter);
		newSearch.setVisited.add(startPipe);
		Position p = new Position(startPipe.xCoord, startPipe.yCoord, startPipe.zCoord, startOrientation);
		p.moveForwards(1);
		TileEntity entity = startPipe.worldObj.getBlockTileEntity((int)p.x, (int)p.y, (int)p.z);
		if (!(entity instanceof TileGenericPipe && ((TileGenericPipe)entity).pipe.isPipeConnected(startPipe, startOrientation))){
			return new HashMap<RoutedPipe, ExitRoute>();
		}
		
		return newSearch.getConnectedRoutingPipes((TileGenericPipe) entity,EnumSet.noneOf(PipeRoutingConnectionType.class));
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
	
	private HashMap<RoutedPipe, ExitRoute> getConnectedRoutingPipes(TileGenericPipe startPipe, EnumSet<PipeRoutingConnectionType> connectionFlags) {
		HashMap<RoutedPipe, ExitRoute> foundPipes = new HashMap<RoutedPipe, ExitRoute>();
		
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
		
		//Break recursion if we end up on a routing pipe, unless its the first one. Will break if matches the first call
		if (startPipe.pipe instanceof RoutedPipe && setVisited.size() != 0) {
			foundPipes.put((RoutedPipe) startPipe.pipe, new ExitRoute(ForgeDirection.UNKNOWN, setVisited.size(), connectionFlags));
			
			return foundPipes;
		}
		
		//Iron, obsidean and liquid pipes will separate networks
		if (startPipe.pipe instanceof LogisticsLiquidConnectorPipe) {
			return foundPipes;
		}		
		
		//Visited is checked after, so we can reach the same target twice to allow to keep the shortest path
		setVisited.add(startPipe);
		
		if(startPipe.pipe != null) {
			List<TileGenericPipe> pipez = SimpleServiceLocator.specialconnection.getConnectedPipes(startPipe);
			for (TileGenericPipe specialpipe : pipez){
				if (setVisited.contains(specialpipe)) {
					//Don't go where we have been before
					continue;
				}
				HashMap<RoutedPipe, ExitRoute> result = getConnectedRoutingPipes(specialpipe,connectionFlags);
				for(RoutedPipe pipe : result.keySet()) {
					result.get(pipe).exitOrientation = ForgeDirection.UNKNOWN;
					if (!foundPipes.containsKey(pipe)) {
						// New path
						foundPipes.put(pipe, result.get(pipe));
					}
					else if (result.get(pipe).metric < foundPipes.get(pipe).metric) {
						//If new path is better, replace old path, otherwise do nothing
						foundPipes.put(pipe, result.get(pipe));
					}
				}
			}
		}
		
		
		//Recurse in all directions
		for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
			EnumSet<PipeRoutingConnectionType> nextConnectionFlags = EnumSet.copyOf(connectionFlags);
			Position p = new Position(startPipe.xCoord, startPipe.yCoord, startPipe.zCoord, direction);
			p.moveForwards(1);
			TileEntity tile = startPipe.worldObj.getBlockTileEntity((int) p.x, (int) p.y, (int) p.z);
			
			if (tile == null) continue;
			boolean isDirectConnection = false;
			int resistance = 0;
			

			
			if(tile instanceof IInventory) {
				if(startPipe.pipe instanceof IDirectRoutingConnection) {
					if(SimpleServiceLocator.connectionManager.hasDirectConnection(((RoutedPipe)startPipe.pipe).getRouter())) {
						CoreRoutedPipe CRP = SimpleServiceLocator.connectionManager.getConnectedPipe(((RoutedPipe)startPipe.pipe).getRouter());
						if(CRP != null) {
							tile = CRP.container;
							isDirectConnection = true;
							resistance = ((IDirectRoutingConnection)startPipe.pipe).getConnectionResistance();
						}
					}
				}
			}
			
			if (tile == null) continue;
			
			if (tile instanceof TileGenericPipe && (isDirectConnection || SimpleServiceLocator.buildCraftProxy.checkPipesConnections(startPipe, tile))) {
				TileGenericPipe currentPipe = (TileGenericPipe) tile;
				if (setVisited.contains(tile)) {
					//Don't go where we have been before
					continue;
				}

				//Iron, obsidean, and diamond pipes will separate networks
				if(currentPipe.pipe instanceof PipeItemsObsidian){
					nextConnectionFlags.add(PipeRoutingConnectionType.passedThroughObsidian);
				}
				if(currentPipe.pipe instanceof PipeItemsDiamond){
					nextConnectionFlags.add(PipeRoutingConnectionType.passedThroughDiamond);
				}
				if(startPipe.pipe instanceof PipeItemsInvSysConnector){
					nextConnectionFlags.add(PipeRoutingConnectionType.blocksPowerFlow);
				}
				if(startPipe.pipe instanceof PipeItemsIron){
					if(startPipe.pipe.outputOpen(direction))
						nextConnectionFlags.add(PipeRoutingConnectionType.passedThroughIronOpen);
					else
						nextConnectionFlags.add(PipeRoutingConnectionType.passedThroughIronClosed);
				}
				if(currentPipe.pipe instanceof PipeItemsIron){
					if(currentPipe.pipe.outputOpen(direction.getOpposite()))
						nextConnectionFlags.add(PipeRoutingConnectionType.reversedPassedThroughIronOpen);
					else
						nextConnectionFlags.add(PipeRoutingConnectionType.reversedPassedThroughIronClosed);
				}
				int beforeRecurseCount = foundPipes.size();
				HashMap<RoutedPipe, ExitRoute> result = getConnectedRoutingPipes(((TileGenericPipe)tile),nextConnectionFlags);
				for(RoutedPipe pipe : result.keySet()) {
					//Update Result with the direction we took
					result.get(pipe).exitOrientation = direction;
					if (!foundPipes.containsKey(pipe)) {
						// New path
						foundPipes.put(pipe, result.get(pipe));
						//Add resistance
						foundPipes.get(pipe).metric += resistance;
					}
					else if (result.get(pipe).metric + resistance < foundPipes.get(pipe).metric) {
						//If new path is better, replace old path, otherwise do nothing
						foundPipes.put(pipe, result.get(pipe));
						//Add resistance
						foundPipes.get(pipe).metric += resistance;
					}
				}
				if (foundPipes.size() > beforeRecurseCount && pathPainter != null){
					p.moveBackwards(1);
					pathPainter.addLaser(startPipe.worldObj, p, direction);
				}
			}
		}
		setVisited.remove(startPipe);
		return foundPipes;
	}
}
