/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.buildcraft.krapht.routing;

import java.util.HashMap;
import java.util.LinkedList;

import logisticspipes.buildcraft.krapht.RoutedPipe;


import net.minecraft.src.World;
import buildcraft.api.core.LaserKind;
import buildcraft.api.core.Orientations;
import buildcraft.api.core.Position;
import buildcraft.core.EntityBlock;
import buildcraft.core.ProxyCore;
import buildcraft.core.Utils;

class RouteLaser implements IPaintPath{
	private LinkedList<EntityBlock> _lasers = new LinkedList<EntityBlock>();
	private IRouter _lastRouter;
	private LaserKind _pewpewLazors = LaserKind.Stripes;
	
	
	public void clear(){
		for(EntityBlock b : _lasers)
		ProxyCore.proxy.removeEntity(b);
		_lasers = new LinkedList<EntityBlock>();
	}
	
	private void addLeg(World worldObj, Position start, Orientations o){
		Position end = new Position(start.x, start.y, start.z, o);			
		end.moveForwards(1);
		switch(o){
			case XNeg: case YNeg: case ZNeg:
				_lasers.add(Utils.createLaser(worldObj, end, start, _pewpewLazors));
				break;
			default:
				_lasers.add(Utils.createLaser(worldObj, start, end, _pewpewLazors));
		}
	}
	
	public void displayRoute(IRouter source, IRouter destination) {
		_pewpewLazors = LaserKind.Red;
		LinkedList<IRouter> routerList = new LinkedList<IRouter>();
		routerList.add(destination);
		displayRoute(source, routerList);
		_pewpewLazors = LaserKind.Stripes;
	}

	
	public void displayRoute(IRouter r){
		LinkedList<IRouter> knownRouters = new LinkedList<IRouter>();
		for (Router table : r.getRouteTable().keySet()){
			if (table == r) continue;
			knownRouters.add(table);
		}
		displayRoute(r, knownRouters);
	}
	
	public void displayRoute(IRouter r, LinkedList<IRouter> knownRouters){
		clear();
		if (r == _lastRouter){
			_lastRouter = null;
			return;
		}
		_lastRouter = r;
		
		
		while (!knownRouters.isEmpty()){
			//Pick a router
			IRouter targetRouter = knownRouters.pop();
			boolean found = false;
			
			//Get the first exit
			Orientations next = r.getRouteTable().get(targetRouter);
			if (next == Orientations.Unknown){
				System.out.println("BAAAD MOJO");
			}
			
			IRouter nextRouter = r;
			LinkedList<IRouter> visited = new LinkedList<IRouter>();
			while(nextRouter != targetRouter){
				if (visited.contains(nextRouter)){
					System.out.println("ROUTE LOOP");
					break;
				}
				visited.add(nextRouter);
				
				//Paint that route
				LinkedList<IRouter> discovered = new LinkedList<IRouter>();
				Position firstPos = new Position(nextRouter.getPipe().container.xCoord, nextRouter.getPipe().container.yCoord, nextRouter.getPipe().zCoord, next);
				addLeg(r.getPipe().worldObj, firstPos, next);
				HashMap<RoutedPipe, ExitRoute> result = PathFinder.paintAndgetConnectedRoutingPipes(nextRouter.getPipe().container, next, 50, 100, this);
				
				for(RoutedPipe pipe : result.keySet()){
					discovered.add(pipe.getRouter());
				}
				//OLD PAINT PATH LOGIC
				//paintPath(r.getPipe().worldObj, firstPos, new LinkedList<TileEntity>(), discovered);
				
				if (discovered.isEmpty()){
					System.out.println("BAD ROUTE");
				}
				boolean ok = false;
				for (IRouter dicoveredRouter : discovered){
					if (knownRouters.contains(dicoveredRouter)){
						knownRouters.remove(dicoveredRouter);
					}
					if (dicoveredRouter.getRouteTable().containsKey(targetRouter))
					{
						ok = true;
						nextRouter = dicoveredRouter;
						next = dicoveredRouter.getRouteTable().get(targetRouter);
					}
				}
				if (!ok){
					System.out.println("DEAD ROUTE");
					break;
				}
			}
		}

		//OLD PAINT PATH LOGIC
//		Position p1 = new Position(r.getPipe().container.xCoord, r.getPipe().container.yCoord, r.getPipe().zCoord);
//		for (int i = 0; i < 6; i++){
//			Orientations o = Orientations.values()[i];
//			if (!r.isRoutedExit(o)) continue;
//			Position firstPos = new Position(p1.x, p1.y, p1.z, o);
//			addLeg(r.getPipe().worldObj, firstPos, o);
//			paintPath(r.getPipe().worldObj, firstPos, new LinkedList<Position>());
//		}
	}
	
	
	
//	private boolean paintPath(World worldObj, Position start, LinkedList<TileEntity> visited, LinkedList<Router> discovered){
//		
//		start.moveForwards(1);
//				
//		TileEntity tile = worldObj.getBlockTileEntity((int) start.x, (int) start.y, (int) start.z);
//		if (visited.contains(tile)){
//			return false;
//		}
//		visited.add(tile);
//
//		boolean found = false;
//		if (tile instanceof TileGenericPipe) {
//			if (((TileGenericPipe)tile).pipe instanceof RoutedPipe && visited.size() != 0) {
//				discovered.add(((RoutedPipe)((TileGenericPipe)tile).pipe).router);
//				return true;
//			}
//			for (int i = 0; i < 6; i++)	{
//				Orientations nextOrientation = Orientations.values()[i];
//				if (nextOrientation.reverse() == start.orientation) continue;
//				Position nextPos = new Position(start.x, start.y, start.z, nextOrientation);
//				
//				boolean result = paintPath(worldObj, nextPos, (LinkedList<TileEntity>)visited.clone(), discovered);
//				found = found || result;
//				if (result){
//					addLeg(worldObj, start, nextOrientation);
//				}
//			}
//		}
//		return found;
//	}

	@Override
	public void addLaser(World worldObj, Position start, Orientations o) {
		addLeg(worldObj, start, o);
		
	}

}
