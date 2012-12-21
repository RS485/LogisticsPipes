package logisticspipes.request;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

import logisticspipes.interfaces.routing.IRequestItems;
import logisticspipes.logisticspipes.MessageManager;
import logisticspipes.network.packets.PacketItems;
import logisticspipes.network.packets.PacketRequestGuiContent;
import logisticspipes.network.packets.PacketRequestSubmit;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.ticks.QueuedTasks;
import logisticspipes.utils.ItemIdentifier;
import logisticspipes.utils.ItemIdentifierStack;
import logisticspipes.utils.ItemMessage;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import cpw.mods.fml.common.network.Player;

public class RequestHandler {
	
	private static int request_id = 0;
	
	public enum DisplayOptions {
		Both,
		SupplyOnly,
		CraftOnly;
	}
	
	public static void request(final EntityPlayerMP player, final PacketRequestSubmit packet, CoreRoutedPipe pipe) {
		//LogisticsRequest request = new LogisticsRequest(ItemIdentifier.get(packet.itemID, packet.dataValue, packet.tag), packet.amount, pipe, true);
		LinkedList<ItemMessage> errors = new LinkedList<ItemMessage>();
		if(!pipe.useEnergy(5)) {
			player.sendChatToPlayer("No Energy");
			return;
		}
		boolean result = RequestManager.request(ItemIdentifier.get(packet.itemID, packet.dataValue, packet.tag).makeStack(packet.amount), pipe, pipe.getRouter().getIRoutersByCost(), new RequestLog() {
			@Override
			public void handleSucessfullRequestOf(ItemMessage item) {
				LinkedList list = new LinkedList<ItemMessage>();
				list.add(new ItemMessage(packet.itemID, packet.dataValue, packet.amount, packet.tag));
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
	
	public static void refresh(EntityPlayerMP player, CoreRoutedPipe pipe, DisplayOptions option) {
		HashMap<ItemIdentifier, Integer> _availableItems;
		LinkedList<ItemIdentifier> _craftableItems;
		LinkedList<ItemIdentifierStack>_allItems = new LinkedList<ItemIdentifierStack>(); 
		
		if (option == DisplayOptions.SupplyOnly || option == DisplayOptions.Both){
			_availableItems = SimpleServiceLocator.logisticsManager.getAvailableItems(pipe.getRouter().getRouteTable().keySet());
		} else {
			_availableItems = new HashMap<ItemIdentifier, Integer>();
		}
		if (option == DisplayOptions.CraftOnly || option == DisplayOptions.Both){
			_craftableItems = SimpleServiceLocator.logisticsManager.getCraftableItems(pipe.getRouter().getRouteTable().keySet());
		} else {
			_craftableItems = new LinkedList<ItemIdentifier>();
		}
		_allItems.clear();
		
		outer:
		for (ItemIdentifier item : _availableItems.keySet()){
			for (int i = 0; i <_allItems.size(); i++){
				if (item.itemID < _allItems.get(i).getItem().itemID || item.itemID == _allItems.get(i).getItem().itemID && item.itemDamage < _allItems.get(i).getItem().itemDamage){
					_allItems.add(i, item.makeStack(_availableItems.get(item)));
					continue outer;
				}
			}
			_allItems.addLast(item.makeStack(_availableItems.get(item)));
		}
		
		outer:
		for (ItemIdentifier item : _craftableItems){
			if (_availableItems.containsKey(item)) continue;
			for (int i = 0; i <_allItems.size(); i++){
				if (item.itemID < _allItems.get(i).getItem().itemID || item.itemID == _allItems.get(i).getItem().itemID && item.itemDamage < _allItems.get(i).getItem().itemDamage){
					_allItems.add(i, item.makeStack(0));
					continue outer;
				}
			}
			_allItems.addLast(item.makeStack(0));
		}
		MainProxy.sendPacketToPlayer(new PacketRequestGuiContent(_allItems).getPacket(), (Player)player);
	}
	

	public static void requestMacrolist(NBTTagCompound itemlist, IRequestItems requester, final EntityPlayer player) {
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
		RequestManager.request(transaction, requester, requester.getRouter().getIRoutersByCost(), new RequestLog() {
			
			@Override
			public void handleSucessfullRequestOfList(LinkedList<ItemMessage> items) {
				MainProxy.sendPacketToPlayer(new PacketItems(items, false).getPacket(), (Player)player);
			}
			
			@Override
			public void handleSucessfullRequestOf(ItemMessage item) {
				//Not used here
			}
			
			@Override
			public void handleMissingItems(LinkedList<ItemMessage> list) {
				MainProxy.sendPacketToPlayer(new PacketItems(list, true).getPacket(), (Player)player);
			}
		});
	}

	public static int computerRequest(final ItemIdentifierStack makeStack, final CoreRoutedPipe pipe) {
		LinkedList<ItemMessage> errors = new LinkedList<ItemMessage>();
		if(!pipe.useEnergy(15)) {
			return -1;
		}
		request_id++;
		QueuedTasks.queueTask(new Callable() {
			@Override
			public Object call() throws Exception {
				RequestManager.request(makeStack, pipe, pipe.getRouter().getIRoutersByCost(), new RequestLog() {
					@Override
					public void handleSucessfullRequestOf(ItemMessage item) {
						pipe.queueEvent("request_successfull", new Object[]{request_id});
					}
					
					@Override
					public void handleMissingItems(LinkedList<ItemMessage> list) {
						pipe.queueEvent("request_failed", new Object[]{request_id});
					}

					@Override
					public void handleSucessfullRequestOfList(LinkedList<ItemMessage> items) {
						//Not needed here
					}
				});
				return null;
			}
		});
		return request_id;
	}
}
