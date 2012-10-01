/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.pipes;

import java.util.HashMap;
import java.util.LinkedList;

import logisticspipes.LogisticsPipes;
import logisticspipes.config.Textures;
import logisticspipes.interfaces.ILogisticsModule;
import logisticspipes.interfaces.routing.IRequestItems;
import logisticspipes.logic.TemporaryLogic;
import logisticspipes.main.GuiIDs;
import logisticspipes.main.RoutedPipe;
import logisticspipes.main.SimpleServiceLocator;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.ItemIdentifier;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.World;
import buildcraft.BuildCraftCore;

public class PipeItemsRequestLogistics extends RoutedPipe implements IRequestItems{
	
	private final LinkedList<HashMap<ItemIdentifier, Integer>> _history = new LinkedList<HashMap<ItemIdentifier,Integer>>(); 

	public PipeItemsRequestLogistics(int itemID) {
		super(new TemporaryLogic(), itemID);
	}

	@Override
	public int getCenterTexture() {
		return Textures.LOGISTICSPIPE_REQUESTER_TEXTURE;
	}

	@Override
	public ILogisticsModule getLogisticsModule() {
		return null;
	}
	
	public void openGui(EntityPlayer entityplayer) {
		//ModLoader.getMinecraftInstance().displayGuiScreen(new GuiOrderer(this, entityplayer));
		entityplayer.openGui(LogisticsPipes.instance, GuiIDs.GUI_Normal_Orderer_ID, this.worldObj, this.xCoord , this.yCoord, this.zCoord);
	}
	
	@Override
	public boolean blockActivated(World world, int i, int j, int k,	EntityPlayer entityplayer) {
		if (entityplayer.getCurrentEquippedItem() != null && entityplayer.getCurrentEquippedItem().getItem() == BuildCraftCore.wrenchItem){
			if (MainProxy.isServer(this.worldObj)) {
				openGui(entityplayer);
			}
		}
		
		return super.blockActivated(world, i, j, k, entityplayer);
	}
	
	@Override
	public void updateEntity() {
		super.updateEntity();
		if(MainProxy.isClient()) return;
		if (this.worldObj.getWorldTime() % 1200 == 0){
			_history.addLast(SimpleServiceLocator.logisticsManager.getAvailableItems(getRouter().getRouteTable().keySet()));
			if (_history.size() > 20){
				_history.removeFirst();
			}
		}
	}
	
	public LinkedList<HashMap<ItemIdentifier, Integer>>  getHistory(){
		return _history;
	}

	@Override
	public ItemSendMode getItemSendMode() {
		return ItemSendMode.Normal;
	}

//	@Override
//	public Router getRouter() {
//		return router;
//	}
}
