package logisticspipes.pipes;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import logisticspipes.LogisticsPipes;
import logisticspipes.api.IRoutedPowerProvider;
import logisticspipes.interfaces.routing.IRequestItems;
import logisticspipes.interfaces.routing.IRequireReliableTransport;
import logisticspipes.logisticspipes.IRoutedItem;
import logisticspipes.modules.LogisticsModule;
import logisticspipes.network.GuiIDs;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.request.RequestTree;
import logisticspipes.textures.Textures;
import logisticspipes.textures.Textures.TextureType;
import logisticspipes.transport.PipeTransportLogistics;
import logisticspipes.utils.AdjacentTile;
import logisticspipes.utils.FluidIdentifier;
import logisticspipes.utils.ItemIdentifier;
import logisticspipes.utils.ItemIdentifierStack;
import logisticspipes.utils.SimpleInventory;
import logisticspipes.utils.WorldUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidContainerRegistry;
import buildcraft.transport.TravelingItem;
import buildcraft.transport.IItemTravelingHook;
import buildcraft.transport.PipeTransportItems;
import buildcraft.transport.TileGenericPipe;

public class PipeItemsFluidSupplier extends CoreRoutedPipe implements IRequestItems, IRequireReliableTransport, IItemTravelingHook{

	private boolean _lastRequestFailed = false;
	
	public PipeItemsFluidSupplier(int itemID) {
		super(new PipeTransportLogistics() {

			@Override
			public boolean canPipeConnect(TileEntity tile, ForgeDirection dir) {
				if(super.canPipeConnect(tile, dir)) return true;
				if(tile instanceof TileGenericPipe) return false;
				if (tile instanceof IFluidTank) {
					IFluidTank liq = (IFluidTank) tile;
					//TODO: check this change
					//					if (liq.getTanks(ForgeDirection.UNKNOWN) != null && liq.getTanks(ForgeDirection.UNKNOWN).length > 0)
 					if (liq.getCapacity() > 0)
						return true;
				}
				return false;
			}
		}, itemID);
		((PipeTransportItems) transport).travelHook = this;

		throttleTime = 100;
	}

	@Override
	public TextureType getCenterTexture() {
		return Textures.LOGISTICSPIPE_LIQUIDSUPPLIER_TEXTURE;
	}

	/* TRIGGER INTERFACE */
	public boolean isRequestFailed(){
		return _lastRequestFailed;
	}

	public void setRequestFailed(boolean value){
		_lastRequestFailed = value;
	}

	@Override
	public LogisticsModule getLogisticsModule() {
		return null;
	}

	@Override
	public ItemSendMode getItemSendMode() {
		return ItemSendMode.Fast;
	}


	/* IItemTravelingHook */

	@Override
	public boolean endReached(PipeTransportItems pipe, TravelingItem data, TileEntity tile) {
		//((PipeTransportLogistics)pipe).markChunkModified(tile);
		if (!(tile instanceof IFluidHandler)) return false;
		if (tile instanceof TileGenericPipe) return false;
		IFluidHandler container = (IFluidHandler) tile;
		//container.getFluidSlots()[0].getFluidQty();
		if (data == null) return false;
		if (data.getItemStack() == null) return false ;
		FluidStack liquidId = FluidContainerRegistry.getFluidForFilledItem(data.getItemStack());
		if (liquidId == null) return false;
		ForgeDirection orientation = data.output.getOpposite();
		if(getUpgradeManager().hasSneakyUpgrade()) {
			orientation = getUpgradeManager().getSneakyOrientation();
		}
		while (data.getItemStack().stackSize > 0 && container.fill(orientation, liquidId, false) == liquidId.amount && this.useEnergy(5)) {
			container.fill(orientation, liquidId, true);
			data.getItemStack().stackSize--;
			if (data.getItemStack().itemID >= 0 && data.getItemStack().itemID < Item.itemsList.length){
				Item item = Item.itemsList[data.getItemStack().itemID];
				if (item.hasContainerItem()){
					Item containerItem = item.getContainerItem();
					IRoutedItem itemToSend = SimpleServiceLocator.buildCraftProxy.CreateRoutedItem(this.container, new ItemStack(containerItem, 1));
					this.queueRoutedItem(itemToSend, data.output);
				}
			}
		}
		if (data.getItemStack().stackSize < 1){
			((PipeTransportItems)this.transport).items.scheduleRemoval(data);
		}
		return true;
	}

	@Override
	public void drop(PipeTransportItems pipe, TravelingItem data) {}

	@Override
	public void centerReached(PipeTransportItems pipe, TravelingItem data) {}
	
	@Override
	public boolean hasGenericInterests() {
		return true;
	}
// from PipeItemsFluidSupplier
	private SimpleInventory dummyInventory = new SimpleInventory(9, "Fluids to keep stocked", 127);
	
	private final HashMap<ItemIdentifier, Integer> _requestedItems = new HashMap<ItemIdentifier, Integer>();
	
	private boolean _requestPartials = false;
		
	@Override
	public void throttledUpdateEntity() {
		if (MainProxy.isClient(getWorld())) return;
		super.throttledUpdateEntity();
		WorldUtil worldUtil = new WorldUtil(getWorld(), getX(), getY(), getZ());
		for (AdjacentTile tile :  worldUtil.getAdjacentTileEntities(true)){
			if (!(tile.tile instanceof IFluidHandler) || tile.tile instanceof TileGenericPipe) continue;
			IFluidHandler container = (IFluidHandler) tile.tile;
			if (container.getTankInfo(ForgeDirection.UNKNOWN) == null || container.getTankInfo(ForgeDirection.UNKNOWN).length == 0) continue;
			
			//How much do I want?
			Map<ItemIdentifier, Integer> wantContainers = dummyInventory.getItemsAndCount();
			HashMap<FluidIdentifier, Integer> wantFluids = new HashMap<FluidIdentifier, Integer>();
			for (Entry<ItemIdentifier, Integer> item : wantContainers.entrySet()){
				ItemStack wantItem = item.getKey().unsafeMakeNormalStack(1);
				FluidStack liquidstack = FluidContainerRegistry.getFluidForFilledItem(wantItem);
				if (liquidstack == null) continue;
				wantFluids.put(FluidIdentifier.get(liquidstack), item.getValue() * liquidstack.amount);
			}

			//How much do I have?
			HashMap<FluidIdentifier, Integer> haveFluids = new HashMap<FluidIdentifier, Integer>();
			
			FluidTankInfo[] result = container.getTankInfo(ForgeDirection.UNKNOWN);
			for (FluidTankInfo slot : result){
				if (slot.fluid == null || !wantFluids.containsKey(FluidIdentifier.get(slot.fluid))) continue;
				Integer liquidWant = haveFluids.get(FluidIdentifier.get(slot.fluid));
				if (liquidWant==null){
					haveFluids.put(FluidIdentifier.get(slot.fluid), slot.fluid.amount);
				} else {
					haveFluids.put(FluidIdentifier.get(slot.fluid), liquidWant +  slot.fluid.amount);
				}
			}
			
			//HashMap<Integer, Integer> needFluids = new HashMap<Integer, Integer>();
			//Reduce what I have and what have been requested already
			for (Entry<FluidIdentifier, Integer> liquidId: wantFluids.entrySet()){
				Integer haveCount = haveFluids.get(liquidId.getKey());
				if (haveCount != null){
					liquidId.setValue(liquidId.getValue() - haveCount);
				}
				for (Entry<ItemIdentifier, Integer> requestedItem : _requestedItems.entrySet()){
					if(requestedItem.getKey().getFluidIdentifier() == liquidId.getKey()) {
						ItemStack wantItem = requestedItem.getKey().unsafeMakeNormalStack(1);
						FluidStack requestedFluidId = FluidContainerRegistry.getFluidForFilledItem(wantItem);
						if (requestedFluidId == null) continue;
						liquidId.setValue(liquidId.getValue() - requestedItem.getValue() * requestedFluidId.amount);
					}
				}
			}
			
			((PipeItemsFluidSupplier)this.container.pipe).setRequestFailed(false);
			
			//Make request
			
			for (ItemIdentifier need : wantContainers.keySet()){
				FluidStack requestedFluidId = FluidContainerRegistry.getFluidForFilledItem(need.unsafeMakeNormalStack(1));
				if (requestedFluidId == null) continue;
				if (!wantFluids.containsKey(FluidIdentifier.get(requestedFluidId))) continue;
				int countToRequest = wantFluids.get(FluidIdentifier.get(requestedFluidId)) / requestedFluidId.amount;
				if (countToRequest < 1) continue;
				
				if(!useEnergy(11)) {
					break;
				}
				
				boolean success = false;

				if(_requestPartials) {
					countToRequest = RequestTree.requestPartial(need.makeStack(countToRequest), (IRequestItems) this.container.pipe);
					if(countToRequest > 0) {
						success = true;
					}
				} else {
					success = RequestTree.request(need.makeStack(countToRequest), (IRequestItems) this.container.pipe, null);
				}
				
				if (success){
					Integer currentRequest = _requestedItems.get(need);
					if(currentRequest==null) {
						_requestedItems.put(need, countToRequest);
					} else {
						_requestedItems.put(need, currentRequest + countToRequest);
					}
				} else{
					((PipeItemsFluidSupplier)this.container.pipe).setRequestFailed(true);
				}
			}
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);	
		dummyInventory.readFromNBT(nbttagcompound, "");
		_requestPartials = nbttagcompound.getBoolean("requestpartials");
    }

	@Override
    public void writeToNBT(NBTTagCompound nbttagcompound) {
    	super.writeToNBT(nbttagcompound);
    	dummyInventory.writeToNBT(nbttagcompound, "");
    	nbttagcompound.setBoolean("requestpartials", _requestPartials);
    }
	
	private void decreaseRequested(ItemIdentifierStack item) {
		int remaining = item.stackSize;
		//see if we can get an exact match
		Integer count = _requestedItems.get(item.getItem());
		if (count != null) {
			_requestedItems.put(item.getItem(), Math.max(0, count - remaining));
			remaining -= count;
		}
		if(remaining <= 0) {
			return;
		}
		//still remaining... was from fuzzyMatch on a crafter
		for(Entry<ItemIdentifier, Integer> e : _requestedItems.entrySet()) {
			if(e.getKey().itemID == item.getItem().itemID && e.getKey().itemDamage == item.getItem().itemDamage) {
				int expected = e.getValue();
				e.setValue(Math.max(0, expected - remaining));
				remaining -= expected;
			}
			if(remaining <= 0) {
				return;
			}
		}
		//we have no idea what this is, log it.
		LogisticsPipes.requestLog.info("liquid supplier got unexpected item " + item.toString());
	}

	@Override
	public void itemLost(ItemIdentifierStack item) {
		decreaseRequested(item);
	}

	@Override
	public void itemArrived(ItemIdentifierStack item) {
		decreaseRequested(item);
		delayThrottle();
	}
	
	public boolean isRequestingPartials(){
		return _requestPartials;
	}
	
	public void setRequestingPartials(boolean value){
		_requestPartials = value;
	}

	@Override
	public void onWrenchClicked(EntityPlayer entityplayer) {
		if(MainProxy.isServer(entityplayer.worldObj)) {
			entityplayer.openGui(LogisticsPipes.instance, GuiIDs.GUI_FluidSupplier_ID, getWorld(), getX(), getY(), getZ());
		}
	}
	
	/*** GUI ***/
	public SimpleInventory getDummyInventory() {
		return dummyInventory;
	}
}
