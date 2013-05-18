/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.logic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import logisticspipes.LogisticsPipes;
import logisticspipes.blocks.LogisticsSecurityTileEntity;
import logisticspipes.config.Configs;
import logisticspipes.network.GuiIDs;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.routing.ExitRoute;
import logisticspipes.routing.IRouter;
import logisticspipes.routing.ServerRouter;
import logisticspipes.security.SecuritySettings;
import logisticspipes.utils.ItemIdentifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.transport.pipes.PipeLogic;

public abstract class BaseRoutingLogic extends PipeLogic{
	
	public CoreRoutedPipe getRoutedPipe(){
		return (CoreRoutedPipe) this.container.pipe;
	}
	
	public abstract void onWrenchClicked(EntityPlayer entityplayer);
	
	public abstract void destroy();
	
	protected int throttleTime = 20;
	private int throttleTimeLeft = 20 + new Random().nextInt(Configs.LOGISTICS_DETECTION_FREQUENCY);
	
	
	@Override
	public void updateEntity() {
		super.updateEntity();
		if (--throttleTimeLeft > 0) return;
		throttledUpdateEntity();
		throttleTimeLeft = throttleTime;
	}
	
	public void throttledUpdateEntity(){}
	
	protected void delayThrottle() {
		//delay 6(+1) ticks to prevent suppliers from ticking between a item arriving at them and the item hitting their adj. inv
		if(throttleTimeLeft < 7)
			throttleTimeLeft = 7;
	}
	
	@Override
	public boolean blockActivated(EntityPlayer entityplayer) {
		SecuritySettings settings = null;
		if(MainProxy.isServer(entityplayer.worldObj)) {
			LogisticsSecurityTileEntity station = SimpleServiceLocator.securityStationManager.getStation(getRoutedPipe().getUpgradeManager().getSecurityID());
			if(station != null) {
				settings = station.getSecuritySettingsForPlayer(entityplayer, false);
			}
		}
		if (entityplayer.getCurrentEquippedItem() == null) {
			if (!entityplayer.isSneaking()) return false;
			//getRoutedPipe().getRouter().displayRoutes();
			if (LogisticsPipes.DEBUG) {
				doDebugStuff(entityplayer);
			}
			return true;
		} else if (entityplayer.getCurrentEquippedItem().getItem() == LogisticsPipes.LogisticsNetworkMonitior && (settings == null || settings.openNetworkMonitor)) {
			if(MainProxy.isServer(entityplayer.worldObj)) {
				entityplayer.openGui(LogisticsPipes.instance, GuiIDs.GUI_RoutingStats_ID, worldObj, xCoord, yCoord, zCoord);
			}
			return true;
		} else if (SimpleServiceLocator.buildCraftProxy.isWrenchEquipped(entityplayer) && (settings == null || settings.openGui)) {
			onWrenchClicked(entityplayer);
			return true;
		} else if (entityplayer.getCurrentEquippedItem().getItem() == LogisticsPipes.LogisticsRemoteOrderer && (settings == null || settings.openRequest)) {
			if(MainProxy.isServer(entityplayer.worldObj)) {
				entityplayer.openGui(LogisticsPipes.instance, GuiIDs.GUI_Normal_Orderer_ID, worldObj, xCoord, yCoord, zCoord);
			}
			return true;
		} else if(entityplayer.getCurrentEquippedItem().getItem() == LogisticsPipes.LogisticsRemoteOrderer) {
			if(MainProxy.isServer(entityplayer.worldObj)) {
				entityplayer.sendChatToPlayer("Permission denied");
			}
			return true;
		} else if(entityplayer.getCurrentEquippedItem().getItem() == LogisticsPipes.LogisticsNetworkMonitior) {
			if(MainProxy.isServer(entityplayer.worldObj)) {
				entityplayer.sendChatToPlayer("Permission denied");
			}
			return true;
		}
		return super.blockActivated(entityplayer);
	}
	
	private void doDebugStuff(EntityPlayer entityplayer) {
		//entityplayer.worldObj.setWorldTime(4951);
		IRouter r = getRoutedPipe().getRouter();
		if(!(r instanceof ServerRouter)) return;
		System.out.println("***");
		System.out.println("---------Interests---------------");
		for(Entry<ItemIdentifier, Set<IRouter>> i: ServerRouter.getInterestedInSpecifics().entrySet()){
			System.out.print(i.getKey().getFriendlyName()+":");
			for(IRouter j:i.getValue())
				System.out.print(j.getSimpleID()+",");
			System.out.println();
		}
		
		System.out.print("ALL ITEMS:");
		for(IRouter j:ServerRouter.getInterestedInGeneral())
			System.out.print(j.getSimpleID()+",");
		System.out.println();
			
		
		
		
		ServerRouter sr = (ServerRouter) r;
		
		System.out.println(r.toString());
		System.out.println("---------CONNECTED TO---------------");
		for (CoreRoutedPipe adj : sr._adjacent.keySet()) {
			System.out.println(adj.getRouter().getSimpleID());
		}
		System.out.println();
		System.out.println("========DISTANCE TABLE==============");
		for(ExitRoute n : r.getIRoutersByCost()) {
			System.out.println(n.destination.getSimpleID()+ " @ " + n.distanceToDestination + " -> "+ n.connectionDetails +"("+n.destination.getId() +")");
		}
		System.out.println();
		System.out.println("*******EXIT ROUTE TABLE*************");
		ArrayList<ExitRoute> table = r.getRouteTable();
		for (int i=0; i < table.size(); i++){			
			if(table.get(i)!=null)
			System.out.println(i + " -> " + r.getSimpleID() + " via " + table.get(i).exitOrientation + "(" + table.get(i) + " distance)");
		}
		System.out.println();
		System.out.println("++++++++++CONNECTIONS+++++++++++++++");
		System.out.println(Arrays.toString(ForgeDirection.VALID_DIRECTIONS));
		System.out.println(Arrays.toString(sr.sideDisconnected));
		System.out.println(Arrays.toString(getRoutedPipe().container.pipeConnectionsBuffer));
		System.out.println();
		System.out.println("~~~~~~~~~~~~~~~POWER~~~~~~~~~~~~~~~~");
		System.out.println(r.getPipe().getRoutedPowerProviders());
		System.out.println(r.getPowerProvider());
		getRoutedPipe().refreshConnectionAndRender(true);
	}

}
