/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.routing;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import logisticspipes.interfaces.routing.IDirectRoutingConnection;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.RoutedPipe;
import logisticspipes.proxy.SimpleServiceLocator;
import net.minecraft.src.IInventory;
import net.minecraft.src.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.api.core.Position;
import buildcraft.transport.TileGenericPipe;
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
	 * @param startPipe	- The TileGenericPipe to start the search from 
	 * @param maxVisited - The maximum number of pipes to visit, regardless of recursion level
	 * @param maxLength - The maximum recurse depth, i.e. the maximum length pipe that is supported
	 * @return
	 */
	
	public static HashMap<RoutedPipe, ExitRoute> getConnectedRoutingPipes(TileGenericPipe startPipe, int maxVisited, int maxLength) {
		PathFinder newSearch = new PathFinder(maxVisited, maxLength);
		return newSearch.getConnectedRoutingPipes(startPipe,  new LinkedList<TileGenericPipe>(), null);
	}
	
	public static HashMap<RoutedPipe, ExitRoute> paintAndgetConnectedRoutingPipes(TileGenericPipe startPipe, ForgeDirection startOrientation, int maxVisited, int maxLength, IPaintPath pathPainter) {
		PathFinder newSearch = new PathFinder(maxVisited, maxLength);
		LinkedList<TileGenericPipe> visited = new LinkedList<TileGenericPipe>();
		visited.add(startPipe);
		Position p = new Position(startPipe.xCoord, startPipe.yCoord, startPipe.zCoord, startOrientation);
		p.moveForwards(1);
		TileEntity entity = startPipe.worldObj.getBlockTileEntity((int)p.x, (int)p.y, (int)p.z);
		if (!(entity instanceof TileGenericPipe && ((TileGenericPipe)entity).pipe.isPipeConnected(startPipe))){
			return new HashMap<RoutedPipe, ExitRoute>();
		}
		
		return newSearch.getConnectedRoutingPipes((TileGenericPipe) entity,  visited, pathPainter);
	}
	
	private PathFinder(int maxVisited, int maxLength) {
		this.maxVisited = maxVisited;
		this.maxLength = maxLength;
	}
	
	private int maxVisited;
	private int maxLength;
	
	
	private HashMap<RoutedPipe, ExitRoute> getConnectedRoutingPipes(TileGenericPipe startPipe, LinkedList<TileGenericPipe> visited, IPaintPath pathPainter)	{
		HashMap<RoutedPipe, ExitRoute> foundPipes = new HashMap<RoutedPipe, ExitRoute>();
		
		//Break recursion if we have visited a set number of pipes, to prevent client hang if pipes are weirdly configured			
		if (maxVisited-- < 1) {
			return foundPipes;
		}
		
		//Break recursion after certain amount of nodes visited
		if (visited.size() > maxLength)	{
			return foundPipes;
		}
	
		//Break recursion if we end up on a routing pipe, unless its the first one. Will break if matches the first call 
		if (startPipe.pipe instanceof RoutedPipe && visited.size() != 0) {
			foundPipes.put((RoutedPipe) startPipe.pipe, new ExitRoute(ForgeDirection.UNKNOWN, visited.size(), false));
			
			return foundPipes;
		}
		
		//Visited is checked after, so we can reach the same target twice to allow to keep the shortest path 
		visited.add(startPipe);
		
		//Iron and obsidean pipes will separate networks
		if (startPipe instanceof TileGenericPipe && (startPipe.pipe instanceof PipeItemsIron) || (startPipe.pipe instanceof PipeItemsObsidian)){
			return foundPipes;
		}
		
		if(startPipe.pipe != null) {
			//Special check for unnormal pipes
			try {
				List<TileGenericPipe> pipez = SimpleServiceLocator.specialconnection.getConnectedPipes(startPipe);
				for (TileGenericPipe specialpipe : pipez){
					if (visited.contains(specialpipe)) {
						//Don't go where we have been before
						continue;
					}
					HashMap<RoutedPipe, ExitRoute> result = getConnectedRoutingPipes(specialpipe, (LinkedList<TileGenericPipe>)visited.clone(), pathPainter);
					for(RoutedPipe pipe : result.keySet()) 	{
						result.get(pipe).exitOrientation = ForgeDirection.UNKNOWN;
						if (!foundPipes.containsKey(pipe)) {  
							// New path
							foundPipes.put(pipe, result.get(pipe));
						}
						else if (result.get(pipe).metric < foundPipes.get(pipe).metric)	{ 
							//If new path is better, replace old path, otherwise do nothing
							foundPipes.put(pipe, result.get(pipe));
						}
					}
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		//Recurse in all directions
		for (int i = 0; i < 6; i++)	{
			Position p = new Position(startPipe.xCoord, startPipe.yCoord, startPipe.zCoord, ForgeDirection.values()[i]);
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
				if (visited.contains(tile)) {
					//Don't go where we have been before
					continue;
				}
				int beforeRecurseCount = foundPipes.size();
				HashMap<RoutedPipe, ExitRoute> result = getConnectedRoutingPipes(((TileGenericPipe)tile), (LinkedList<TileGenericPipe>)visited.clone(), pathPainter);
				for(RoutedPipe pipe : result.keySet()) 	{
					//Update Result with the direction we took
					result.get(pipe).exitOrientation = ForgeDirection.values()[i];
					if(isDirectConnection) {
						result.get(pipe).isPipeLess = true;
					}
					if (!foundPipes.containsKey(pipe)) {  
						// New path
						foundPipes.put(pipe, result.get(pipe));
						//Add resistance
						foundPipes.get(pipe).metric += resistance;
					}
					else if (result.get(pipe).metric + resistance < foundPipes.get(pipe).metric)	{ 
						//If new path is better, replace old path, otherwise do nothing
						foundPipes.put(pipe, result.get(pipe));
						//Add resistance
						foundPipes.get(pipe).metric += resistance;
					}
				}
				if (foundPipes.size() > beforeRecurseCount && pathPainter != null){
					p.moveBackwards(1);
					pathPainter.addLaser(startPipe.worldObj, p, ForgeDirection.values()[i]);
				}
			}
		}
		return foundPipes;
	}
}
