package logisticspipes.request;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;

import logisticspipes.interfaces.routing.IRequestLiquid;
import logisticspipes.logisticspipes.MessageManager;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.orderer.MissingItems;
import logisticspipes.network.packets.orderer.OrdererContent;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.request.RequestTree.ActiveRequestType;
import logisticspipes.utils.ItemIdentifier;
import logisticspipes.utils.ItemIdentifierStack;
import logisticspipes.utils.ItemMessage;
import logisticspipes.utils.LiquidIdentifier;
import logisticspipes.utils.Pair;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import cpw.mods.fml.common.network.Player;

public class RequestHandler {
	
	public enum DisplayOptions {
		Both,
		SupplyOnly,
		CraftOnly;
	}
	
	public static void request(final EntityPlayer player, final ItemIdentifierStack stack, CoreRoutedPipe pipe) {
		if(!pipe.useEnergy(5)) {
			player.sendChatToPlayer("No Energy");
			return;
		}
		RequestTree.request(ItemIdentifier.get(stack.getItem().itemID, stack.getItem().itemDamage, stack.getItem().tag).makeStack(stack.stackSize), pipe
				, new RequestLog() {
			@Override
			public void handleSucessfullRequestOf(ItemMessage item) {
				LinkedList<ItemMessage> list = new LinkedList<ItemMessage>();
				list.add(new ItemMessage(stack.getItem().itemID, stack.getItem().itemDamage, stack.stackSize, stack.getItem().tag));
				MessageManager.requested(player, list);
			}
			
			@Override
			public void handleMissingItems(LinkedList<ItemMessage> list) {
				MessageManager.errors(player, list);
			}

			@Override
			public void handleSucessfullRequestOfList(LinkedList<ItemMessage> items) {
				//Not needed here
			}
		});
	}
	
	public static void simulate(final EntityPlayer player, final ItemIdentifierStack stack, CoreRoutedPipe pipe) {
		final LinkedList<ItemMessage> used = new LinkedList<ItemMessage>();
		final LinkedList<ItemMessage> missing = new LinkedList<ItemMessage>();
		RequestTree.simulate(ItemIdentifier.get(stack.getItem().itemID, stack.getItem().itemDamage, stack.getItem().tag).makeStack(stack.stackSize), pipe, new RequestLog() {
			@Override
			public void handleSucessfullRequestOf(ItemMessage item) {
				//Not needed
			}
			
			@Override
			public void handleMissingItems(LinkedList<ItemMessage> list) {
				missing.addAll(list);
			}

			@Override
			public void handleSucessfullRequestOfList(LinkedList<ItemMessage> items) {
				used.addAll(items);
			}
		});
		MessageManager.simulated(player, used, missing);
	}
	
	public static void refresh(EntityPlayer player, CoreRoutedPipe pipe, DisplayOptions option) {
		Map<ItemIdentifier, Integer> _availableItems;
		LinkedList<ItemIdentifier> _craftableItems;
		
		if (option == DisplayOptions.SupplyOnly || option == DisplayOptions.Both){
			_availableItems = SimpleServiceLocator.logisticsManager.getAvailableItems(pipe.getRouter().getIRoutersByCost());
		} else {
			_availableItems = new HashMap<ItemIdentifier, Integer>();
		}
		if (option == DisplayOptions.CraftOnly || option == DisplayOptions.Both){
			_craftableItems = SimpleServiceLocator.logisticsManager.getCraftableItems(pipe.getRouter().getIRoutersByCost());
		} else {
			_craftableItems = new LinkedList<ItemIdentifier>();
		}
		TreeSet<ItemIdentifierStack>_allItems= new TreeSet<ItemIdentifierStack>();
		
		for (Entry<ItemIdentifier, Integer> item : _availableItems.entrySet()){
			ItemIdentifierStack newStack = item.getKey().makeStack(item.getValue());
			_allItems.add(newStack);
		}
		
		for (ItemIdentifier item : _craftableItems){
			if (_availableItems.containsKey(item)) continue;
			_allItems.add(item.makeStack(0));
		}
//TODO	MainProxy.sendPacketToPlayer(new PacketRequestGuiContent(NetworkConstants.ORDERER_CONTENT_ANSWER, _allItems).getPacket(), (Player)player);
		MainProxy.sendPacketToPlayer(PacketHandler.getPacket(OrdererContent.class).setIdentSet(_allItems), (Player)player);
	}
	

	public static void requestMacrolist(NBTTagCompound itemlist, CoreRoutedPipe requester, final EntityPlayer player) {
		if(!requester.useEnergy(5)) {
			player.sendChatToPlayer("No Energy");
			return;
		}
		NBTTagList list = itemlist.getTagList("inventar");
		LinkedList<ItemIdentifierStack> transaction = new LinkedList<ItemIdentifierStack>();
		List<ItemMessage> items = new ArrayList<ItemMessage>();
		for(int i = 0;i < list.tagCount();i++) {
			NBTTagCompound itemnbt = (NBTTagCompound) list.tagAt(i);
			NBTTagCompound itemNBTContent = itemnbt.getCompoundTag("nbt");
			if(!itemnbt.hasKey("nbt")) {
				itemNBTContent = null;
			}
			ItemIdentifierStack stack = ItemIdentifier.get(itemnbt.getInteger("id"),itemnbt.getInteger("data"),itemNBTContent).makeStack(itemnbt.getInteger("amount"));
			transaction.add(stack);
			items.add(new ItemMessage(stack));
		}
		RequestTree.request(transaction, requester, new RequestLog() {
			
			@Override
			public void handleSucessfullRequestOfList(LinkedList<ItemMessage> items) {
//TODO 			MainProxy.sendPacketToPlayer(new PacketItems(NetworkConstants.MISSING_ITEMS, items, false).getPacket(), (Player)player);
				MainProxy.sendPacketToPlayer(PacketHandler.getPacket(MissingItems.class).setItems(items).setFlag(false), (Player)player);
			}
			
			@Override
			public void handleSucessfullRequestOf(ItemMessage item) {
				//Not used here
			}
			
			@Override
			public void handleMissingItems(LinkedList<ItemMessage> list) {
//TODO 			MainProxy.sendPacketToPlayer(new PacketItems(NetworkConstants.MISSING_ITEMS, list, true).getPacket(), (Player)player);
				MainProxy.sendPacketToPlayer(PacketHandler.getPacket(MissingItems.class).setItems(list).setFlag(true), (Player)player);
			}
		},RequestTree.defaultRequestFlags);
	}

	public static List computerRequest(final ItemIdentifierStack makeStack, final CoreRoutedPipe pipe, boolean craftingOnly) {

		final List status = new LinkedList();
		
		EnumSet<ActiveRequestType> requestFlags;
		if(craftingOnly){
			requestFlags=EnumSet.of(ActiveRequestType.Craft);
		} else {
			requestFlags=EnumSet.of(ActiveRequestType.Craft,ActiveRequestType.Provide);			
		}
		if(!pipe.useEnergy(15)) {
			status.add("NO_POWER");
			return status;
		}
		RequestTree.request(makeStack, pipe, new RequestLog() {
			@Override
			public void handleSucessfullRequestOf(ItemMessage item) {
				status.add("DONE");
				
				List<Pair<ItemIdentifier, Integer>> itemList = new LinkedList<Pair<ItemIdentifier, Integer>>();
				
				itemList.add(new Pair<ItemIdentifier,Integer>(item.getItemIdentifier(), item.amount));
				
				status.add(itemList);
			}
			
			@Override
			public void handleMissingItems(LinkedList<ItemMessage> list) {
				status.add("MISSING");
				
				List<Pair<ItemIdentifier, Integer>> itemList = new LinkedList<Pair<ItemIdentifier, Integer>>();
				
				for(ItemMessage item : list) {
					itemList.add(new Pair<ItemIdentifier,Integer>(item.getItemIdentifier(), item.amount));
				}
				
				status.add(itemList);
			}

			@Override
			public void handleSucessfullRequestOfList(LinkedList<ItemMessage> items) {
				//Not needed here
			}
		},false, false,true,false,requestFlags);
		
		return status;
	}

	public static void refreshLiquid(EntityPlayer player, CoreRoutedPipe pipe) {
		TreeSet<ItemIdentifierStack> _allItems = SimpleServiceLocator.logisticsLiquidManager.getAvailableLiquid(pipe.getRouter().getIRoutersByCost());
//TODO 	MainProxy.sendPacketToPlayer(new PacketRequestGuiContent(NetworkConstants.ORDERER_CONTENT_ANSWER, _allItems).getPacket(), (Player)player);
		MainProxy.sendPacketToPlayer(PacketHandler.getPacket(OrdererContent.class).setIdentSet(_allItems), (Player)player);
	}

	public static void requestLiquid(final EntityPlayer player, final ItemIdentifierStack stack, CoreRoutedPipe pipe, IRequestLiquid requester) {
		if(!pipe.useEnergy(10)) {
			player.sendChatToPlayer("No Energy");
			return;
		}
		
		RequestTree.requestLiquid(LiquidIdentifier.get(stack.getItem().itemID, stack.getItem().itemDamage) , stack.stackSize, requester, new RequestLog() {
			@Override
			public void handleSucessfullRequestOf(ItemMessage item) {
				LinkedList<ItemMessage> list = new LinkedList<ItemMessage>();
				list.add(new ItemMessage(stack.getItem().itemID, stack.getItem().itemDamage, stack.stackSize, stack.getItem().tag));
				MessageManager.requested(player, list);
			}
			
			@Override
			public void handleMissingItems(LinkedList<ItemMessage> list) {
				MessageManager.errors(player, list);
			}

			@Override
			public void handleSucessfullRequestOfList(LinkedList<ItemMessage> items) {
				//Not needed here
			}
		});
	}
}
