/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.pipes;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import logisticspipes.LogisticsPipes;
import logisticspipes.interfaces.IInventoryUtil;
import logisticspipes.interfaces.routing.IRequestItems;
import logisticspipes.interfaces.routing.IRequireReliableTransport;
import logisticspipes.modules.LogisticsModule;
import logisticspipes.network.GuiIDs;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.module.SupplierPipeLimitedPacket;
import logisticspipes.network.packets.modules.SupplierPipeMode;
import logisticspipes.pipefxhandlers.Particles;
import logisticspipes.pipes.basic.CoreRoutedPipe;
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
	
	private ItemIdentifierInventory dummyInventory = new ItemIdentifierInventory(9, "Items to keep stocked", 127);
	
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
		if(MainProxy.isServer(entityplayer.worldObj)) {
			entityplayer.openGui(LogisticsPipes.instance, GuiIDs.GUI_SupplierPipe_ID, getWorld(), getX(), getY(), getZ());
			MainProxy.sendPacketToPlayer(PacketHandler.getPacket(SupplierPipeMode.class).setHasPatternUpgrade(getUpgradeManager().hasPatternUpgrade()).setInteger((getUpgradeManager().hasPatternUpgrade() ? getPatternMode() : getSupplyMode()).ordinal()).setPosX(getX()).setPosY(getY()).setPosZ(getZ()), (Player)entityplayer);
			MainProxy.sendPacketToPlayer(PacketHandler.getPacket(SupplierPipeLimitedPacket.class).setLimited(isLimited()).setPosX(getX()).setPosY(getY()).setPosZ(getZ()), (Player)entityplayer);
		}
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
				MainProxy.sendSpawnParticlePacket(Particles.VioletParticle, getX(), getY(), getZ(), this.getWorld(), 2);
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
				if(have.getItem() != needed.getItem()) {
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
			
			ItemIdentifierStack toRequest = new ItemIdentifierStack(needed.getItem(), needed.getStackSize() - haveCount);
			
			if(!useEnergy(10)) {
				break;
			}
			
			boolean success = false;

			if(_patternMode != PatternMode.Full) {
				neededCount = RequestTree.requestPartial(toRequest, (IRequestItems) container.pipe);
				if(neededCount > 0) {
					success = true;
				}
			} else {
				success = RequestTree.request(toRequest, (IRequestItems) container.pipe, null);
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
		//How many do I want?
		HashMap<ItemIdentifier, Integer> needed = new HashMap<ItemIdentifier, Integer>(dummyInventory.getItemsAndCount());
		
		//How many do I have?
		Map<ItemIdentifier, Integer> have = invUtil.getItemsAndCount();
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
				neededCount = RequestTree.requestPartial(need.getKey().makeStack(neededCount), (IRequestItems) container.pipe);
				if(neededCount > 0) {
					success = true;
				}
			} else {
				success = RequestTree.request(need.getKey().makeStack(neededCount), (IRequestItems) container.pipe, null);
			}
			
			if (success){
				Integer currentRequest = _requestedItems.get(need.getKey());
				if(currentRequest == null) {
					_requestedItems.put(need.getKey(), neededCount);
				} else {
					_requestedItems.put(need.getKey(), currentRequest + neededCount);
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
		LogisticsPipes.requestLog.info("supplier got unexpected item " + item.toString());
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
			if(dummyInventory.getIDStackInSlot(i) != null && dummyInventory.getIDStackInSlot(i).getItem() == item) size++;
		}
		int[] array = new int[size];
		int pos = 0;
		for(int i=0;i<9;i++) {
			if(dummyInventory.getIDStackInSlot(i) != null && dummyInventory.getIDStackInSlot(i).getItem() == item) {
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
}
