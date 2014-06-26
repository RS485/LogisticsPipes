/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.pipes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import logisticspipes.LogisticsPipes;
import logisticspipes.interfaces.IInventoryUtil;
import logisticspipes.interfaces.routing.IAdditionalTargetInformation;
import logisticspipes.interfaces.routing.IRequestItems;
import logisticspipes.interfaces.routing.IRequireReliableTransport;
import logisticspipes.modules.abstractmodules.LogisticsModule;
import logisticspipes.network.GuiIDs;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.module.SupplierPipeLimitedPacket;
import logisticspipes.network.packets.modules.SupplierPipeMode;
import logisticspipes.pipefxhandlers.Particles;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.debug.StatusEntry;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.request.RequestTree;
import logisticspipes.textures.Textures;
import logisticspipes.textures.Textures.TextureType;
import logisticspipes.utils.AdjacentTile;
import logisticspipes.utils.WorldUtil;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.item.ItemIdentifierInventory;
import logisticspipes.utils.item.ItemIdentifierStack;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import buildcraft.transport.TileGenericPipe;
import cpw.mods.fml.common.network.Player;

public class PipeItemsSupplierLogistics extends CoreRoutedPipe implements IRequestItems, IRequireReliableTransport{

	private boolean _lastRequestFailed = false;
		
	public PipeItemsSupplierLogistics(int itemID) {
		super(itemID);
		throttleTime = 100;
	}
	
	@Override
	public TextureType getCenterTexture() {
		return Textures.LOGISTICSPIPE_SUPPLIER_TEXTURE;
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
		return ItemSendMode.Normal;
	}

	// import from PipeItemsSupplierLogistics
	
	private ItemIdentifierInventory dummyInventory = new ItemIdentifierInventory(9, "", 127);
	
	private final HashMap<ItemIdentifier, Integer> _requestedItems = new HashMap<ItemIdentifier, Integer>();
	
	public enum SupplyMode {
		Partial,
		Full,
		Bulk50,
		Bulk100,
		Infinite
	}
	
	public enum PatternMode {
		Partial,
		Full,
		Bulk50,
		Bulk100
	}
	
	private SupplyMode _requestMode = SupplyMode.Bulk50;
	private PatternMode _patternMode = PatternMode.Bulk50;
	@Getter
	@Setter
	private boolean isLimited = true;

	public boolean pause = false;

	public int[] slotArray = new int[9]; //TODO save
	
	@Override
	public void onWrenchClicked(EntityPlayer entityplayer) {
		//pause = true; //Pause until GUI is closed //TODO Find a way to handle this
		entityplayer.openGui(LogisticsPipes.instance, GuiIDs.GUI_SupplierPipe_ID, getWorld(), getX(), getY(), getZ());
		MainProxy.sendPacketToPlayer(PacketHandler.getPacket(SupplierPipeMode.class).setHasPatternUpgrade(getUpgradeManager().hasPatternUpgrade()).setInteger((getUpgradeManager().hasPatternUpgrade() ? getPatternMode() : getSupplyMode()).ordinal()).setPosX(getX()).setPosY(getY()).setPosZ(getZ()), (Player)entityplayer);
		MainProxy.sendPacketToPlayer(PacketHandler.getPacket(SupplierPipeLimitedPacket.class).setLimited(isLimited()).setPosX(getX()).setPosY(getY()).setPosZ(getZ()), (Player)entityplayer);
	}
	
	/*** GUI ***/
	public ItemIdentifierInventory getDummyInventory() {
		return dummyInventory;
	}

	@Override
	public void throttledUpdateEntity() {
		
		if (!((CoreRoutedPipe)this.container.pipe).isEnabled()){
			return;
		}
		
		if(MainProxy.isClient(this.getWorld())) return;
		if (pause) return;
		super.throttledUpdateEntity();

		for(int amount : _requestedItems.values()) {
			if(amount > 0) {
				spawnParticle(Particles.VioletParticle, 2);
			}
		}

		WorldUtil worldUtil = new WorldUtil(getWorld(), getX(), getY(), getZ());
		for (AdjacentTile tile :  worldUtil.getAdjacentTileEntities(true)){
			if (tile.tile instanceof TileGenericPipe) continue;
			if (!(tile.tile instanceof IInventory)) continue;
			
			IInventory inv = (IInventory) tile.tile;
			if (inv.getSizeInventory() < 1) continue;
			IInventoryUtil invUtil = SimpleServiceLocator.inventoryUtilFactory.getInventoryUtil(inv);
			
			if(getUpgradeManager().hasPatternUpgrade()) {
				createPatternRequest(invUtil);
			} else {
				createSupplyRequest(invUtil);
			}

		}
	}

	private void createPatternRequest(IInventoryUtil invUtil) {
		debug.log("Supplier: Start calculating pattern request");
		((PipeItemsSupplierLogistics)this.container.pipe).setRequestFailed(false);
		for(int i=0;i < 9;i++) {
			ItemIdentifierStack needed = dummyInventory.getIDStackInSlot(i);
			if(needed == null) continue;
			if(invUtil.getSizeInventory() <= slotArray[i]) continue;
			ItemStack stack = invUtil.getStackInSlot(slotArray[i]);
			ItemIdentifierStack have = null;
			if(stack != null) {
				have = ItemIdentifierStack.getFromStack(stack);
			}
			int haveCount = 0;
			if(have != null) {
				if(!have.getItem().equals(needed.getItem())) {
					debug.log("Supplier: Slot for " + i + ", " + needed + " already taken by " + have);
					((PipeItemsSupplierLogistics)this.container.pipe).setRequestFailed(true);
					continue;
				}
				haveCount = have.getStackSize();
			}
			if( ( _patternMode==PatternMode.Bulk50 && haveCount > needed.getStackSize()/2) ||
			    ( _patternMode==PatternMode.Bulk100 && haveCount >= needed.getStackSize())) {
				continue;
			}
			
			
			int neededCount = needed.getStackSize() - haveCount;
			if(neededCount < 1) continue;
			
			ItemIdentifierStack toRequest = new ItemIdentifierStack(needed.getItem(), neededCount);
			
			debug.log("Supplier: Missing for slot " + i + ": " + toRequest);

			if(!useEnergy(10)) {
				break;
			}
			
			boolean success = false;

			if(_patternMode != PatternMode.Full) {
				debug.log("Supplier: Requesting partial: " + toRequest);
				neededCount = RequestTree.requestPartial(toRequest, (IRequestItems) container.pipe, null);
				debug.log("Supplier: Requested: " + toRequest.getItem().makeStack(neededCount));
				if(neededCount > 0) {
					success = true;
				}
			} else {
				debug.log("Supplier: Requesting: " + toRequest);
				success = RequestTree.request(toRequest, (IRequestItems) container.pipe, null);
				if(success) {
					debug.log("Supplier: Request success");
				} else {
					debug.log("Supplier: Request failed");
				}
			}
			
			if (success){
				Integer currentRequest = _requestedItems.get(toRequest.getItem());
				if(currentRequest == null) {
					_requestedItems.put(toRequest.getItem(), neededCount);
				} else {
					_requestedItems.put(toRequest.getItem(), currentRequest + neededCount);
				}
			} else {
				((PipeItemsSupplierLogistics)this.container.pipe).setRequestFailed(true);
			}
		}
	}

	private void createSupplyRequest(IInventoryUtil invUtil) {
		debug.log("Supplier: Start calculating supply request");
		//How many do I want?
		HashMap<ItemIdentifier, Integer> needed = new HashMap<ItemIdentifier, Integer>(dummyInventory.getItemsAndCount());
		debug.log("Supplier: Needed: " + needed);
		
		//How many do I have?
		Map<ItemIdentifier, Integer> have = invUtil.getItemsAndCount();
		debug.log("Supplier: Have:   " + have);
		
		//How many do I have?
		HashMap<ItemIdentifier, Integer> haveUndamaged = new HashMap<ItemIdentifier, Integer>();
		for (Entry<ItemIdentifier, Integer> item : have.entrySet()){
			Integer n=haveUndamaged.get(item.getKey().getUndamaged());
			if(n==null)
				haveUndamaged.put(item.getKey().getUndamaged(), item.getValue());
			else
				haveUndamaged.put(item.getKey().getUndamaged(), item.getValue()+n);
		}
		
		//Reduce what I have and what have been requested already
		for (Entry<ItemIdentifier, Integer> item : needed.entrySet()){
			Integer haveCount = haveUndamaged.get(item.getKey().getUndamaged());
			if(haveCount==null)
				haveCount=0;
			int spaceAvailable=invUtil.roomForItem(item.getKey());
			if(_requestMode==SupplyMode.Infinite){
				item.setValue(Math.min(item.getKey().getMaxStackSize(),spaceAvailable));
				continue;

			}
			if(spaceAvailable == 0 || 
					( _requestMode==SupplyMode.Bulk50 && haveCount>item.getValue()/2) ||
					( _requestMode==SupplyMode.Bulk100 && haveCount>=item.getValue()))
			{
				item.setValue(0);
				continue;
			}
			if (haveCount >0){
				item.setValue(item.getValue() - haveCount);
				// so that 1 damaged item can't satisfy a request for 2 other damage values.
				haveUndamaged.put(item.getKey().getUndamaged(),haveCount - item.getValue());
			}
			Integer requestedCount =  _requestedItems.get(item.getKey());
			if (requestedCount!=null){
				item.setValue(item.getValue() - requestedCount);
			}
		}
		
		debug.log("Supplier: Missing:   " + needed);
		
		((PipeItemsSupplierLogistics)this.container.pipe).setRequestFailed(false);

		//Make request
		for (Entry<ItemIdentifier, Integer> need : needed.entrySet()){
			Integer amountRequested = need.getValue();
			if (amountRequested==null || amountRequested < 1) continue;
			int neededCount = amountRequested;
			if(!useEnergy(10)) {
				break;
			}
			
			boolean success = false;

			if(_requestMode!=SupplyMode.Full) {
				debug.log("Supplier: Requesting partial: " + need.getKey().makeStack(neededCount));
				neededCount = RequestTree.requestPartial(need.getKey().makeStack(neededCount), (IRequestItems) container.pipe, null);
				debug.log("Supplier: Requested: " + need.getKey().makeStack(neededCount));
				if(neededCount > 0) {
					success = true;
				}
			} else {
				debug.log("Supplier: Requesting: " + need.getKey().makeStack(neededCount));
				success = RequestTree.request(need.getKey().makeStack(neededCount), (IRequestItems) container.pipe, null);
				if(success) {
					debug.log("Supplier: Request success");
				} else {
					debug.log("Supplier: Request failed");
				}
			}
			
			if (success){
				Integer currentRequest = _requestedItems.get(need.getKey());
				if(currentRequest == null) {
					_requestedItems.put(need.getKey(), neededCount);
					debug.log("Supplier: Inserting Requested Items: " + neededCount);
				} else {
					_requestedItems.put(need.getKey(), currentRequest + neededCount);
					debug.log("Supplier: Raising Requested Items from: " + currentRequest + " to: " + currentRequest + neededCount);
				}
			} else {
				((PipeItemsSupplierLogistics)this.container.pipe).setRequestFailed(true);
			}
			
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);	
		dummyInventory.readFromNBT(nbttagcompound, "");
		if(nbttagcompound.hasKey("requestmode")){
			_requestMode=SupplyMode.values()[nbttagcompound.getShort("requestmode")];
		}
		if(nbttagcompound.hasKey("patternmode")){
			_patternMode=PatternMode.values()[nbttagcompound.getShort("patternmode")];
		}
		if(nbttagcompound.hasKey("limited")){
			setLimited(nbttagcompound.getBoolean("limited"));
		}
		if(nbttagcompound.hasKey("requestpartials")){
			boolean oldPartials = nbttagcompound.getBoolean("requestpartials");
			if(oldPartials)
				_requestMode=SupplyMode.Partial;
			else
				_requestMode=SupplyMode.Full;
		}
		for(int i=0;i<9;i++) {
			slotArray[i] = nbttagcompound.getInteger("slotArray_" + i);
		}
    }

	@Override
    public void writeToNBT(NBTTagCompound nbttagcompound) {
    	super.writeToNBT(nbttagcompound);
    	dummyInventory.writeToNBT(nbttagcompound, "");
    	nbttagcompound.setShort("requestmode", (short) _requestMode.ordinal());
    	nbttagcompound.setShort("patternmode", (short) _patternMode.ordinal());
    	nbttagcompound.setBoolean("limited", isLimited());
    	for(int i=0;i<9;i++) {
			nbttagcompound.setInteger("slotArray_" + i, slotArray[i]);
		}
	}
	
	private void decreaseRequested(ItemIdentifierStack item) {
		int remaining = item.getStackSize();
		//see if we can get an exact match
		Integer count = _requestedItems.get(item.getItem());
		if (count != null) {
			debug.log("Supplier: Exact match. Still missing: " + Math.max(0, count - remaining));
			if(count - remaining > 0) {
				_requestedItems.put(item.getItem(), count - remaining);
			} else {
				_requestedItems.remove(item.getItem());
			}
			remaining -= count;
		}
		if(remaining <= 0) {
			return;
		}
		//still remaining... was from fuzzyMatch on a crafter
		Iterator<Entry<ItemIdentifier, Integer>> it = _requestedItems.entrySet().iterator();
		while(it.hasNext()) {
			Entry<ItemIdentifier, Integer> e = it.next();
			if(e.getKey().itemID == item.getItem().itemID && e.getKey().itemDamage == item.getItem().itemDamage) {
				int expected = e.getValue();
				debug.log("Supplier: Fuzzy match with" + e + ". Still missing: " + Math.max(0, expected - remaining));
				if(expected - remaining > 0) {
					e.setValue(expected - remaining);
				} else {
					it.remove();
				}
				remaining -= expected;
			}
			if(remaining <= 0) {
				return;
			}
		}
		//we have no idea what this is, log it.
		LogisticsPipes.requestLog.info("supplier got unexpected item " + item.toString());
		debug.log("Supplier: supplier got unexpected item " + item.toString());
	}

	@Override
	public void itemLost(ItemIdentifierStack item, IAdditionalTargetInformation info) {
		debug.log("Supplier: Registered Item Lost: " + item);
		decreaseRequested(item);
	}

	@Override
	public void itemArrived(ItemIdentifierStack item, IAdditionalTargetInformation info) {
		debug.log("Supplier: Registered Item Arrived: " + item);
		decreaseRequested(item);
		delayThrottle();
	}
	
	public SupplyMode getSupplyMode() {
		return _requestMode;
	}
	
	public void setSupplyMode(SupplyMode mode) {
		_requestMode = mode;
	}
	
	public PatternMode getPatternMode() {
		return _patternMode;
	}
	
	public void setPatternMode(PatternMode mode) {
		_patternMode = mode;
	}

	public int[] getSlotsForItemIdentifier(ItemIdentifier item) {
		int size = 0;
		for(int i=0;i<9;i++) {
			if(dummyInventory.getIDStackInSlot(i) != null && dummyInventory.getIDStackInSlot(i).getItem().equals(item)) size++;
		}
		int[] array = new int[size];
		int pos = 0;
		for(int i=0;i<9;i++) {
			if(dummyInventory.getIDStackInSlot(i) != null && dummyInventory.getIDStackInSlot(i).getItem().equals(item)) {
				array[pos++] = i;
			}
		}
		return array;
	}
	
	public int getInvSlotForSlot(int i) {
		return slotArray[i];
	}
	
	public int getAmountForSlot(int i) {
		return dummyInventory.getIDStackInSlot(i).getStackSize();
	}

	@Override
	public void addStatusInformation(List<StatusEntry> status) {
		super.addStatusInformation(status);
		StatusEntry entry = new StatusEntry();
		entry.name = "Requested Items";
		entry.subEntry = new ArrayList<StatusEntry>();
		for(Entry<ItemIdentifier, Integer> part:_requestedItems.entrySet()) {
			StatusEntry subEntry = new StatusEntry();
			subEntry.name = part.toString();
			entry.subEntry.add(subEntry);
		}
		status.add(entry);
	}
}
