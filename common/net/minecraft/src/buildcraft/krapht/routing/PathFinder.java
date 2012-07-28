/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.krapht.routing;

import java.util.HashMap;
import java.util.LinkedList;

import net.minecraft.src.TileEntity;
import net.minecraft.src.core_LogisticsPipes;
import buildcraft.api.core.Orientations;
import buildcraft.api.core.Position;
import buildcraft.core.Utils;
import net.minecraft.src.buildcraft.krapht.RoutedPipe;
import net.minecraft.src.buildcraft.krapht.SimpleServiceLocator;
import net.minecraft.src.buildcraft.krapht.routing.ExitRoute;
import net.minecraft.src.buildcraft.krapht.routing.IPaintPath;
import buildcraft.transport.Pipe;
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
	
	public static HashMap<RoutedPipe, ExitRoute> paintAndgetConnectedRoutingPipes(TileGenericPipe startPipe, Orientations startOrientation, int maxVisited, int maxLength, IPaintPath pathPainter) {
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
		
		//Break recursion after certain amount of nodes visited or if we end up where we have been before		
		if (visited.size() > maxLength || visited.contains(startPipe))	{
			return foundPipes;
		}
	
		//Break recursion if we end up on a routing pipe, unless its the first one. Will break if matches the first call 
		if (startPipe.pipe instanceof RoutedPipe && visited.size() != 0) {
			foundPipes.put((RoutedPipe) startPipe.pipe, new ExitRoute(Orientations.Unknown, visited.size()));
			
			return foundPipes;
		}
		
		//Visited is checked after, so we can reach the same target twice to allow to keep the shortest path 
		visited.add(startPipe);
		
		//Iron and obsidean pipes will separate networks
		if (startPipe instanceof TileGenericPipe && (startPipe.pipe instanceof PipeItemsIron) || (startPipe.pipe instanceof PipeItemsObsidian)){
			return foundPipes;
		}
		
		if(startPipe.pipe != null && core_LogisticsPipes.PipeItemTeleport != null) {
			//Special check for teleport pipes
			if (core_LogisticsPipes.teleportPipeDetected && core_LogisticsPipes.PipeItemTeleport.isAssignableFrom(startPipe.pipe.getClass())){
				
				try {
					LinkedList<? extends Pipe> pipez = (LinkedList<? extends Pipe>) core_LogisticsPipes.teleportPipeMethod.invoke(startPipe.pipe, false);
					for (Pipe telepipe : pipez){
						HashMap<RoutedPipe, ExitRoute> result = getConnectedRoutingPipes(((TileGenericPipe)telepipe.container), (LinkedList<TileGenericPipe>)visited.clone(), pathPainter);
						for(RoutedPipe pipe : result.keySet()) 	{
							result.get(pipe).exitOrientation = Orientations.Unknown;
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
		}
		
		//Recurse in all directions
		for (int i = 0; i < 6; i++)	{
			Position p = new Position(startPipe.xCoord, startPipe.yCoord, startPipe.zCoord, Orientations.values()[i]);
			p.moveForwards(1);
			TileEntity tile = startPipe.worldObj.getBlockTileEntity((int) p.x, (int) p.y, (int) p.z);
			if (tile == null || !(tile instanceof TileGenericPipe) || !SimpleServiceLocator.buildCraftProxy.checkPipesConnections(startPipe, tile))
				continue;
	
			int beforeRecurseCount = foundPipes.size();
			HashMap<RoutedPipe, ExitRoute> result = getConnectedRoutingPipes(((TileGenericPipe)tile), (LinkedList<TileGenericPipe>)visited.clone(), pathPainter);
			for(RoutedPipe pipe : result.keySet()) 	{
				//Update Result with the direction we took
				result.get(pipe).exitOrientation = Orientations.values()[i];
				if (!foundPipes.containsKey(pipe)) {  
					// New path
					foundPipes.put(pipe, result.get(pipe));
				}
				else if (result.get(pipe).metric < foundPipes.get(pipe).metric)	{ 
					//If new path is better, replace old path, otherwise do nothing
					foundPipes.put(pipe, result.get(pipe));
				}
			}
			if (foundPipes.size() > beforeRecurseCount && pathPainter != null){
				p.moveBackwards(1);
				pathPainter.addLaser(startPipe.worldObj, p, Orientations.values()[i]);
			}
		}
		return foundPipes;
	}
}
