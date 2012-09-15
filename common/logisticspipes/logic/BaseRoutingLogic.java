/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.logic;

import logisticspipes.LogisticsPipes;
import logisticspipes.main.GuiIDs;
import logisticspipes.main.RoutedPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.routing.IRouter;
import net.minecraft.src.EntityPlayer;
import buildcraft.transport.pipes.PipeLogic;

public abstract class BaseRoutingLogic extends PipeLogic{
	
	public RoutedPipe getRoutedPipe(){
		return (RoutedPipe) this.container.pipe;
	}
	
	public IRouter getRouter(){
		return getRoutedPipe().getRouter();
	}
	
	public abstract void onWrenchClicked(EntityPlayer entityplayer);
	
	public abstract void destroy();
	
	protected int throttleTime = 20;
	private int throttleTimeLeft = 0;
	
	
	@Override
	public void updateEntity() {
		super.updateEntity();
		if (--throttleTimeLeft > 0) return;
		throttledUpdateEntity();
		resetThrottle();
	}
	
	public void throttledUpdateEntity(){}
	
	protected void resetThrottle(){
		throttleTimeLeft = throttleTime;
	}
	
	@Override
	public boolean blockActivated(EntityPlayer entityplayer) {
		if (entityplayer.getCurrentEquippedItem() == null)	{
			if (!entityplayer.isSneaking()) return false;
			getRouter().displayRoutes();
			if (LogisticsPipes.DEBUG) {
				doDebugStuff(entityplayer);
			}
			return true;
		} else if (entityplayer.getCurrentEquippedItem().getItem() == LogisticsPipes.LogisticsNetworkMonitior){
			if(MainProxy.isServer(entityplayer.worldObj)) {
				entityplayer.openGui(LogisticsPipes.instance, GuiIDs.GUI_RoutingStats_ID, worldObj, xCoord, yCoord, zCoord);
			}
			return true;
		} else if (entityplayer.getCurrentEquippedItem().getItem() == buildcraft.BuildCraftCore.wrenchItem){
			onWrenchClicked(entityplayer);
			return true;
		} else if (entityplayer.getCurrentEquippedItem().getItem() == LogisticsPipes.LogisticsRemoteOrderer) {
			if(MainProxy.isServer(entityplayer.worldObj)) {
				entityplayer.openGui(LogisticsPipes.instance, GuiIDs.GUI_Normal_Orderer_ID, worldObj, xCoord, yCoord, zCoord);
			}
			return true;
		}
		return super.blockActivated(entityplayer);
	}
	
	private void doDebugStuff(EntityPlayer entityplayer){
		entityplayer.worldObj.setWorldTime(4951);
		System.out.println("***");
		IRouter r = getRouter();
//		
		System.out.println("ID: " + r.getId().toString());
//		System.out.println("---------CONNECTED TO---------------");
//		for (RoutedPipe adj : r._adjacent.keySet())
//		{
//			System.out.println(adj.getRouterId());
//		}
//		System.out.println("*******ROUTE TABLE**************");
//		for (Router p : r.getRouteTable().keySet())
//		{
//			System.out.println(p.getId() + " -> " + r.getRouteTable().get(p).toString());
//		}
		
		System.out.println();
		System.out.println();
//			//Give stuff! for debug purpose, ensure commented before release
//			entityplayer.inventory.addItemStackToInventory(new ItemStack(net.minecraft.src.BuildCraftFactory.autoWorkbenchBlock, 64));
//			entityplayer.inventory.addItemStackToInventory(new ItemStack(Block.chest, 64));
//			entityplayer.inventory.addItemStackToInventory(new ItemStack(net.minecraft.src.BuildCraftCore.woodenGearItem, 64));
//			entityplayer.inventory.addItemStackToInventory(new ItemStack(Block.wood, 64));				
//			entityplayer.inventory.addItemStackToInventory(new ItemStack(Block.brick, 64));
//			entityplayer.inventory.addItemStackToInventory(new ItemStack(Block.dirt, 64));
//			entityplayer.inventory.addItemStackToInventory(new ItemStack(Block.cobblestone, 64));		
//			entityplayer.inventory.addItemStackToInventory(new ItemStack(Item.pickaxeDiamond, 1));
//			entityplayer.inventory.addItemStackToInventory(new ItemStack(Item.shovelDiamond, 1));
//			entityplayer.inventory.addItemStackToInventory(new ItemStack(Block.cobblestone, 64));						
//			entityplayer.inventory.addItemStackToInventory(new ItemStack(Block.torchRedstoneActive, 64));
//			entityplayer.inventory.addItemStackToInventory(new ItemStack(Item.coal, 64));
//			entityplayer.inventory.addItemStackToInventory(new ItemStack(mod_zAdditionalPipes.pipeItemTeleport, 64));
		//entityplayer.inventory.addItemStackToInventory(new ItemStack(Item.redstone, 64));
		//entityplayer.inventory.addItemStackToInventory(new ItemStack(BuildCraftCore.diamondGearItem, 64));
		//entityplayer.inventory.addItemStackToInventory(new ItemStack(Item.diamond, 64));
		
		
//			entityplayer.inventory.addItemStackToInventory(new ItemStack(net.minecraft.src.BuildCraftTransport.pipeItemsDiamond, 64));
//			entityplayer.inventory.addItemStackToInventory(new ItemStack(net.minecraft.src.BuildCraftTransport.pipeItemsWood, 64));
//			entityplayer.inventory.addItemStackToInventory(new ItemStack(BuildCraftEnergy.engineBlock, 64, 0));
//			entityplayer.inventory.addItemStackToInventory(new ItemStack(Block.torchRedstoneActive, 64));
//			entityplayer.inventory.addItemStackToInventory(new ItemStack(Item.redstone, 64));
//			entityplayer.inventory.addItemStackToInventory(new ItemStack(Block.torchWood, 64));
//			entityplayer.inventory.addItemStackToInventory(new ItemStack(net.minecraft.src.BuildCraftTransport.pipeItemsIron, 64));
//			entityplayer.inventory.addItemStackToInventory(new ItemStack(net.minecraft.src.BuildCraftTransport.pipeItemsObsidian, 64));
//			entityplayer.inventory.addItemStackToInventory(new ItemStack(net.minecraft.src.BuildCraftCore.wrenchItem, 1));

//			entityplayer.inventory.addItemStackToInventory(new ItemStack(Block.glass, 64));
//			entityplayer.inventory.addItemStackToInventory(new ItemStack(BuildCraftCore.goldGearItem, 64));
//			entityplayer.inventory.addItemStackToInventory(new ItemStack(BuildCraftTransport.pipeItemsDiamond, 64));
//			entityplayer.inventory.addItemStackToInventory(new ItemStack(Block.torchRedstoneActive, 1));
//			entityplayer.inventory.addItemStackToInventory(new ItemStack(BuildCraftFactory.autoWorkbenchBlock, 64));
	}

}
