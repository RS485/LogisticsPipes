package mcp.mobius.betterbarrels.common.blocks;

import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import powercrystals.minefactoryreloaded.api.IDeepStorageUnit;

public abstract interface IBarrelStorage extends ISidedInventory, IDeepStorageUnit {
	public abstract boolean hasItem();
	
	public abstract ItemStack getItem();
	
	public abstract ItemStack getItemForRender();
	
	public abstract void setItem(ItemStack paramItemStack);
	
	public abstract boolean sameItem(ItemStack paramItemStack);
	
	public abstract int getAmount();
	
	public abstract void setAmount(int paramInt);
	
	public abstract int getMaxStacks();
	
	public abstract void setBaseStacks(int paramInt);
	
	public abstract NBTTagCompound writeTagCompound();
	
	public abstract void readTagCompound(NBTTagCompound paramNBTTagCompound);
	
	public abstract int addStack(ItemStack paramItemStack);
	
	public abstract ItemStack getStack();
	
	public abstract ItemStack getStack(int paramInt);
	
	public abstract boolean switchGhosting();
	
	public abstract boolean isGhosting();
	
	public abstract void setGhosting(boolean paramBoolean);
	
	public abstract boolean isVoid();
	
	public abstract void setVoid(boolean paramBoolean);
	
	public abstract boolean isCreative();
	
	public abstract void setCreative(boolean paramBoolean);
	
	public abstract void addStorageUpgrade();
	
	public abstract void rmStorageUpgrade();
	
	public abstract ItemStack decrStackSize_Hopper(int paramInt1, int paramInt2);
}
