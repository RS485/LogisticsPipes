package logisticspipes.utils;

import logisticspipes.items.ItemModule;
import logisticspipes.items.LogisticsItemCard;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class CardManagmentInventory implements IInventory {
	
	SimpleInventory inv = new SimpleInventory(4, "", 1);
	
	@Override
	public int getSizeInventory() {
		return 10;
	}

	@Override
	public ItemStack getStackInSlot(int i) {
		if(i > -1 && i < 4) return inv.getStackInSlot(i);
		ItemStack card = inv.getStackInSlot(3);
		if(card != null) {
			NBTTagCompound nbt = card.getTagCompound();
			if(nbt == null) {
				nbt = new NBTTagCompound("tag");
			}
			NBTTagCompound colors = nbt.getCompoundTag("colors");
			if(colors == null) {
				colors = new NBTTagCompound();
			}
			int slot = i - 4;
			
			int colorCode;
			if(colors.hasKey("color:" + slot)) {
				colorCode = colors.getInteger("color:" + slot);
			} else {
				colors.setInteger("color:" + slot, 16);
				colorCode = 16;
			}
			
			Colors color = Colors.values()[colorCode];
			
			nbt.setCompoundTag("colors", colors);
			card.setTagCompound(nbt);
			inv.setInventorySlotContents(3, card);
			
			return color.getItemStack();
		}

		return null;
	}

	@Override
	public ItemStack decrStackSize(int i, int j) {
		if(i > -1 && i < 4) return inv.decrStackSize(i, j);
		return null;
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int i) {
		if(i > -1 && i < 4) return inv.getStackInSlotOnClosing(i);
		return null;
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack) {
		if(i > -1 && i < 4) {
			if(i == 0 && itemstack != null && inv.getStackInSlot(1) != null && inv.getStackInSlot(2) == null && inv.getStackInSlot(1).getItemDamage() == itemstack.getItemDamage()) {
				itemstack.setTagCompound(inv.getStackInSlot(1).getTagCompound());
				inv.setInventorySlotContents(2, itemstack);
				return;
			}
			if(i == 1 && itemstack != null && inv.getStackInSlot(0) != null && inv.getStackInSlot(2) == null && inv.getStackInSlot(0).getItemDamage() == itemstack.getItemDamage()) {
				itemstack.setTagCompound(inv.getStackInSlot(0).getTagCompound());
				inv.setInventorySlotContents(2, itemstack);
				return;
			}
			inv.setInventorySlotContents(i, itemstack);
			return;
		}
		ItemStack card = inv.getStackInSlot(3);
		if(card != null) {
			NBTTagCompound nbt = card.getTagCompound();
			if(nbt == null) {
				nbt = new NBTTagCompound("tag");
			}
			NBTTagCompound colors = nbt.getCompoundTag("colors");
			if(colors == null) {
				colors = new NBTTagCompound();
			}
			int slot = i - 4;
			colors.setInteger("color:" + slot, Colors.getColor(itemstack).ordinal());
			nbt.setCompoundTag("colors", colors);
			card.setTagCompound(nbt);
			inv.setInventorySlotContents(3, card);
		}
	}

	@Override
	public String getInvName() {
		return "Card Managment Inventory";
	}

	@Override
	public boolean isInvNameLocalized() {
		return false;
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public void onInventoryChanged() {}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer) {
		return true;
	}

	@Override
	public void openChest() {}

	@Override
	public void closeChest() {}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack) {
		if(itemstack == null) return false;
		if(i == 0 || i == 1) {
			return itemstack.getItem() instanceof ItemModule;
		} else if(i == 3) {
			return itemstack.getItem() instanceof LogisticsItemCard;
		}
		return false;
	}
	
	public void close(EntityPlayer player,int x, int y, int z) {
		inv.dropContents(player.worldObj, x, y, z);
	}

}
