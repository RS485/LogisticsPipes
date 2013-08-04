/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.pipes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import logisticspipes.LogisticsPipes;
import logisticspipes.api.IRequestAPI;
import logisticspipes.interfaces.routing.IRequestItems;
import logisticspipes.modules.LogisticsModule;
import logisticspipes.network.GuiIDs;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.proxy.cc.interfaces.CCCommand;
import logisticspipes.proxy.cc.interfaces.CCQueued;
import logisticspipes.proxy.cc.interfaces.CCType;
import logisticspipes.request.RequestHandler;
import logisticspipes.request.RequestLog;
import logisticspipes.request.RequestTree;
import logisticspipes.security.SecuritySettings;
import logisticspipes.textures.Textures;
import logisticspipes.textures.Textures.TextureType;
import logisticspipes.utils.ItemIdentifier;
import logisticspipes.utils.Pair;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatMessageComponent;

@CCType(name = "LogisticsPipes:Request")
public class PipeItemsRequestLogistics extends CoreRoutedPipe implements IRequestItems, IRequestAPI {
	
	private final LinkedList<Map<ItemIdentifier, Integer>> _history = new LinkedList<Map<ItemIdentifier,Integer>>(); 

	public PipeItemsRequestLogistics(int itemID) {
		super(itemID);
	}

	@Override
	public TextureType getCenterTexture() {
		return Textures.LOGISTICSPIPE_REQUESTER_TEXTURE;
	}

	@Override
	public LogisticsModule getLogisticsModule() {
		return null;
	}
	
	public void openGui(EntityPlayer entityplayer) {
		entityplayer.openGui(LogisticsPipes.instance, GuiIDs.GUI_Normal_Orderer_ID, this.getWorld(), this.getX() , this.getY(), this.getZ());
	}
	
	@Override
	public boolean wrenchClicked(EntityPlayer entityplayer, SecuritySettings settings) {
		if(MainProxy.isServer(getWorld())) {
			if (settings == null || settings.openRequest) {
				openGui(entityplayer);
			} else {
				entityplayer.sendChatToPlayer(ChatMessageComponent.func_111066_d("Permission denied"));
			}
		}
		return true;
	}
	
	@Override
	public void enabledUpdateEntity() {
		if (this.getWorld().getWorldTime() % 1200 == 0){
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

	
	/* IRequestAPI */

	@Override
	public List<ItemStack> getProvidedItems() {
		if(stillNeedReplace())
			return new ArrayList<ItemStack>();
		Map<ItemIdentifier, Integer> items = SimpleServiceLocator.logisticsManager.getAvailableItems(getRouter().getIRoutersByCost());
		List<ItemStack> list = new ArrayList<ItemStack>(items.size());
		for(Entry <ItemIdentifier, Integer> item:items.entrySet()) {
			ItemStack is = item.getKey().unsafeMakeNormalStack(item.getValue());
			list.add(is);
		}
		return list;
	}

	@Override
	public List<ItemStack> getCraftedItems() {
		if(stillNeedReplace())
			return new ArrayList<ItemStack>();
		LinkedList<ItemIdentifier> items = SimpleServiceLocator.logisticsManager.getCraftableItems(getRouter().getIRoutersByCost());
		List<ItemStack> list = new ArrayList<ItemStack>(items.size());
		for(ItemIdentifier item:items) {
			ItemStack is = item.unsafeMakeNormalStack(0);
			list.add(is);
		}
		return list;
	}

	@Override
	public SimulationResult simulateRequest(ItemStack wanted) {
		final Map<ItemIdentifier,Integer> used = new HashMap<ItemIdentifier,Integer>();
		final Map<ItemIdentifier,Integer> missing = new HashMap<ItemIdentifier,Integer>();
		RequestTree.simulate(ItemIdentifier.get(wanted.itemID, wanted.getItemDamage(), wanted.getTagCompound()).makeStack(wanted.stackSize), this, new RequestLog() {
			@Override
			public void handleMissingItems(Map<ItemIdentifier,Integer> items) {
				for(Entry<ItemIdentifier,Integer>e:items.entrySet()) {
					Integer count = missing.get(e.getKey());
					if(count == null)
						count = 0;
					count += e.getValue();
					missing.put(e.getKey(), count);
				}
			}

			@Override
			public void handleSucessfullRequestOf(ItemIdentifier item, int count) {}

			@Override
			public void handleSucessfullRequestOfList(Map<ItemIdentifier,Integer> items) {
				for(Entry<ItemIdentifier,Integer>e:items.entrySet()) {
					Integer count = used.get(e.getKey());
					if(count == null)
						count = 0;
					count += e.getValue();
					used.put(e.getKey(), count);
				}
			}
		});
		List<ItemStack> usedList = new ArrayList<ItemStack>(used.size());
		List<ItemStack> missingList = new ArrayList<ItemStack>(missing.size());
		for(Entry<ItemIdentifier,Integer>e:used.entrySet()) {
			usedList.add(e.getKey().unsafeMakeNormalStack(e.getValue()));
		}
		for(Entry<ItemIdentifier,Integer>e:missing.entrySet()) {
			missingList.add(e.getKey().unsafeMakeNormalStack(e.getValue()));
		}
		
		SimulationResult r = new SimulationResult();
		r.used = usedList;
		r.missing = missingList;
		return r;
	}

	@Override
	public List<ItemStack> performRequest(ItemStack wanted) {
		final Map<ItemIdentifier,Integer> missing = new HashMap<ItemIdentifier,Integer>();
		RequestTree.request(ItemIdentifier.get(wanted.itemID, wanted.getItemDamage(), wanted.getTagCompound()).makeStack(wanted.stackSize), this, new RequestLog() {
			@Override
			public void handleMissingItems(Map<ItemIdentifier,Integer> items) {
				for(Entry<ItemIdentifier,Integer>e:items.entrySet()) {
					Integer count = missing.get(e.getKey());
					if(count == null)
						count = 0;
					count += e.getValue();
					missing.put(e.getKey(), count);
				}
			}

			@Override
			public void handleSucessfullRequestOf(ItemIdentifier item, int count) {}

			@Override
			public void handleSucessfullRequestOfList(Map<ItemIdentifier,Integer> items) {}
		});
		List<ItemStack> missingList = new ArrayList<ItemStack>(missing.size());
		for(Entry<ItemIdentifier,Integer>e:missing.entrySet()) {
			missingList.add(e.getKey().unsafeMakeNormalStack(e.getValue()));
		}

		return missingList;
	}


	/* CC */

	@CCCommand(description="Requests the given ItemIdentifier Id with the given amount")
	@CCQueued
	public Object[] makeRequest(Double itemId, Double amount) throws Exception {
		return makeRequest(itemId, amount, false);
	}
	@CCCommand(description="Requests the given ItemIdentifier Id with the given amount")
	@CCQueued
	public Object[] makeRequest(Double itemId, Double amount, Boolean forceCrafting) throws Exception {
		if(forceCrafting==null)
			forceCrafting=false;
		ItemIdentifier item = ItemIdentifier.getForId((int)Math.floor(itemId));
		if(item == null) throw new Exception("Invalid ItemIdentifierID");
		return RequestHandler.computerRequest(item.makeStack((int)Math.floor(amount)), this,forceCrafting);
	}

	@CCCommand(description="Asks for all available ItemIdentifier inside the Logistics Network")
	@CCQueued
	public List<Pair<ItemIdentifier, Integer>> getAvailableItems() {
		Map<ItemIdentifier, Integer> items = SimpleServiceLocator.logisticsManager.getAvailableItems(getRouter().getIRoutersByCost());
		List<Pair<ItemIdentifier, Integer>> list = new LinkedList<Pair<ItemIdentifier, Integer>>();
		for(Entry<ItemIdentifier, Integer> item:items.entrySet()) {
			int amount = item.getValue();
			list.add(new Pair<ItemIdentifier,Integer>(item.getKey(), amount));
		}
		return list;
	}

	@CCCommand(description="Asks for all craftable ItemIdentifier inside the Logistics Network")
	@CCQueued
	public List<ItemIdentifier> getCraftableItems() {
		LinkedList<ItemIdentifier> items = SimpleServiceLocator.logisticsManager.getCraftableItems(getRouter().getIRoutersByCost());
		return items;
	}
}
