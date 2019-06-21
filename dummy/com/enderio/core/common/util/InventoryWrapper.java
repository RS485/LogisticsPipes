package com.enderio.core.common.util;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.ITextComponent;

public class InventoryWrapper implements ISidedInventory {

	public static ISidedInventory asSidedInventory(IInventory inv) {
		if (inv == null) {
			return null;
		}
		if (inv instanceof ISidedInventory) {
			return (ISidedInventory) inv;
		}
		return new InventoryWrapper(inv);
	}

	public InventoryWrapper(IInventory inventory) {
	}

	public IInventory getWrappedInv() {
		return null;
	}

	@Override
	public int getSizeInventory() {
		return 0;
	}

	@Override
	public boolean isEmpty() {
		return false;
	}

	@Nonnull
	@Override
	public ItemStack getStackInSlot(int slot) {
		return ItemStack.EMPTY;
	}

	@Nonnull
	@Override
	public ItemStack decrStackSize(int slot, int amount) {
		return ItemStack.EMPTY;
	}

	@Override
	public void setInventorySlotContents(int slot, @Nullable ItemStack itemStack) {

	}

	@Override
	public int getInventoryStackLimit() {
		return 0;
	}

	@Override
	public void markDirty() {
	}

	@Override
	public boolean isUsableByPlayer(EntityPlayer player) {
		return false;
	}

	@Override
	public boolean isItemValidForSlot(int slot, @Nonnull ItemStack itemStack) {
		return false;
	}

	@Override
	public int[] getSlotsForFace(EnumFacing side) {
		int[] slots = new int[0];
		return slots;
	}

	@Override
	public boolean canInsertItem(int slot, @Nonnull ItemStack itemStack, EnumFacing side) {
		return isItemValidForSlot(slot, itemStack);
	}

	@Override
	public boolean canExtractItem(int slot, @Nonnull ItemStack itemStack, EnumFacing side) {
		return slot >= 0 && slot < getSizeInventory();
	}

	@Nonnull
	@Override
	public ItemStack removeStackFromSlot(int index) {
		return ItemStack.EMPTY;
	}

	@Override
	public void openInventory(EntityPlayer player) {
	}

	@Override
	public void closeInventory(EntityPlayer player) {
	}

	@Override
	public int getField(int id) {
		return 0;
	}

	@Override
	public void setField(int id, int value) {

	}

	@Override
	public int getFieldCount() {
		return 0;
	}

	@Override
	public void clear() {
	}

	@Override
	public String getName() {
		return null;
	}

	@Override
	public boolean hasCustomName() {
		return false;
	}

	@Override
	public ITextComponent getDisplayName() {
		return null;
	}

}