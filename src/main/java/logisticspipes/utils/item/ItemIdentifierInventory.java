/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.utils.item;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;

import logisticspipes.LogisticsPipes;
import logisticspipes.interfaces.routing.ISaveState;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.FluidIdentifier;
import logisticspipes.utils.ISimpleInventoryEventHandler;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;

public class ItemIdentifierInventory implements IInventory, ISaveState {

	private ItemIdentifierStack[] _contents;
	private final String _name;
	private final int _stackLimit;
	private final HashMap<ItemIdentifier, Integer> _contentsMap;
	private final HashSet<ItemIdentifier> _contentsUndamagedSet;
	private final boolean isLiquidInvnetory;
	
	private final LinkedList<ISimpleInventoryEventHandler> _listener = new LinkedList<ISimpleInventoryEventHandler>(); 

	public ItemIdentifierInventory(int size, String name, int stackLimit, boolean liquidInv) {
		_contents = new ItemIdentifierStack[size];
		_name = name;
		_stackLimit = stackLimit;
		_contentsMap = new HashMap<ItemIdentifier, Integer>((int)(size * 1.5));
		_contentsUndamagedSet = new HashSet<ItemIdentifier>((int)(size * 1.5));
		isLiquidInvnetory = liquidInv;
	}

	public ItemIdentifierInventory(int size, String name, int stackLimit) {
		this(size, name, stackLimit, false);
	}
	
	@Override
	public int getSizeInventory() {
		return _contents.length;
	}

	@Override
	public ItemStack getStackInSlot(int i) {
		if (_contents[i] == null) return null;
		return _contents[i].makeNormalStack();
	}

	//NOTE: this is a clone, changing the return of this function does not altet the inventory
	public ItemIdentifierStack getIDStackInSlot(int i) {
		return _contents[i];
	}

	
	@Override
	public ItemStack decrStackSize(int slot, int count) {
		if (_contents[slot] == null) return null;
		if (_contents[slot].getStackSize() > count) {
			ItemStack ret = _contents[slot].makeNormalStack();
			ret.stackSize=count;
			_contents[slot].setStackSize(_contents[slot].getStackSize() - count);
			updateContents();
			return ret;
		}
		ItemStack ret = _contents[slot].makeNormalStack();
		_contents[slot] = null;
		updateContents();
		return ret;
	}
	
	// here so the returned stack can be stuck in another inventory without re-converting it/
	public ItemIdentifierStack decrIDStackSize(int slot, int count) {
		if (_contents[slot] == null) return null;
		if (_contents[slot].getStackSize() > count) {
			ItemIdentifierStack ret = _contents[slot].clone();
			ret.setStackSize(count);
			_contents[slot].setStackSize(_contents[slot].getStackSize() - count);
			updateContents();
			return ret;
		}
		ItemIdentifierStack ret = _contents[slot];
		_contents[slot] = null;
		updateContents();
		return ret;
	}


	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack) {
		if(itemstack == null) {
			_contents[i] = null;
		} else {
			if(!isValidStack(itemstack)) {
				if(LogisticsPipes.DEBUG) {
					new UnsupportedOperationException("Not valid for this Inventory: (" + itemstack + ")").printStackTrace();
				}
				return;
			}
			_contents[i] = ItemIdentifierStack.getFromStack(itemstack);
		}
		updateContents();
	}

	public void setInventorySlotContents(int i, ItemIdentifierStack itemstack) {
		if(itemstack == null) {
			_contents[i] = null;
		} else {
			if(!isValidStack(itemstack)) {
				if(LogisticsPipes.DEBUG) {
					new UnsupportedOperationException("Not valid for this Inventory: (" + itemstack + ")").printStackTrace();
				}
				return;
			}
			_contents[i] = itemstack;
		}
		updateContents();
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
		updateContents();
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
    			ItemStack stack = ItemStack.loadItemStackFromNBT(nbttagcompound2);
    			if(stack != null) {
					ItemIdentifierStack itemstack = ItemIdentifierStack.getFromStack(stack);
					if(!isValidStack(itemstack)) {
						FluidIdentifier fluid = FluidIdentifier.convertFromID(itemstack.getItem().itemID);
						if(fluid != null) {
							_contents [index] = fluid.getItemIdentifier().makeStack(1);
						}
					} else {
						_contents [index] = itemstack;
					}
    			}
    		} else {
    			LogisticsPipes.log.severe("SimpleInventory: java.lang.ArrayIndexOutOfBoundsException: " + index + " of " + _contents.length);
    		}
    	}
		updateContents();
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		writeToNBT(nbttagcompound, "");
	}

	public void writeToNBT(NBTTagCompound nbttagcompound, String prefix) {
		NBTTagList nbttaglist = new NBTTagList();
    	for (int j = 0; j < _contents.length; ++j) {    		    		
    		if (_contents[j] != null && _contents[j].getStackSize() > 0) {
        		NBTTagCompound nbttagcompound2 = new NBTTagCompound ();
        		nbttaglist.appendTag(nbttagcompound2);
    			nbttagcompound2.setInteger("index", j);
    			_contents[j].unsafeMakeNormalStack().writeToNBT(nbttagcompound2);	
    		}     		
    	}
    	nbttagcompound.setTag(prefix + "items", nbttaglist);
    	nbttagcompound.setInteger(prefix + "itemsCount", _contents.length);
	}

	public void dropContents(World worldObj, int posX, int posY, int posZ) {
		if(MainProxy.isServer(worldObj)) {
			for(int i=0;i<_contents.length;i++) {
				while(_contents[i] != null) {
					ItemStack todrop = decrStackSize(i, _contents[i].unsafeMakeNormalStack().getMaxStackSize());
			    	dropItems(worldObj, todrop, posX, posY, posZ);
				}
			}
			updateContents();
		}
	}

	public static void dropItems(World world, ItemStack stack, int i, int j, int k) {
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
		
		ItemStack stackToTake = this._contents[i].makeNormalStack();
		this._contents[i] = null;
		updateContents();
		return stackToTake;
	}

	public void handleItemIdentifierList(Collection<ItemIdentifierStack> _allItems) {
		int i=0;
		for(ItemIdentifierStack stack:_allItems) {
			if(_contents.length <= i) break;
			_contents[i] = stack;
			i++;
		}
		onInventoryChanged();
	}
	
	private int tryAddToSlot(int i, ItemStack stack, int realstacklimit) {
		if(!isValidStack(stack)) {
			if(LogisticsPipes.DEBUG) {
				new UnsupportedOperationException("Not valid for this Inventory: (" + stack + ")").printStackTrace();
			}
			return 0;
		}
		ItemIdentifierStack slot = _contents[i];
		if(slot == null) {
			_contents[i] = ItemIdentifierStack.getFromStack(stack);
			_contents[i].setStackSize(Math.min(_contents[i].getStackSize(), realstacklimit));
			return _contents[i].getStackSize();
		}
		ItemIdentifier stackIdent = ItemIdentifier.get(stack);
		ItemIdentifier slotIdent = slot.getItem();
		if(slotIdent.equals(stackIdent)) {
			slot.setStackSize(slot.getStackSize() + stack.stackSize);
			if(slot.getStackSize() > realstacklimit) {
				int ans = stack.stackSize - (slot.getStackSize() - realstacklimit);
				slot.setStackSize(realstacklimit);
				return ans;
			} else {
				return stack.stackSize;
			}
		} else {
			return 0;
		}
	}
	
	public int addCompressed(ItemStack stack, boolean ignoreMaxStackSize) {
		if(stack == null) return 0;
		if(!isValidStack(stack)) {
			if(LogisticsPipes.DEBUG) {
				new UnsupportedOperationException("Not valid for this Inventory: (" + stack + ")").printStackTrace();
			}
			return 0;
		}
		stack = stack.copy();

		ItemIdentifier stackIdent = ItemIdentifier.get(stack);
		int stacklimit = this._stackLimit;
		if(!ignoreMaxStackSize)
			stacklimit = Math.min(stacklimit, stackIdent.getMaxStackSize());

		for(int i=0; i<this._contents.length;i++) {
			if(stack.stackSize <= 0) {
				break;
			}
			if(_contents[i] == null) continue; //Skip Empty Slots on first attempt.
			int added = tryAddToSlot(i, stack, stacklimit);
			stack.stackSize -= added;
		}
		for(int i=0; i<this._contents.length;i++) {
			if(stack.stackSize <= 0) {
				break;
			}
			int added = tryAddToSlot(i, stack, stacklimit);
			stack.stackSize -= added;
		}
		onInventoryChanged();
		return stack.stackSize;
	}

	/* InventoryUtil-like functions */

	private void updateContents() {
		_contentsMap.clear();
		_contentsUndamagedSet.clear();
		for (int i = 0; i < _contents.length; i++) {
			if(_contents[i] == null) continue;
			ItemIdentifier itemId = _contents[i].getItem();
			Integer count = _contentsMap.get(itemId);
			if (count == null) {
				_contentsMap.put(itemId,  _contents[i].getStackSize());
			} else {
				_contentsMap.put(itemId, _contentsMap.get(itemId) +  _contents[i].getStackSize());
			}
			_contentsUndamagedSet.add(itemId.getUndamaged()); // add is cheaper than check then add; it just returns false if it is already there
		}
	}

	public int itemCount(final ItemIdentifier item) {
		Integer i =  _contentsMap.get(item);
		if(i == null) 
			return 0;
		return i;
	}

	public Map<ItemIdentifier, Integer> getItemsAndCount() {
		return _contentsMap;
	}

	public boolean containsItem(final ItemIdentifier item) {
		return _contentsMap.containsKey(item);
	}

	public boolean containsUndamagedItem(final ItemIdentifier item) {
		return _contentsUndamagedSet.contains(item);
	}

	public boolean isEmpty() {
		return _contentsMap.isEmpty();
	}

	@Override
	public boolean isInvNameLocalized() {
		// TODO ?
		return true;
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack) {
		// TODO Auto-generated method stub
		return true;
	}

	public void clearInventorySlotContents(int i) {
		_contents[i]=null;
		updateContents();
		
	}

	public void compact_first_9() {
		// Compact
		for (int i = 0; i < 9; i++) {
			final ItemIdentifierStack stackInSlot = getIDStackInSlot(i);
			if (stackInSlot == null) {
				continue;
			}
			final ItemIdentifier itemInSlot = stackInSlot.getItem();
			for (int j = i + 1; j < 9; j++) {
				final ItemIdentifierStack stackInOtherSlot = getIDStackInSlot(j);
				if (stackInOtherSlot == null) {
					continue;
				}
				if (itemInSlot == stackInOtherSlot.getItem()) {
					stackInSlot.setStackSize(stackInSlot.getStackSize() + stackInOtherSlot.getStackSize());
					clearInventorySlotContents(j);
				}
			}
			setInventorySlotContents(i,stackInSlot);
		}
		
		for (int i = 0; i < 9; i++) {
			if (getStackInSlot(i) != null) {
				continue;
			}
			for (int j = i + 1; j < 9; j++) {
				if (getStackInSlot(j) == null) {
					continue;
				}
				setInventorySlotContents(i, getStackInSlot(j));
				clearInventorySlotContents(j);
				break;
			}
		}
	}

	private boolean isValidStack(ItemStack stack) {
		if(isLiquidInvnetory) {
			return FluidIdentifier.get(stack) != null;
		}
		return true;
	}

	private boolean isValidStack(ItemIdentifierStack stack) {
		if(isLiquidInvnetory) {
			return FluidIdentifier.get(stack.getItem()) != null;
		}
		return true;
	}
}
