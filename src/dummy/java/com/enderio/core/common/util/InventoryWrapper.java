package com.enderio.core.common.util;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;

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

	@Override
	@Nonnull
	public ItemStack getStackInSlot(int slot) {
		return ItemStack.EMPTY;
	}

	@Override
	@Nonnull
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
	public boolean isUsableByPlayer(@Nonnull EntityPlayer player) {
		return false;
	}

	@Override
	public boolean isItemValidForSlot(int slot, @Nonnull ItemStack itemStack) {
		return false;
	}

	@Override
	@Nonnull
	public int[] getSlotsForFace(@Nonnull EnumFacing side) {
		return new int[0];
	}

	@Override
	public boolean canInsertItem(int slot, @Nonnull ItemStack itemStack, @Nonnull EnumFacing side) {
		return isItemValidForSlot(slot, itemStack);
	}

	@Override
	public boolean canExtractItem(int slot, @Nonnull ItemStack itemStack, @Nonnull EnumFacing side) {
		return slot >= 0 && slot < getSizeInventory();
	}

	@Override
	@Nonnull
	public ItemStack removeStackFromSlot(int index) {
		return ItemStack.EMPTY;
	}

	@Override
	public void openInventory(@Nonnull EntityPlayer player) {
	}

	@Override
	public void closeInventory(@Nonnull EntityPlayer player) {
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
	@Nonnull
	public String getName() {
		return "";
	}

	@Override
	public boolean hasCustomName() {
		return false;
	}

	@Override
	@Nonnull
	public ITextComponent getDisplayName() {
		return new TextComponentString("");
	}

}