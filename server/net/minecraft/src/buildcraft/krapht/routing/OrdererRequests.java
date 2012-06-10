package net.minecraft.src.buildcraft.krapht.routing;

import java.util.HashMap;
import java.util.LinkedList;

import net.minecraft.src.EntityPlayerMP;
import net.minecraft.src.core_LogisticsPipes;
import net.minecraft.src.buildcraft.core.CoreProxy;
import net.minecraft.src.buildcraft.krapht.CoreRoutedPipe;
import net.minecraft.src.buildcraft.krapht.ErrorMessage;
import net.minecraft.src.buildcraft.krapht.LogisticsManager;
import net.minecraft.src.buildcraft.krapht.LogisticsRequest;
import net.minecraft.src.buildcraft.krapht.network.PacketRequestGuiContent;
import net.minecraft.src.buildcraft.krapht.network.PacketRequestSubmit;
import net.minecraft.src.buildcraft.krapht.pipes.PipeItemsRequestLogistics;
import net.minecraft.src.buildcraft.logisticspipes.MessageManager;
import net.minecraft.src.krapht.ItemIdentifier;

public class OrdererRequests {

	public enum DisplayOptions {
		Both,
		SupplyOnly,
		CraftOnly;
	}
	
	public static void request(EntityPlayerMP player, PacketRequestSubmit packet, PipeItemsRequestLogistics pipe) {
		LogisticsRequest request = new LogisticsRequest(ItemIdentifier.get(packet.itemID,packet.dataValue), packet.amount, pipe);
		LinkedList<ErrorMessage> errors = new LinkedList<ErrorMessage>();
		boolean result = LogisticsManager.Request(request, pipe.getRouter().getRoutersByCost(), errors, player);
		if (!result){
			MessageManager.errors(player, errors);
		}
		else{
			player.addChatMessage("Request successful!");
		}
	}
	
	public static void refresh(EntityPlayerMP player, CoreRoutedPipe pipe, DisplayOptions option) {
		HashMap<ItemIdentifier, Integer> _availableItems;
		LinkedList<ItemIdentifier> _craftableItems;
		LinkedList<ItemIdentifier>_allItems = new LinkedList<ItemIdentifier>(); 
		
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
				if (item.itemID < _allItems.get(i).itemID || item.itemID == _allItems.get(i).itemID && item.itemDamage < _allItems.get(i).itemDamage){
					_allItems.add(i, item);
					continue outer;
				}
			}
			_allItems.addLast(item);
		}
		
		outer:
		for (ItemIdentifier item : _craftableItems){
			if (_allItems.contains(item)) continue;
			for (int i = 0; i <_allItems.size(); i++){
				if (item.itemID < _allItems.get(i).itemID || item.itemID == _allItems.get(i).itemID && item.itemDamage < _allItems.get(i).itemDamage){
					_allItems.add(i, item);
					continue outer;
				}
			}
			_allItems.addLast(item);
		}
		CoreProxy.sendToPlayer(player, new PacketRequestGuiContent(_availableItems, _craftableItems, _allItems));
	}
}
