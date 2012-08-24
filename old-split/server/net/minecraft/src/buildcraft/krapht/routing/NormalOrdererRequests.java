package net.minecraft.src.buildcraft.krapht.routing;

import java.util.HashMap;
import java.util.LinkedList;

import net.minecraft.src.EntityPlayerMP;
import net.minecraft.src.core_LogisticsPipes;
import buildcraft.core.CoreProxy;
import net.minecraft.src.buildcraft.krapht.CoreRoutedPipe;
import net.minecraft.src.buildcraft.krapht.ItemMessage;
import net.minecraft.src.buildcraft.krapht.LogisticsManager;
import net.minecraft.src.buildcraft.krapht.LogisticsRequest;
import net.minecraft.src.buildcraft.krapht.network.PacketRequestGuiContent;
import net.minecraft.src.buildcraft.krapht.network.PacketRequestSubmit;
import net.minecraft.src.buildcraft.krapht.pipes.PipeItemsRequestLogistics;
import net.minecraft.src.buildcraft.logisticspipes.MessageManager;
import net.minecraft.src.krapht.ItemIdentifier;
import net.minecraft.src.krapht.ItemIdentifierStack;

public class NormalOrdererRequests {

	public enum DisplayOptions {
		Both,
		SupplyOnly,
		CraftOnly;
	}
	
	public static void request(EntityPlayerMP player, PacketRequestSubmit packet, CoreRoutedPipe pipe) {
		LogisticsRequest request = new LogisticsRequest(ItemIdentifier.get(packet.itemID, packet.dataValue, packet.tag), packet.amount, pipe);
		LinkedList<ItemMessage> errors = new LinkedList<ItemMessage>();
		boolean result = LogisticsManager.Request(request, pipe.getRouter().getRoutersByCost(), errors, player);
		if (!result){
			MessageManager.errors(player, errors);
		}
		else{
			LinkedList list = new LinkedList<ItemMessage>();
			list.add(new ItemMessage(packet.itemID, packet.dataValue, packet.amount, packet.tag));
			MessageManager.requested(player, list);
		}
	}
	
	public static void refresh(EntityPlayerMP player, CoreRoutedPipe pipe, DisplayOptions option) {
		HashMap<ItemIdentifier, Integer> _availableItems;
		LinkedList<ItemIdentifier> _craftableItems;
		LinkedList<ItemIdentifierStack>_allItems = new LinkedList<ItemIdentifierStack>(); 
		
		if (option == DisplayOptions.SupplyOnly || option == DisplayOptions.Both){
			_availableItems = core_LogisticsPipes.logisticsManager.getAvailableItems(pipe.getRouter().getRouteTable().keySet());
		} else {
			_availableItems = new HashMap<ItemIdentifier, Integer>();
		}
		if (option == DisplayOptions.CraftOnly || option == DisplayOptions.Both){
			_craftableItems = core_LogisticsPipes.logisticsManager.getCraftableItems(pipe.getRouter().getRouteTable().keySet());
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
		CoreProxy.sendToPlayer(player, new PacketRequestGuiContent(_allItems));
	}
}
