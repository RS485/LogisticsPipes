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

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.NBTTagCompound;
import buildcraft.transport.TileGenericPipe;
import cpw.mods.fml.common.network.Player;
import logisticspipes.LogisticsPipes;
import logisticspipes.interfaces.IInventoryUtil;
import logisticspipes.interfaces.routing.IRequestItems;
import logisticspipes.interfaces.routing.IRequireReliableTransport;
import logisticspipes.modules.LogisticsModule;
import logisticspipes.network.GuiIDs;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.modules.SupplierPipeMode;
import logisticspipes.pipefxhandlers.Particles;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.request.RequestTree;
import logisticspipes.textures.Textures;
import logisticspipes.textures.Textures.TextureType;
import logisticspipes.utils.AdjacentTile;
import logisticspipes.utils.ItemIdentifier;
import logisticspipes.utils.ItemIdentifierStack;
import logisticspipes.utils.SimpleInventory;
import logisticspipes.utils.WorldUtil;

public class PipeItemsSupplierLogistics extends CoreRoutedPipe implements IRequestItems, IRequireReliableTransport{

	private boolean _lastRequestFailed = false;
		
	public PipeItemsSupplierLogistics(int itemID) {
		super( itemID);
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
	
	private SimpleInventory dummyInventory = new SimpleInventory(9, "Items to keep stocked", 127);
	
	private final HashMap<ItemIdentifier, Integer> _requestedItems = new HashMap<ItemIdentifier, Integer>();
	
	private boolean _requestPartials = false;

	public boolean pause = false;
	
	@Override
	public void onWrenchClicked(EntityPlayer entityplayer) {
		//pause = true; //Pause until GUI is closed //TODO Find a way to handle this
		if(MainProxy.isServer(entityplayer.worldObj)) {
			//GuiProxy.openGuiSupplierPipe(entityplayer.inventory, dummyInventory, this);
			entityplayer.openGui(LogisticsPipes.instance, GuiIDs.GUI_SupplierPipe_ID, getWorld(), getX(), getY(), getZ());
//TODO 		MainProxy.sendPacketToPlayer(new PacketPipeInteger(NetworkConstants.SUPPLIER_PIPE_MODE_RESPONSE, getX(), getY(), getZ(), isRequestingPartials() ? 1 : 0).getPacket(), (Player)entityplayer);
			MainProxy.sendPacketToPlayer(PacketHandler.getPacket(SupplierPipeMode.class).setInteger(isRequestingPartials() ? 1 : 0).setPosX(getX()).setPosY(getY()).setPosZ(getZ()), (Player)entityplayer);
		}
	}
	
	/*** GUI ***/
	public SimpleInventory getDummyInventory() {
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
				if (haveCount != null){
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

				if(_requestPartials) {
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
	
	public boolean isRequestingPartials(){
		return _requestPartials;
	}
	
	public void setRequestingPartials(boolean value){
		_requestPartials = value;
	}


}
