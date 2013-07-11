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
import logisticspipes.network.oldpackets.PacketItems;
import logisticspipes.network.oldpackets.PacketRequestGuiContent;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.request.RequestTree.ActiveRequestType;
import logisticspipes.utils.ItemIdentifier;
import logisticspipes.utils.ItemIdentifierStack;
import logisticspipes.utils.ItemMessage;
import logisticspipes.utils.LiquidIdentifier;
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
//TODO Must be handled manualy
		MainProxy.sendPacketToPlayer(new PacketRequestGuiContent(_allItems).getPacket(), (Player)player);
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
//TODO Must be handled manualy
				MainProxy.sendPacketToPlayer(new PacketItems(items, false).getPacket(), (Player)player);
			}
			
			@Override
			public void handleSucessfullRequestOf(ItemMessage item) {
				//Not used here
			}
			
			@Override
			public void handleMissingItems(LinkedList<ItemMessage> list) {
//TODO Must be handled manualy
				MainProxy.sendPacketToPlayer(new PacketItems(list, true).getPacket(), (Player)player);
			}
		},RequestTree.defaultRequestFlags);
	}

	public static String computerRequest(final ItemIdentifierStack makeStack, final CoreRoutedPipe pipe, boolean craftingOnly) {

		EnumSet<ActiveRequestType> requestFlags;
		if(craftingOnly){
			requestFlags=EnumSet.of(ActiveRequestType.Craft);
		} else {
			requestFlags=EnumSet.of(ActiveRequestType.Craft,ActiveRequestType.Provide);			
		}
		if(!pipe.useEnergy(15)) {
			return "NO_POWER";
		}
		final String[] status = new String[1];
		RequestTree.request(makeStack, pipe, new RequestLog() {
			@Override
			public void handleSucessfullRequestOf(ItemMessage item) {
				status[0] = "DONE";
			}
			
			@Override
			public void handleMissingItems(LinkedList<ItemMessage> list) {
				status[0] = "MISSING";
			}

			@Override
			public void handleSucessfullRequestOfList(LinkedList<ItemMessage> items) {
				//Not needed here
			}
		},false, false,true,false,requestFlags);
		return status[0];
	}

	public static void refreshLiquid(EntityPlayer player, CoreRoutedPipe pipe) {
		TreeSet<ItemIdentifierStack> _allItems = SimpleServiceLocator.logisticsLiquidManager.getAvailableLiquid(pipe.getRouter().getIRoutersByCost());
//TODO Must be handled manualy
		MainProxy.sendPacketToPlayer(new PacketRequestGuiContent(_allItems).getPacket(), (Player)player);
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
