package logisticspipes.request;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;

import logisticspipes.interfaces.routing.IRequestFluid;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.orderer.ComponentList;
import logisticspipes.network.packets.orderer.MissingItems;
import logisticspipes.network.packets.orderer.OrdererContent;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.request.RequestTree.ActiveRequestType;
import logisticspipes.utils.ItemIdentifier;
import logisticspipes.utils.ItemIdentifierStack;
import logisticspipes.utils.FluidIdentifier;
import logisticspipes.utils.Pair;
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
		if(!pipe.useEnergy(5)) {
			player.sendChatToPlayer(ChatMessageComponent.func_111066_d("No Energy"));
			return;
		}
		RequestTree.request(ItemIdentifier.get(stack.getItem().itemID, stack.getItem().itemDamage, stack.getItem().tag).makeStack(stack.stackSize), pipe
				, new RequestLog() {
			@Override
			public void handleMissingItems(Map<ItemIdentifier,Integer> items) {
				Collection<ItemIdentifierStack> coll = new ArrayList<ItemIdentifierStack>(items.size());
				for(Entry<ItemIdentifier,Integer>e:items.entrySet()) {
					coll.add(new ItemIdentifierStack(e.getKey(), e.getValue()));
				}
				MainProxy.sendPacketToPlayer(PacketHandler.getPacket(MissingItems.class).setItems(coll).setFlag(true), (Player)player);
			}

			@Override
			public void handleSucessfullRequestOf(ItemIdentifier item, int count) {
				Collection<ItemIdentifierStack> coll = new ArrayList<ItemIdentifierStack>(1);
				coll.add(new ItemIdentifierStack(item, count));
				MainProxy.sendPacketToPlayer(PacketHandler.getPacket(MissingItems.class).setItems(coll).setFlag(false), (Player)player);
			}
			
			@Override
			public void handleSucessfullRequestOfList(Map<ItemIdentifier,Integer> items) {}
		});
	}
	
	public static void simulate(final EntityPlayer player, final ItemIdentifierStack stack, CoreRoutedPipe pipe) {
		final Map<ItemIdentifier,Integer> used = new HashMap<ItemIdentifier,Integer>();
		final Map<ItemIdentifier,Integer> missing = new HashMap<ItemIdentifier,Integer>();
		RequestTree.simulate(ItemIdentifier.get(stack.getItem().itemID, stack.getItem().itemDamage, stack.getItem().tag).makeStack(stack.stackSize), pipe, new RequestLog() {
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
		List<ItemIdentifierStack> usedList = new ArrayList<ItemIdentifierStack>(used.size());
		List<ItemIdentifierStack> missingList = new ArrayList<ItemIdentifierStack>(missing.size());
		for(Entry<ItemIdentifier,Integer>e:used.entrySet()) {
			usedList.add(new ItemIdentifierStack(e.getKey(), e.getValue()));
		}
		for(Entry<ItemIdentifier,Integer>e:missing.entrySet()) {
			missingList.add(new ItemIdentifierStack(e.getKey(), e.getValue()));
		}
		MainProxy.sendPacketToPlayer(PacketHandler.getPacket(ComponentList.class).setUsed(usedList).setMissing(missingList), (Player)player);
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
			player.sendChatToPlayer(ChatMessageComponent.func_111066_d("No Energy"));
			return;
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
		RequestTree.request(transaction, requester, new RequestLog() {
			@Override
			public void handleMissingItems(Map<ItemIdentifier,Integer> items) {
				Collection<ItemIdentifierStack> coll = new ArrayList<ItemIdentifierStack>(items.size());
				for(Entry<ItemIdentifier,Integer>e:items.entrySet()) {
					coll.add(new ItemIdentifierStack(e.getKey(), e.getValue()));
				}
				MainProxy.sendPacketToPlayer(PacketHandler.getPacket(MissingItems.class).setItems(coll).setFlag(true), (Player)player);
			}
			
			@Override
			public void handleSucessfullRequestOf(ItemIdentifier item, int count) {}
			
			@Override
			public void handleSucessfullRequestOfList(Map<ItemIdentifier,Integer> items) {
				Collection<ItemIdentifierStack> coll = new ArrayList<ItemIdentifierStack>(items.size());
				for(Entry<ItemIdentifier,Integer>e:items.entrySet()) {
					coll.add(new ItemIdentifierStack(e.getKey(), e.getValue()));
				}
				MainProxy.sendPacketToPlayer(PacketHandler.getPacket(MissingItems.class).setItems(coll).setFlag(false), (Player)player);
			}
		},RequestTree.defaultRequestFlags);
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
//TODO 	MainProxy.sendPacketToPlayer(new PacketRequestGuiContent(NetworkConstants.ORDERER_CONTENT_ANSWER, _allItems).getPacket(), (Player)player);
		MainProxy.sendPacketToPlayer(PacketHandler.getPacket(OrdererContent.class).setIdentSet(_allItems), (Player)player);
	}

	public static void requestFluid(final EntityPlayer player, final ItemIdentifierStack stack, CoreRoutedPipe pipe, IRequestFluid requester) {
		if(!pipe.useEnergy(10)) {
			player.sendChatToPlayer(ChatMessageComponent.func_111066_d("No Energy"));
			return;
		}
		
		RequestTree.requestFluid(FluidIdentifier.get(stack.getItem().itemID, stack.getItem().itemDamage) , stack.stackSize, requester, new RequestLog() {
			@Override
			public void handleMissingItems(Map<ItemIdentifier,Integer> items) {
				Collection<ItemIdentifierStack> coll = new ArrayList<ItemIdentifierStack>(items.size());
				for(Entry<ItemIdentifier,Integer>e:items.entrySet()) {
					coll.add(new ItemIdentifierStack(e.getKey(), e.getValue()));
				}
				MainProxy.sendPacketToPlayer(PacketHandler.getPacket(MissingItems.class).setItems(coll).setFlag(true), (Player)player);
			}

			@Override
			public void handleSucessfullRequestOf(ItemIdentifier item, int count) {
				Collection<ItemIdentifierStack> coll = new ArrayList<ItemIdentifierStack>(1);
				coll.add(new ItemIdentifierStack(item, count));
				MainProxy.sendPacketToPlayer(PacketHandler.getPacket(MissingItems.class).setItems(coll).setFlag(false), (Player)player);
			}
			
			@Override
			public void handleSucessfullRequestOfList(Map<ItemIdentifier,Integer> items) {}
		});
	}
}
