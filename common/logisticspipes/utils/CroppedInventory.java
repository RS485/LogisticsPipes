package logisticspipes.utils;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;

public class CroppedInventory implements IInventory{
	
	private final IInventory _baseInventory;
	private final int _cropStart;
	private final int _cropEnd;
	
	public CroppedInventory(IInventory baseInventory, int cropStart, int cropEnd){
		if (baseInventory == null) throw new RuntimeException("PRECONDITION FAILED: baseInventory cannot be null");
		_baseInventory = baseInventory;
		_cropStart = cropStart;
		_cropEnd = cropEnd;
	}

	@Override
	public int getSizeInventory() {
		return _baseInventory.getSizeInventory() - (_cropStart + _cropEnd);
	}

	@Override
	public ItemStack getStackInSlot(int i) {
		return _baseInventory.getStackInSlot(i + _cropStart);
	}

	@Override
	public ItemStack decrStackSize(int i, int j) {
		return _baseInventory.decrStackSize(i + _cropStart, j);
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack) {
		_baseInventory.setInventorySlotContents(i + _cropStart, itemstack);
	}

	@Override
	public String getInvName() {
		return _baseInventory.getInvName();
	}

	@Override
	public int getInventoryStackLimit() {
		return _baseInventory.getInventoryStackLimit();
	}

	@Override
	public void onInventoryChanged() {
		_baseInventory.onInventoryChanged();
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer) {
		return _baseInventory.isUseableByPlayer(entityplayer); 
	}

	@Override
	public void openChest() {
		_baseInventory.openChest();
	}

	@Override
	public void closeChest() {
		_baseInventory.closeChest();
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int var1) {
		return _baseInventory.getStackInSlotOnClosing(var1 + _cropStart);
	}
}
