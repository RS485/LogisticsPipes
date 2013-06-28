/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.pipes;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import logisticspipes.LogisticsPipes;
import logisticspipes.api.IRequestAPI;
import logisticspipes.interfaces.routing.IRequestItems;
import logisticspipes.logic.TemporaryLogic;
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
import logisticspipes.utils.ItemMessage;
import logisticspipes.utils.Pair;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

@CCType(name = "LogisticsPipes:Request")
public class PipeItemsRequestLogistics extends CoreRoutedPipe implements IRequestItems, IRequestAPI {
	
	private final LinkedList<Map<ItemIdentifier, Integer>> _history = new LinkedList<Map<ItemIdentifier,Integer>>(); 

	public PipeItemsRequestLogistics(int itemID) {
		super(new TemporaryLogic(), itemID);
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
		entityplayer.openGui(LogisticsPipes.instance, GuiIDs.GUI_Normal_Orderer_ID, this.worldObj, this.getX() , this.getY(), this.getZ());
	}
	
	@Override
	public boolean wrenchClicked(World world, int i, int j, int k, EntityPlayer entityplayer, SecuritySettings settings) {
		if(MainProxy.isServer(world)) {
			if (settings == null || settings.openRequest) {
				openGui(entityplayer);
			} else {
				entityplayer.sendChatToPlayer("Permission denied");
			}
		}
		return true;
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

	
	/* IRequestAPI */

	@Override
	public List<ItemStack> getProvidedItems() {
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
		final List<ItemStack> used = new LinkedList<ItemStack>();
		final List<ItemStack> missing = new LinkedList<ItemStack>();
		RequestTree.simulate(ItemIdentifier.get(wanted.itemID, wanted.getItemDamage(), wanted.getTagCompound()).makeStack(wanted.stackSize), this, new RequestLog() {
			@Override
			public void handleMissingItems(LinkedList<ItemMessage> list) {
				for(ItemMessage msg:list) {
					ItemStack is = new ItemStack(msg.id, msg.amount, msg.data);
					is.setTagCompound(msg.tag);
					missing.add(is);
				}
			}

			@Override
			public void handleSucessfullRequestOf(ItemMessage item) {}

			@Override
			public void handleSucessfullRequestOfList(LinkedList<ItemMessage> items) {
				for(ItemMessage msg:items) {
					ItemStack is = new ItemStack(msg.id, msg.amount, msg.data);
					is.setTagCompound(msg.tag);
					used.add(is);
				}
			}
		});
		SimulationResult r = new SimulationResult();
		r.used = used;
		r.missing = missing;
		return r;
	}

	@Override
	public List<ItemStack> performRequest(ItemStack wanted) {
		final List<ItemStack> missing = new LinkedList<ItemStack>();
		RequestTree.request(ItemIdentifier.get(wanted.itemID, wanted.getItemDamage(), wanted.getTagCompound()).makeStack(wanted.stackSize), this, new RequestLog() {
			@Override
			public void handleMissingItems(LinkedList<ItemMessage> list) {
outer:
				for(ItemMessage msg:list) {
					ItemStack is = new ItemStack(msg.id, msg.amount, msg.data);
					is.setTagCompound(msg.tag);
					for(ItemStack seen : missing) {
						if(seen.isItemEqual(is) && ItemStack.areItemStackTagsEqual(seen, is)) {
							seen.stackSize += is.stackSize;
							continue outer;
						}
					}
					missing.add(is);
				}
			}

			@Override
			public void handleSucessfullRequestOf(ItemMessage item) {}

			@Override
			public void handleSucessfullRequestOfList(LinkedList<ItemMessage> items) {}
		});
		return missing;
	}


	/* CC */

	@CCCommand(description="Requests the given ItemIdentifier Id with the given amount")
	@CCQueued
	public List makeRequest(Double itemId, Double amount) throws Exception {
		return makeRequest(itemId, amount, false);
	}
	@CCCommand(description="Requests the given ItemIdentifier Id with the given amount")
	@CCQueued
	public List makeRequest(Double itemId, Double amount, Boolean forceCrafting) throws Exception {
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
