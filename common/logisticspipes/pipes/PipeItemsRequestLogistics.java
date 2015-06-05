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
import logisticspipes.modules.abstractmodules.LogisticsModule;
import logisticspipes.network.GuiIDs;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.proxy.computers.interfaces.CCCommand;
import logisticspipes.proxy.computers.interfaces.CCQueued;
import logisticspipes.proxy.computers.interfaces.CCType;
import logisticspipes.request.RequestHandler;
import logisticspipes.request.RequestLog;
import logisticspipes.request.RequestTree;
import logisticspipes.request.resources.DictResource;
import logisticspipes.request.resources.IResource;
import logisticspipes.request.resources.ItemResource;
import logisticspipes.routing.order.LinkedLogisticsOrderList;
import logisticspipes.security.SecuritySettings;
import logisticspipes.textures.Textures;
import logisticspipes.textures.Textures.TextureType;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.item.ItemIdentifierStack;
import logisticspipes.utils.tuples.Pair;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;

@CCType(name = "LogisticsPipes:Request")
public class PipeItemsRequestLogistics extends CoreRoutedPipe implements IRequestItems, IRequestAPI {

	private final LinkedList<Map<ItemIdentifier, Integer>> _history = new LinkedList<Map<ItemIdentifier, Integer>>();

	public PipeItemsRequestLogistics(Item item) {
		super(item);
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
		entityplayer.openGui(LogisticsPipes.instance, GuiIDs.GUI_Normal_Orderer_ID, getWorld(), getX(), getY(), getZ());
	}

	@Override
	public boolean handleClick(EntityPlayer entityplayer, SecuritySettings settings) {
		if (SimpleServiceLocator.toolWrenchHandler.isWrenchEquipped(entityplayer) && SimpleServiceLocator.toolWrenchHandler.canWrench(entityplayer, getX(), getY(), getZ())) {
			if (MainProxy.isServer(getWorld())) {
				if (settings == null || settings.openRequest) {
					openGui(entityplayer);
				} else {
					entityplayer.addChatMessage(new ChatComponentText("Permission denied"));
				}
			}
			SimpleServiceLocator.toolWrenchHandler.wrenchUsed(entityplayer, getX(), getY(), getZ());
			return true;
		}
		return false;
	}

	@Override
	public void enabledUpdateEntity() {
		super.enabledUpdateEntity();
		if (getWorld().getTotalWorldTime() % 1200 == 0) {
			_history.addLast(SimpleServiceLocator.logisticsManager.getAvailableItems(getRouter().getIRoutersByCost()));
			if (_history.size() > 20) {
				_history.removeFirst();
			}
		}
	}

	public LinkedList<Map<ItemIdentifier, Integer>> getHistory() {
		return _history;
	}

	@Override
	public ItemSendMode getItemSendMode() {
		return ItemSendMode.Normal;
	}

	/* IRequestAPI */

	@Override
	public List<ItemStack> getProvidedItems() {
		if (stillNeedReplace()) {
			return new ArrayList<ItemStack>();
		}
		Map<ItemIdentifier, Integer> items = SimpleServiceLocator.logisticsManager.getAvailableItems(getRouter().getIRoutersByCost());
		List<ItemStack> list = new ArrayList<ItemStack>(items.size());
		for (Entry<ItemIdentifier, Integer> item : items.entrySet()) {
			ItemStack is = item.getKey().unsafeMakeNormalStack(item.getValue());
			list.add(is);
		}
		return list;
	}

	@Override
	public List<ItemStack> getCraftedItems() {
		if (stillNeedReplace()) {
			return new ArrayList<ItemStack>();
		}
		LinkedList<ItemIdentifier> items = SimpleServiceLocator.logisticsManager.getCraftableItems(getRouter().getIRoutersByCost());
		List<ItemStack> list = new ArrayList<ItemStack>(items.size());
		for (ItemIdentifier item : items) {
			ItemStack is = item.unsafeMakeNormalStack(0);
			list.add(is);
		}
		return list;
	}

	@Override
	public SimulationResult simulateRequest(ItemStack wanted) {
		final List<IResource> used = new ArrayList<IResource>();
		final List<IResource> missing = new ArrayList<IResource>();
		RequestTree.simulate(ItemIdentifier.get(wanted).makeStack(wanted.stackSize), this, new RequestLog() {

			@Override
			public void handleMissingItems(List<IResource> items) {
				missing.addAll(items);
			}

			@Override
			public void handleSucessfullRequestOf(IResource item, LinkedLogisticsOrderList parts) {}

			@Override
			public void handleSucessfullRequestOfList(List<IResource> items, LinkedLogisticsOrderList parts) {
				used.addAll(items);
			}
		});
		List<ItemStack> usedList = new ArrayList<ItemStack>(used.size());
		List<ItemStack> missingList = new ArrayList<ItemStack>(missing.size());
		for (IResource e : used) {
			if (e instanceof ItemResource) {
				usedList.add(((ItemResource) e).getItem().unsafeMakeNormalStack(e.getRequestedAmount()));
			} else if (e instanceof DictResource) {
				usedList.add(((DictResource) e).getItem().unsafeMakeNormalStack(e.getRequestedAmount()));
			}
		}
		for (IResource e : missing) {
			if (e instanceof ItemResource) {
				missingList.add(((ItemResource) e).getItem().unsafeMakeNormalStack(e.getRequestedAmount()));
			} else if (e instanceof DictResource) {
				missingList.add(((DictResource) e).getItem().unsafeMakeNormalStack(e.getRequestedAmount()));
			}
		}

		SimulationResult r = new SimulationResult();
		r.used = usedList;
		r.missing = missingList;
		return r;
	}

	@Override
	public List<ItemStack> performRequest(ItemStack wanted) {
		final List<IResource> missing = new ArrayList<IResource>();
		RequestTree.request(ItemIdentifier.get(wanted).makeStack(wanted.stackSize), this, new RequestLog() {

			@Override
			public void handleMissingItems(List<IResource> items) {
				missing.addAll(items);
			}

			@Override
			public void handleSucessfullRequestOf(IResource item, LinkedLogisticsOrderList parts) {}

			@Override
			public void handleSucessfullRequestOfList(List<IResource> items, LinkedLogisticsOrderList parts) {}
		}, null);
		List<ItemStack> missingList = new ArrayList<ItemStack>(missing.size());
		for (IResource e : missing) {
			if (e instanceof ItemResource) {
				missingList.add(((ItemResource) e).getItem().unsafeMakeNormalStack(e.getRequestedAmount()));
			} else if (e instanceof DictResource) {
				missingList.add(((DictResource) e).getItem().unsafeMakeNormalStack(e.getRequestedAmount()));
			}
		}

		return missingList;
	}

	/* CC */
	@CCCommand(description = "Requests the given ItemIdentifierStack")
	@CCQueued
	public Object[] makeRequest(ItemIdentifierStack stack) throws Exception {
		return makeRequest(stack.getItem(), Double.valueOf(stack.getStackSize()), false);
	}

	@CCCommand(description = "Requests the given ItemIdentifierStack")
	@CCQueued
	public Object[] makeRequest(ItemIdentifierStack stack, Boolean forceCrafting) throws Exception {
		return makeRequest(stack.getItem(), Double.valueOf(stack.getStackSize()), forceCrafting);
	}

	@CCCommand(description = "Requests the given ItemIdentifier with the given amount")
	@CCQueued
	public Object[] makeRequest(ItemIdentifier item, Double amount) throws Exception {
		return makeRequest(item, amount, false);
	}

	@CCCommand(description = "Requests the given ItemIdentifier with the given amount")
	@CCQueued
	public Object[] makeRequest(ItemIdentifier item, Double amount, Boolean forceCrafting) throws Exception {
		if (forceCrafting == null) {
			forceCrafting = false;
		}
		if (item == null) {
			throw new Exception("Invalid ItemIdentifier");
		}
		return RequestHandler.computerRequest(item.makeStack((int) Math.floor(amount)), this, forceCrafting);
	}

	@CCCommand(description = "Asks for all available ItemIdentifier inside the Logistics Network")
	@CCQueued
	public List<Pair<ItemIdentifier, Integer>> getAvailableItems() {
		Map<ItemIdentifier, Integer> items = SimpleServiceLocator.logisticsManager.getAvailableItems(getRouter().getIRoutersByCost());
		List<Pair<ItemIdentifier, Integer>> list = new LinkedList<Pair<ItemIdentifier, Integer>>();
		for (Entry<ItemIdentifier, Integer> item : items.entrySet()) {
			int amount = item.getValue();
			list.add(new Pair<ItemIdentifier, Integer>(item.getKey(), amount));
		}
		return list;
	}

	@CCCommand(description = "Asks for all craftable ItemIdentifier inside the Logistics Network")
	@CCQueued
	public List<ItemIdentifier> getCraftableItems() {
		LinkedList<ItemIdentifier> items = SimpleServiceLocator.logisticsManager.getCraftableItems(getRouter().getIRoutersByCost());
		return items;
	}

	@CCCommand(description = "Asks for the amount of an ItemIdentifier Id inside the Logistics Network")
	@CCQueued
	public int getItemAmount(ItemIdentifier item) throws Exception {
		Map<ItemIdentifier, Integer> items = SimpleServiceLocator.logisticsManager.getAvailableItems(getRouter().getIRoutersByCost());
		if (item == null) {
			throw new Exception("Invalid ItemIdentifierID");
		}
		if (items.containsKey(item)) {
			return items.get(item);
		}
		return 0;
	}
}
