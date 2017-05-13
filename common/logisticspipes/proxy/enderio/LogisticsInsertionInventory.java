package logisticspipes.proxy.enderio;

import javax.annotation.Nullable;

import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.ITextComponent;

public class LogisticsInsertionInventory implements IInventory {
	private final LogisticsTileGenericPipe pipe;
	private final EnumFacing from;

	public LogisticsInsertionInventory(LogisticsTileGenericPipe pipe, EnumFacing from) {
		this.pipe = pipe;
		this.from = from;
	}

	@Override
	public int getSizeInventory() {
		return 1;
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		return null;
	}

	@Override
	public ItemStack decrStackSize(int slot, int amount) {
		return null;
	}

	@Nullable
	@Override
	public ItemStack removeStackFromSlot(int index) {return null;}

	@Override
	public void setInventorySlotContents(int slot, ItemStack stack) {
		pipe.insertItem(from, stack);
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public void markDirty() {

	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer player) {
		return true;
	}

	@Override
	public void openInventory(EntityPlayer player) {

	}

	@Override
	public void closeInventory(EntityPlayer player) {

	}

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack stack) {
		return true;
	}

	@Override
	public int getField(int id) {
		return 0;
	}

	@Override
	public void setField(int id, int value) {}

	@Override
	public int getFieldCount() {return 0;}

	@Override
	public void clear() {}

	@Override
	public String getName() {return "LogisticsInsertionInventory";}

	@Override
	public boolean hasCustomName() {return false;}

	@Override
	public ITextComponent getDisplayName() {return null;}
}
