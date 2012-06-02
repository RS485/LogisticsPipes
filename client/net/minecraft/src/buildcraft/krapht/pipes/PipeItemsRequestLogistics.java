/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.krapht.pipes;

import java.util.HashMap;
import java.util.LinkedList;

import net.minecraft.src.BuildCraftCore;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ModLoader;
import net.minecraft.src.World;
import net.minecraft.src.core_LogisticsPipes;
import net.minecraft.src.buildcraft.krapht.IRequestItems;
import net.minecraft.src.buildcraft.krapht.RoutedPipe;
import net.minecraft.src.buildcraft.krapht.gui.GuiOrderer;
import net.minecraft.src.buildcraft.krapht.logic.TemporaryLogic;
import net.minecraft.src.buildcraft.logisticspipes.modules.ILogisticsModule;
import net.minecraft.src.krapht.ItemIdentifier;

public class PipeItemsRequestLogistics extends RoutedPipe implements IRequestItems{
	
	private final LinkedList<HashMap<ItemIdentifier, Integer>> _history = new LinkedList<HashMap<ItemIdentifier,Integer>>(); 

	public PipeItemsRequestLogistics(int itemID) {
		super(new TemporaryLogic(), itemID);
	}

	@Override
	public int getCenterTexture() {
		return core_LogisticsPipes.LOGISTICSPIPE_REQUESTER_TEXTURE;
	}

	@Override
	public ILogisticsModule getLogisticsModule() {
		return null;
	}

	@Override
	public boolean blockActivated(World world, int i, int j, int k,	EntityPlayer entityplayer) {
		if (entityplayer.getCurrentEquippedItem() != null && entityplayer.getCurrentEquippedItem().getItem() == BuildCraftCore.wrenchItem){
			//ModLoader.getMinecraftInstance().displayGuiScreen(new GuiRequester(_history, ItemIdentifier.get(new ItemStack(Block.dirt, 1))));
			ModLoader.getMinecraftInstance().displayGuiScreen(new GuiOrderer(this, entityplayer));
		}
		
		return super.blockActivated(world, i, j, k, entityplayer);
	}
	
	@Override
	public void updateEntity() {
		super.updateEntity();
		if (this.worldObj.getWorldTime() % 1200 == 0){
			_history.addLast(core_LogisticsPipes.logisticsManager.getAvailableItems(getRouter().getRouteTable().keySet()));
			if (_history.size() > 20){
				_history.removeFirst();
			}
		}
	}
	
	public LinkedList<HashMap<ItemIdentifier, Integer>>  getHistory(){
		return _history;
	}

//	@Override
//	public Router getRouter() {
//		return router;
//	}
}
