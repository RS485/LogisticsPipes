/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.utils;

import java.util.LinkedList;

import logisticspipes.LogisticsPipes;
import logisticspipes.interfaces.routing.ISaveState;
import logisticspipes.proxy.MainProxy;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;

public class SimpleInventory implements IInventory, ISaveState{

	private ItemStack[] _contents;
	private String _name;
	private int _stackLimit;
	
	private final LinkedList<ISimpleInventoryEventHandler> _listener = new LinkedList<ISimpleInventoryEventHandler>(); 
	
	public SimpleInventory(int size, String name, int stackLimit){
		_contents = new ItemStack[size];
		_name = name;
		_stackLimit = stackLimit;
	}
	
	@Override
	public int getSizeInventory() {
		return _contents.length;
	}

	@Override
	public ItemStack getStackInSlot(int i) {
		return _contents[i];
	}

	@Override
	public ItemStack decrStackSize(int i, int j) {
		if (_contents[i] == null) return null;
		if (_contents[i].stackSize > j) return _contents[i].splitStack(j);
		ItemStack ret = _contents[i];
		_contents[i] = null;
		return ret;
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack) {
		_contents[i] = itemstack;
		
	}

	@Override
	public String getInvName() {
		return _name;
	}

	@Override
	public int getInventoryStackLimit() {
		return _stackLimit;
	}

	@Override
	public void onInventoryChanged() {
		for (ISimpleInventoryEventHandler handler : _listener){
			handler.InventoryChanged(this);
		}
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer) {return false;}

	@Override
	public void openChest() {}

	@Override
	public void closeChest() {}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		readFromNBT(nbttagcompound, "");
	}
	
	public void readFromNBT(NBTTagCompound nbttagcompound, String prefix) {
		NBTTagList nbttaglist = nbttagcompound.getTagList(prefix + "items");
    	
    	for (int j = 0; j < nbttaglist.tagCount(); ++j) {    		
    		NBTTagCompound nbttagcompound2 = (NBTTagCompound) nbttaglist.tagAt(j);
    		int index = nbttagcompound2.getInteger("index");
    		if(index < _contents.length) {
    			_contents [index] = ItemStack.loadItemStackFromNBT(nbttagcompound2);
    		} else {
    			LogisticsPipes.log.severe("SimpleInventory: java.lang.ArrayIndexOutOfBoundsException: " + index + " of " + _contents.length);
    		}
    	}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		writeToNBT(nbttagcompound, "");
	}

	public void writeToNBT(NBTTagCompound nbttagcompound, String prefix) {
		NBTTagList nbttaglist = new NBTTagList();
    	for (int j = 0; j < _contents.length; ++j) {    		    		
    		if (_contents[j] != null && _contents[j].stackSize > 0) {
        		NBTTagCompound nbttagcompound2 = new NBTTagCompound ();
        		nbttaglist.appendTag(nbttagcompound2);
    			nbttagcompound2.setInteger("index", j);
    			_contents[j].writeToNBT(nbttagcompound2);	
    		}     		
    	}
    	nbttagcompound.setTag(prefix + "items", nbttaglist);
    	nbttagcompound.setInteger(prefix + "itemsCount", _contents.length);
	}

	public void dropContents(World worldObj, int xCoord, int yCoord, int zCoord) {
		if(MainProxy.isServer(worldObj)) {
			for(int i=0;i<_contents.length;i++) {
				while(_contents[i] != null) {
					ItemStack todrop = decrStackSize(i, _contents[i].getMaxStackSize());
			    	dropItems(worldObj, todrop, xCoord, yCoord, zCoord);
				}
			}
		}
	}

	private static void dropItems(World world, ItemStack stack, int i, int j, int k) {
		if(stack.stackSize <= 0)
			return;
		float f1 = 0.7F;
		double d = (world.rand.nextFloat() * f1) + (1.0F - f1) * 0.5D;
		double d1 = (world.rand.nextFloat() * f1) + (1.0F - f1) * 0.5D;
		double d2 = (world.rand.nextFloat() * f1) + (1.0F - f1) * 0.5D;
		EntityItem entityitem = new EntityItem(world, i + d, j + d1, k + d2, stack);
		entityitem.delayBeforeCanPickup = 10;
		world.spawnEntityInWorld(entityitem);
	}
	
	public void addListener(ISimpleInventoryEventHandler listner){
		if (!_listener.contains(listner)){
			_listener.add(listner);
		}
	}
	
	public void removeListener(ISimpleInventoryEventHandler listner){
		if (_listener.contains(listner)){
			_listener.remove(listner);
		}
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int i) {
		if (this._contents[i] == null) return null;
		
		ItemStack stackToTake = this._contents[i];
		this._contents[i] = null;
		return stackToTake;
	}

	public void handleItemIdentifierList(LinkedList<ItemIdentifierStack> _allItems) {
		int i=0;
		for(ItemIdentifierStack stack:_allItems) {
			if(_contents.length <= i) break;
			if(stack == null) {
				_contents[i] = null;
			} else {
				_contents[i] = stack.makeNormalStack();
			}
			i++;
		}
		onInventoryChanged();
	}
	
	public int tryAddToSlot(int i, ItemStack stack) {
		ItemStack slot = this.getStackInSlot(i);
		if(slot == null) {
			this.setInventorySlotContents(i, stack.copy());
			return stack.stackSize;
		}
		ItemIdentifier slotIdent = ItemIdentifier.get(slot);
		ItemIdentifier stackIdent = ItemIdentifier.get(stack);
		if(slotIdent.equals(stackIdent)) {
			slot.stackSize += stack.stackSize;
			if(slot.stackSize > 127) {
				int ans = stack.stackSize - (slot.stackSize - 127);
				slot.stackSize = 127;
				return ans;
			} else {
				return stack.stackSize;
			}
		} else {
			return 0;
		}
	}
	
	public int addCompressed(ItemStack stack) {
		stack = stack.copy();
		for(int i=0; i<this._contents.length;i++) {
			if(stack.stackSize <= 0) {
				break;
			}
			int added = tryAddToSlot(i, stack);
			stack.stackSize -= added;
		}
		onInventoryChanged();
		return stack.stackSize;
	}
}
