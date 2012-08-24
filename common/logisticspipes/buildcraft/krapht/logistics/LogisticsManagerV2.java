/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.buildcraft.krapht.logistics;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.UUID;

import javax.swing.plaf.basic.BasicInternalFrameTitlePane.MaximizeAction;

import logisticspipes.buildcraft.krapht.CoreRoutedPipe;
import logisticspipes.buildcraft.krapht.PipeTransportLogistics;
import logisticspipes.buildcraft.krapht.SimpleServiceLocator;
import logisticspipes.buildcraft.krapht.pipes.PipeLogisticsChassi;
import logisticspipes.buildcraft.krapht.routing.IRouter;
import logisticspipes.buildcraft.krapht.routing.RoutedEntityItem;
import logisticspipes.buildcraft.krapht.routing.Router;
import logisticspipes.buildcraft.logisticspipes.IRoutedItem;
import logisticspipes.buildcraft.logisticspipes.IRoutedItem.TransportMode;
import logisticspipes.buildcraft.logisticspipes.modules.ILogisticsModule;
import logisticspipes.buildcraft.logisticspipes.modules.SinkReply;
import logisticspipes.krapht.ItemIdentifier;
import logisticspipes.krapht.Pair;


import net.minecraft.src.ItemStack;
import buildcraft.core.EntityPassiveItem;
import buildcraft.api.core.Orientations;

public class LogisticsManagerV2 implements ILogisticsManagerV2{
	
	@Override
	public boolean hasDestination(ItemStack stack, boolean allowDefault, UUID sourceRouter, boolean excludeSource) {
		if (!SimpleServiceLocator.routerManager.isRouter(sourceRouter)) return false;
		Pair<UUID, SinkReply> search = getBestReply(stack, SimpleServiceLocator.routerManager.getRouter(sourceRouter), excludeSource);
		
		if (search.getValue2() == null) return false;
		
		return (allowDefault || !search.getValue2().isDefault);
	}
	
	private Pair<UUID, SinkReply> getBestReply(ItemStack item, IRouter sourceRouter, boolean excludeSource){
		UUID potentialDestination = null;
		SinkReply bestReply = null;
		
		for (IRouter candidateRouter : sourceRouter.getIRoutersByCost()){
			if (excludeSource && candidateRouter.getId().equals(sourceRouter.getId())) continue;
			ILogisticsModule module = candidateRouter.getLogisticsModule();
			if (candidateRouter.getPipe() == null || !candidateRouter.getPipe().isEnabled()) continue;
			if (module == null) continue;
			SinkReply reply = module.sinksItem(item);
			if (reply == null) continue;
			if (bestReply == null){
				potentialDestination = candidateRouter.getId();
				bestReply = reply;
				continue;
			}
			
			if (reply.fixedPriority.ordinal() > bestReply.fixedPriority.ordinal()){
				bestReply = reply;
				potentialDestination = candidateRouter.getId();
				continue;
			}
			
			if (reply.fixedPriority == bestReply.fixedPriority && reply.customPriority >  bestReply.customPriority){
				bestReply = reply;
				potentialDestination = candidateRouter.getId();
				continue;
			}
		}
		Pair<UUID, SinkReply> result = new Pair<UUID, SinkReply>(potentialDestination, bestReply);
		return result;
	}
	
	
	
	@Override
	public IRoutedItem assignDestinationFor(IRoutedItem item, UUID sourceRouterUUID, boolean excludeSource) {
		
		//If the source router does not exist we can't do anything with this
		if (!SimpleServiceLocator.routerManager.isRouter(sourceRouterUUID)) return item;
		//If we for some reason can't get the router we can't do anything either
		IRouter sourceRouter = SimpleServiceLocator.routerManager.getRouter(sourceRouterUUID);
		if (sourceRouter == null) return item;
		
		//Wipe current destination
		item.setDestination(null);
		
//		UUID potentialDestination = null;
//		SinkReply bestReply = null;
		
		Pair<UUID, SinkReply> bestReply = getBestReply(item.getItemStack(), sourceRouter, excludeSource);
		
//		for (IRouter candidateRouter : sourceRouter.getIRoutersByCost()){
//			if (excludeSource && candidateRouter.getId().equals(sourceRouterUUID)) continue;
//			ILogisticsModule module = candidateRouter.getLogisticsModule();
//			if (module == null) continue;
//			SinkReply reply = module.sinksItem(ItemIdentifier.get(item.getItemStack()));
//			if (reply == null) continue;
//			if (bestReply == null){
//				potentialDestination = candidateRouter.getId();
//				bestReply = reply;
//				continue;
//			}
//			
//			if (reply.fixedPriority.ordinal() > bestReply.fixedPriority.ordinal()){
//				bestReply = reply;
//				potentialDestination = candidateRouter.getId();
//				continue;
//			}
//			
//			if (reply.fixedPriority == bestReply.fixedPriority && reply.customPriority >  bestReply.customPriority){
//				bestReply = reply;
//				potentialDestination = candidateRouter.getId();
//				continue;
//			}
//		}
		item.setSource(sourceRouterUUID);
		if (bestReply.getValue1() != null){
			item.setDestination(bestReply.getValue1());
			if (bestReply.getValue2().isPassive){
				if (bestReply.getValue2().isDefault){
					item.setTransportMode(TransportMode.Default);
				} else {
					item.setTransportMode(TransportMode.Passive);
				}
			}
		}
		
		return item;
	}

	@Override
	public IRoutedItem destinationUnreachable(IRoutedItem item,	UUID currentRouter) {
		// TODO Auto-generated method stub
		return assignDestinationFor(item, currentRouter, false);
	}

//	@Override
//	public boolean destinationStillValid(IRoutedItem item,	UUID destinationRouter) {
//		IRouter destination = SimpleServiceLocator.routerManager.getRouter(destinationRouter);
//		if (destination == null) return false;
//		ILogisticsModule module = destination.getLogisticsModule();
//		if (module == null) return false;
//		return module.sinksItem(ItemIdentifier.get(item.getItemStack())) != null;	
//	}
}
