package logisticspipes.proxy.ae;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;

import logisticspipes.interfaces.routing.ICraftItems;
import logisticspipes.interfaces.routing.IRelayItem;
import logisticspipes.interfaces.routing.IRequestItems;
import logisticspipes.request.CraftingTemplate;
import logisticspipes.request.RequestTree;
import logisticspipes.request.RequestTreeNode;
import logisticspipes.routing.LogisticsPromise;
import logisticspipes.utils.ItemIdentifier;
import logisticspipes.utils.ItemIdentifierStack;
import logisticspipes.utils.Pair;

import appeng.api.me.util.ICraftRequest;
import appeng.api.me.util.InterfaceCraftingResponse;
import appeng.api.me.tiles.ITileInterfaceApi;

public class AECraftingTemplate extends CraftingTemplate {
	
	ITileInterfaceApi _interface;
	public AECraftingTemplate(ITileInterfaceApi _interface, ICraftItems crafter,
			int priority) {
		super(null, crafter, priority);
		this._interface = _interface;
	}
	
	@Override
	public int getResultStackSize() {
		return 1;
	}
	
	@Override
	public void addRequirement(ItemIdentifierStack stack, IRequestItems crafter) {
	
	}
	
	@Override
	public boolean canCraft(ItemIdentifier item) {
		List<ItemStack> results = _interface.getCraftingOptions();
		item.getId();
		
		for(ItemStack r:results){
			if(ItemIdentifier.get(r).equals(item)) {
				this._result = item.makeStack(1);
				return true;
			}
		}
		return false;
	}
	
	@Override 
	public LogisticsPromise generatePromise(int nResultSets, List<IRelayItem> relays) {
		InterfaceCraftingResponse response = _interface.requestCrafting(_result.unsafeMakeNormalStack(), true);
		LogisticsPromise promise = new LogisticsPromise();
		promise.item = ItemIdentifier.get(response.Request);
		promise.numberOfItems = response.Request.stackSize;
		promise.sender = _crafter;
		promise.relayPoints = relays;
		return promise;
	}
/*	
	@Override
	public int getSubRequests(int nCraftingSetsNeeded, RequestTree root, RequestTreeNode currentNode){
		InterfaceCraftingRequest response = _interface.requestCrafting(_result.unsafeMakeNormalStack(), true);
		return response.Request.stackSize;
	}
	
	@Override
	protected int generateRequestTreeFor(int workSetsAvailable, RequestTree root, RequestTreeNode currentNode) {		
		InterfaceCraftingRequest response = _interface.requestCrafting(_result.unsafeMakeNormalStack(), true);
		return response.Request.stackSize;
	}*/
}
