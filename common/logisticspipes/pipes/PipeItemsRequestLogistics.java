/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.pipes;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import logisticspipes.LogisticsPipes;
import logisticspipes.interfaces.ILogisticsModule;
import logisticspipes.interfaces.routing.IRequestItems;
import logisticspipes.logic.TemporaryLogic;
import logisticspipes.network.GuiIDs;
import logisticspipes.pipes.basic.RoutedPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.proxy.cc.interfaces.CCCommand;
import logisticspipes.proxy.cc.interfaces.CCQueued;
import logisticspipes.proxy.cc.interfaces.CCType;
import logisticspipes.request.RequestHandler;
import logisticspipes.textures.Textures;
import logisticspipes.textures.Textures.TextureType;
import logisticspipes.utils.ItemIdentifier;
import logisticspipes.utils.Pair;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

@CCType(name = "LogisticsPipes:Request")
public class PipeItemsRequestLogistics extends RoutedPipe implements IRequestItems {
	
	private final LinkedList<Map<ItemIdentifier, Integer>> _history = new LinkedList<Map<ItemIdentifier,Integer>>(); 

	public PipeItemsRequestLogistics(int itemID) {
		super(new TemporaryLogic(), itemID);
	}

	@Override
	public TextureType getCenterTexture() {
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
		if (SimpleServiceLocator.buildCraftProxy.isWrenchEquipped(entityplayer)) {
			if (MainProxy.isServer(this.worldObj)) {
				openGui(entityplayer);
			}
		}
		
		return super.blockActivated(world, i, j, k, entityplayer);
	}
	
	@Override
	public void enabledUpdateEntity() {
		if (this.worldObj.getWorldTime() % 1200 == 0){
			_history.addLast(SimpleServiceLocator.logisticsManager.getAvailableItems(getRouter().getIRoutersByCost()));
			if (_history.size() > 20){
				_history.removeFirst();
			}
		}
	}
	
	public LinkedList<Map<ItemIdentifier, Integer>> getHistory(){
		return _history;
	}

	@Override
	public ItemSendMode getItemSendMode() {
		return ItemSendMode.Normal;
	}
	
	@CCCommand(description="Requests the given ItemIdentifier Id with the given amount")
	@CCQueued(event="request_successfull\n      ---request_failed", realQueue=false)
	public int makeRequest(Double itemId, Double amount) throws Exception {
		ItemIdentifier item = ItemIdentifier.getForId((int)Math.floor(itemId));
		if(item == null) throw new Exception("Invalid ItemIdentifierID");
		return RequestHandler.computerRequest(item.makeStack((int)Math.floor(amount)), this);
	}

	@CCCommand(description="Asks for all available ItemIdentifier inside the Logistics Network")
	@CCQueued(event="available_items_return")
	public List<Pair<ItemIdentifier, Integer>> getAvailableItems() {
		Map<ItemIdentifier, Integer> items = SimpleServiceLocator.logisticsManager.getAvailableItems(getRouter().getIRoutersByCost());
		List<Pair<ItemIdentifier, Integer>> list = new LinkedList<Pair<ItemIdentifier, Integer>>();
		for(ItemIdentifier item:items.keySet()) {
			int amount = items.get(item);
			list.add(new Pair<ItemIdentifier,Integer>(item, amount));
		}
		return list;
	}

	@CCCommand(description="Asks for all craftable ItemIdentifier inside the Logistics Network")
	@CCQueued(event="craftable_items_return")
	public List<ItemIdentifier> getCraftableItems() {
		LinkedList<ItemIdentifier> items = SimpleServiceLocator.logisticsManager.getCraftableItems(getRouter().getIRoutersByCost());
		return items;
	}
}
