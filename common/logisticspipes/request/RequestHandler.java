package logisticspipes.request;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;

import logisticspipes.interfaces.routing.IRequestFluid;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.orderer.ComponentList;
import logisticspipes.network.packets.orderer.MissingItems;
import logisticspipes.network.packets.orderer.MissingItems.ProcessedItem;
import logisticspipes.network.packets.orderer.OrdererContent;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.request.RequestTree.ActiveRequestType;
import logisticspipes.utils.FluidIdentifier;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.item.ItemIdentifierStack;
import logisticspipes.utils.tuples.Pair;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ChatMessageComponent;
import cpw.mods.fml.common.network.Player;

public class RequestHandler {
	
	public enum DisplayOptions {
		Both,
		SupplyOnly,
		CraftOnly;
	}
	
	public static void request(final EntityPlayer player, final ItemIdentifierStack stack, CoreRoutedPipe pipe) {
		request(player, new ItemIdentifierStack[] { stack }, pipe);
	}
	
	public static void request(final EntityPlayer player, final ItemIdentifierStack[] stacks, CoreRoutedPipe pipe) {
		if(!pipe.useEnergy(5 * stacks.length)) {
			player.sendChatToPlayer(ChatMessageComponent.createFromText("No Energy"));
			return;
		}
		
		final List<ProcessedItem> coll = new ArrayList<ProcessedItem>(stacks.length);
		
		RequestLog log = new RequestLog() {
			@Override
			public void handleMissingItems(Map<ItemIdentifier,Integer> items) {
				for(Entry<ItemIdentifier,Integer> e : items.entrySet()) {
					boolean found = false;
					
					for (ProcessedItem p : coll){
						if (p.getStack().getItem() == e.getKey()){
							p.setStack(new ItemIdentifierStack(e.getKey(), e.getValue() + p.getStack().getStackSize()));
							found = true;
						}
					}
					
					if (!found){
						coll.add(new ProcessedItem(new ItemIdentifierStack(e.getKey(), e.getValue()), false));
					}
				}
			}

			@Override
			public void handleSucessfullRequestOf(ItemIdentifier item, int count) {
				coll.add(new ProcessedItem(new ItemIdentifierStack(item, count), true));
			}
			
			@Override
			public void handleSucessfullRequestOfList(Map<ItemIdentifier,Integer> items) {
				for (Entry<ItemIdentifier,Integer> e : items.entrySet()) {
					coll.add(new ProcessedItem(new ItemIdentifierStack(e.getKey(), e.getValue()), true));
				}
			}
		};
		
		for (ItemIdentifierStack stack : stacks){
			RequestTree.request(stack, pipe, log);
		}
		
		MainProxy.sendPacketToPlayer(PacketHandler.getPacket(MissingItems.class).setItems(coll.toArray(new ProcessedItem[coll.size()])), (Player)player);
	}
	
	public static void simulate(final EntityPlayer player, final ItemIdentifierStack stack, CoreRoutedPipe pipe) {
		simulate(player, new ItemIdentifierStack[] { stack }, pipe);
	}
	
	public static void simulate(final EntityPlayer player, final ItemIdentifierStack[] stacks, CoreRoutedPipe pipe) {
		final List<ItemIdentifierStack> used = new ArrayList<ItemIdentifierStack>();
		final List<ItemIdentifierStack> missing = new ArrayList<ItemIdentifierStack>();
		
		RequestLog log = new RequestLog() {
			@Override
			public void handleMissingItems(Map<ItemIdentifier,Integer> items) {
				for (Entry<ItemIdentifier,Integer> e : items.entrySet()) {
					//iterator is used to be able to remove items
					Iterator<ItemIdentifierStack> iter = missing.iterator();
					
					int count = e.getValue();
					
					while (iter.hasNext()){
						ItemIdentifierStack stack = iter.next();
						
						if (stack.getItem() == e.getKey()){
							count += stack.getStackSize();
							iter.remove();
							break;
						}
					}
					
					missing.add(new ItemIdentifierStack(e.getKey(), count));
				}
			}

			@Override
			public void handleSucessfullRequestOf(ItemIdentifier item, int count) {}
			
			@Override
			public void handleSucessfullRequestOfList(Map<ItemIdentifier,Integer> items) {
				for (Entry<ItemIdentifier,Integer> e : items.entrySet()) {
					//iterator is used to be able to remove items
					Iterator<ItemIdentifierStack> iter = used.iterator();
					
					int count = e.getValue();
					
					while (iter.hasNext()){
						ItemIdentifierStack stack = iter.next();
						
						if (stack.getItem() == e.getKey()){
							count += stack.getStackSize();
							iter.remove();
							break;
						}
					}
					
					used.add(new ItemIdentifierStack(e.getKey(), count));
				}
			}
		};
		
		for (ItemIdentifierStack stack : stacks){
			RequestTree.simulate(ItemIdentifier.get(stack.getItem().itemID, stack.getItem().itemDamage, stack.getItem().tag).makeStack(stack.getStackSize()), pipe, log);
		}
		
		MainProxy.sendPacketToPlayer(PacketHandler.getPacket(ComponentList.class).setUsed(used).setMissing(missing), (Player)player);
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
		MainProxy.sendPacketToPlayer(PacketHandler.getPacket(OrdererContent.class).setIdentSet(_allItems), (Player)player);
	}
	

	public static void requestList(final EntityPlayer player, final List<ItemIdentifierStack> list, CoreRoutedPipe pipe) {
		request(player, list.toArray(new ItemIdentifierStack[list.size()]), pipe);
	}

	public static void requestMacrolist(NBTTagCompound itemlist, CoreRoutedPipe requester, final EntityPlayer player) {
		requestMacrolist(itemlist, new CoreRoutedPipe[] { requester} , player);
	}
	
	public static void requestMacrolist(NBTTagCompound itemlist, CoreRoutedPipe[] requesters, final EntityPlayer player) {
		for (CoreRoutedPipe requester : requesters){
			if(!requester.useEnergy(5)) {
				player.sendChatToPlayer(ChatMessageComponent.createFromText("No Energy"));
				return;
			}
		}
		
		NBTTagList list = itemlist.getTagList("inventar");
		List<ItemIdentifierStack> transaction = new ArrayList<ItemIdentifierStack>(list.tagCount());
		for(int i = 0;i < list.tagCount();i++) {
			NBTTagCompound itemnbt = (NBTTagCompound) list.tagAt(i);
			NBTTagCompound itemNBTContent = itemnbt.getCompoundTag("nbt");
			if(!itemnbt.hasKey("nbt")) {
				itemNBTContent = null;
			}
			ItemIdentifierStack stack = ItemIdentifier.get(itemnbt.getInteger("id"),itemnbt.getInteger("data"),itemNBTContent).makeStack(itemnbt.getInteger("amount"));
			transaction.add(stack);
		}
		
		final List<ProcessedItem> coll = new ArrayList<ProcessedItem>(requesters.length);
		
		RequestLog log = new RequestLog() {
			@Override
			public void handleMissingItems(Map<ItemIdentifier,Integer> items) {
				for(Entry<ItemIdentifier,Integer> e : items.entrySet()) {
					boolean found = false;
					
					for (ProcessedItem p : coll){
						if (p.getStack().getItem() == e.getKey()){
							p.setStack(new ItemIdentifierStack(e.getKey(), e.getValue() + p.getStack().getStackSize()));
							found = true;
						}
					}
					
					if (!found){
						coll.add(new ProcessedItem(new ItemIdentifierStack(e.getKey(), e.getValue()), false));
					}
				}
			}

			@Override
			public void handleSucessfullRequestOf(ItemIdentifier item, int count) {
				coll.add(new ProcessedItem(new ItemIdentifierStack(item, count), true));
			}
			
			@Override
			public void handleSucessfullRequestOfList(Map<ItemIdentifier,Integer> items) {
				for (Entry<ItemIdentifier,Integer> e : items.entrySet()) {
					coll.add(new ProcessedItem(new ItemIdentifierStack(e.getKey(), e.getValue()), true));
				}
			}
		};
		
		for (CoreRoutedPipe requester : requesters){
			RequestTree.request(transaction, requester, log,RequestTree.defaultRequestFlags);
		}
		
		MainProxy.sendPacketToPlayer(PacketHandler.getPacket(MissingItems.class).setItems(coll.toArray(new ProcessedItem[coll.size()])), (Player)player);
	}

	public static Object[] computerRequest(final ItemIdentifierStack makeStack, final CoreRoutedPipe pipe, boolean craftingOnly) {

		EnumSet<ActiveRequestType> requestFlags;
		if(craftingOnly){
			requestFlags=EnumSet.of(ActiveRequestType.Craft);
		} else {
			requestFlags=EnumSet.of(ActiveRequestType.Craft,ActiveRequestType.Provide);			
		}
		if(!pipe.useEnergy(15)) {
			return new Object[]{"NO_POWER"};
		}
		final Object[] status = new Object[2];
		RequestTree.request(makeStack, pipe, new RequestLog() {
			@Override
			public void handleMissingItems(Map<ItemIdentifier,Integer> items) {
				status[0] = "MISSING";
				List<Pair<ItemIdentifier, Integer>> itemList = new LinkedList<Pair<ItemIdentifier, Integer>>();
				for(Entry<ItemIdentifier, Integer> item : items.entrySet()) {
					itemList.add(new Pair<ItemIdentifier,Integer>(item.getKey(), item.getValue()));
			}
				status[1] = itemList;
			}

			@Override
			public void handleSucessfullRequestOf(ItemIdentifier item, int count) {
				status[0] = "DONE";
				List<Pair<ItemIdentifier, Integer>> itemList = new LinkedList<Pair<ItemIdentifier, Integer>>();
				itemList.add(new Pair<ItemIdentifier,Integer>(item, count));
				status[1] = itemList;
			}
			
			@Override
			public void handleSucessfullRequestOfList(Map<ItemIdentifier,Integer> items) {}
		},false, false,true,false,requestFlags);
		return status;
	}

	public static void refreshFluid(EntityPlayer player, CoreRoutedPipe pipe) {
		TreeSet<ItemIdentifierStack> _allItems = SimpleServiceLocator.logisticsFluidManager.getAvailableFluid(pipe.getRouter().getIRoutersByCost());
		MainProxy.sendPacketToPlayer(PacketHandler.getPacket(OrdererContent.class).setIdentSet(_allItems), (Player)player);
	}

	public static void requestFluid(final EntityPlayer player, final ItemIdentifierStack[] stacks, CoreRoutedPipe pipe, IRequestFluid requester) {
		if(!pipe.useEnergy(10)) {
			player.sendChatToPlayer(ChatMessageComponent.createFromText("No Energy"));
			return;
		}
		
		final List<ProcessedItem> coll = new ArrayList<ProcessedItem>(stacks.length);
		
		RequestLog log = new RequestLog() {
			@Override
			public void handleMissingItems(Map<ItemIdentifier,Integer> items) {
				for(Entry<ItemIdentifier,Integer> e : items.entrySet()) {
					boolean found = false;
					
					for (ProcessedItem p : coll){
						if (p.getStack().getItem() == e.getKey()){
							p.setStack(new ItemIdentifierStack(e.getKey(), e.getValue() + p.getStack().getStackSize()));
							found = true;
						}
					}
					
					if (!found){
						coll.add(new ProcessedItem(new ItemIdentifierStack(e.getKey(), e.getValue()), false));
					}
				}
			}

			@Override
			public void handleSucessfullRequestOf(ItemIdentifier item, int count) {
				coll.add(new ProcessedItem(new ItemIdentifierStack(item, count), true));
			}
			
			@Override
			public void handleSucessfullRequestOfList(Map<ItemIdentifier,Integer> items) {
				for (Entry<ItemIdentifier,Integer> e : items.entrySet()) {
					coll.add(new ProcessedItem(new ItemIdentifierStack(e.getKey(), e.getValue()), true));
				}
			}
		};
		
		for (ItemIdentifierStack stack : stacks){
			RequestTree.requestFluid(FluidIdentifier.get(stack.getItem()) , stack.getStackSize(), requester, log);
		}
		
		MainProxy.sendPacketToPlayer(PacketHandler.getPacket(MissingItems.class).setItems(coll.toArray(new ProcessedItem[coll.size()])), (Player)player);
	}
}
