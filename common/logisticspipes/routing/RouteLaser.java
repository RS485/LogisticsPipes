/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.routing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import logisticspipes.LogisticsPipes;
import logisticspipes.pipes.basic.RoutedPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.Pair;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.api.core.LaserKind;
import buildcraft.api.core.Position;
import buildcraft.core.EntityBlock;
import buildcraft.core.utils.Utils;

class RouteLaser implements IPaintPath{
	private LinkedList<EntityBlock> _lasers = new LinkedList<EntityBlock>();
	private IRouter _lastRouter;
	private LaserKind _pewpewLazors = LaserKind.Stripes;
	
	
	public void clear(){
		for(EntityBlock b : _lasers) {
			b.setDead();
			if(MainProxy.isClient()) {
				((WorldClient) b.worldObj).removeEntityFromWorld(b.entityId);
			}
		}
		_lasers = new LinkedList<EntityBlock>();
	}
	
	private void addLeg(World worldObj, Position start, ForgeDirection o){
		Position end = new Position(start.x, start.y, start.z, o);			
		end.moveForwards(1);
		switch(o){
			case WEST: case DOWN: case NORTH:
				_lasers.add(Utils.createLaser(worldObj, end, start, _pewpewLazors));
				break;
			default:
				_lasers.add(Utils.createLaser(worldObj, start, end, _pewpewLazors));
		}
	}
	
	public void displayRoute(IRouter source, Integer destination) {
		_pewpewLazors = LaserKind.Red;
		LinkedList<Integer> routerList = new LinkedList<Integer>();
		routerList.add(destination);
		displayRoute(source, routerList);
		_pewpewLazors = LaserKind.Stripes;
	}

	
	public void displayRoute(IRouter r){
		LinkedList<Integer> knownRouters = new LinkedList<Integer>();
		ArrayList<ExitRoute> table = r.getRouteTable();
		for (int i=0; i< table.size(); i++){
			if (table.get(i)==null || i == r.getSimpleID())
				continue;
			knownRouters.add(i);
		}
		displayRoute(r, knownRouters);
	}
	
	public void displayRoute(IRouter r, LinkedList<Integer> knownRouters){
		clear();
		if (r == _lastRouter){
			_lastRouter = null;
			return;
		}
		_lastRouter = r;
		
		
		while (!knownRouters.isEmpty()){
			//Pick a router
			int targetRouter = knownRouters.pop();
			
			//Get the first exit
			ForgeDirection next = r.getRouteTable().get(targetRouter).exitOrientation;
			if (next == ForgeDirection.UNKNOWN){
				LogisticsPipes.log.warning("BAAAD MOJO");
			}
			
			IRouter nextRouter = r;
			LinkedList<Integer> visited = new LinkedList<Integer>();
			while(nextRouter.getSimpleID() != targetRouter){
				if (visited.contains(nextRouter)){
					LogisticsPipes.log.info("ROUTE LOOP");
					break;
				}
				visited.add(nextRouter.getSimpleID());
				
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
					LogisticsPipes.log.info("BAD ROUTE");
				}
				boolean ok = false;
				for (IRouter dicoveredRouter : discovered){
					if (knownRouters.contains(dicoveredRouter)){
						knownRouters.remove(dicoveredRouter);
					}
					ExitRoute source =dicoveredRouter.getRouteTable().get(targetRouter);
					if(source != null && source.containsFlag(PipeRoutingConnectionType.canRouteTo)) {
						ok = true;
						nextRouter = dicoveredRouter;
						next = dicoveredRouter.getRouteTable().get(targetRouter).exitOrientation;
					}
				}
				if (!ok){
					LogisticsPipes.log.info("DEAD ROUTE");
					break;
				}
			}
		}

		//OLD PAINT PATH LOGIC
//		Position p1 = new Position(r.getPipe().container.xCoord, r.getPipe().container.yCoord, r.getPipe().zCoord);
//		for (int i = 0; i < 6; i++){
//			ForgeDirection o = ForgeDirection.values()[i];
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
//				ForgeDirection nextOrientation = ForgeDirection.values()[i];
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
	public void addLaser(World worldObj, Position start, ForgeDirection o) {
		addLeg(worldObj, start, o);
		
	}

}
