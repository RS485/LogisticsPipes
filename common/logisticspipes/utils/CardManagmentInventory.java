package logisticspipes.utils;

import javax.annotation.Nonnull;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;

import logisticspipes.items.ItemModule;
import logisticspipes.items.LogisticsItemCard;
import logisticspipes.utils.item.ItemIdentifierInventory;

public class CardManagmentInventory implements IInventory {

	ItemIdentifierInventory inv = new ItemIdentifierInventory(4, "", 1);

	@Override
	public int getSizeInventory() {
		return 10;
	}

	@Override
	public boolean isEmpty() {
		return inv.isEmpty();
	}

	@Nonnull
	@Override
	public ItemStack getStackInSlot(int i) {
		if (i > -1 && i < 4) {
			return inv.getStackInSlot(i);
		}
		ItemStack card = inv.getStackInSlot(3);
		if (!card.isEmpty()) {
			NBTTagCompound nbt = card.getTagCompound();
			if (nbt == null) {
				nbt = new NBTTagCompound();
			}
			NBTTagCompound colors = nbt.getCompoundTag("colors");
			int slot = i - 4;

			int colorCode;
			if (colors.hasKey("color:" + slot)) {
				colorCode = colors.getInteger("color:" + slot);
			} else {
				colors.setInteger("color:" + slot, 16);
				colorCode = 16;
			}

			MinecraftColor color = MinecraftColor.values()[colorCode];

			nbt.setTag("colors", colors);
			card.setTagCompound(nbt);
			inv.setInventorySlotContents(3, card);

			return color.getItemStack();
		}

		return ItemStack.EMPTY;
	}

	@Nonnull
	@Override
	public ItemStack decrStackSize(int i, int j) {
		if (i > -1 && i < 4) {
			return inv.decrStackSize(i, j);
		}
		return ItemStack.EMPTY;
	}

	@Nonnull
	@Override
	public ItemStack removeStackFromSlot(int i) {
		if (i > -1 && i < 4) {
			return inv.removeStackFromSlot(i);
		}
		return ItemStack.EMPTY;
	}

	@Override
	public void setInventorySlotContents(int i, @Nonnull ItemStack itemstack) {
		if (i > -1 && i < 4) {
			if (i == 0 && !itemstack.isEmpty() && !inv.getStackInSlot(1).isEmpty() && inv.getStackInSlot(2).isEmpty() && inv.getStackInSlot(1).getItemDamage() == itemstack.getItemDamage()) {
				itemstack.setTagCompound(inv.getStackInSlot(1).getTagCompound());
				inv.setInventorySlotContents(2, itemstack);
				return;
			}
			if (i == 1 && !itemstack.isEmpty() && !inv.getStackInSlot(0).isEmpty() && inv.getStackInSlot(2).isEmpty() && inv.getStackInSlot(0).getItemDamage() == itemstack.getItemDamage()) {
				itemstack.setTagCompound(inv.getStackInSlot(0).getTagCompound());
				inv.setInventorySlotContents(2, itemstack);
				return;
			}
			inv.setInventorySlotContents(i, itemstack);
			return;
		}
		ItemStack card = inv.getStackInSlot(3);
		if (!card.isEmpty()) {
			NBTTagCompound nbt = card.getTagCompound();
			if (nbt == null) {
				nbt = new NBTTagCompound();
			}
			NBTTagCompound colors = nbt.getCompoundTag("colors");
			int slot = i - 4;
			colors.setInteger("color:" + slot, MinecraftColor.getColor(itemstack).ordinal());
			nbt.setTag("colors", colors);
			card.setTagCompound(nbt);
			inv.setInventorySlotContents(3, card);
		}
	}

	@Nonnull
	@Override
	public String getName() {
		return "Card Managment Inventory";
	}

	@Nonnull
	@Override
	public ITextComponent getDisplayName() {
		return new TextComponentString("");
	}

	@Override
	public boolean hasCustomName() {
		return false;
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public boolean isUsableByPlayer(@Nonnull EntityPlayer entityplayer) {
		return true;
	}

	@Override
	public void markDirty() {}

	@Override
	public void openInventory(@Nonnull EntityPlayer player) {}

	@Override
	public void closeInventory(@Nonnull EntityPlayer player) {}

	@Override
	public boolean isItemValidForSlot(int i, @Nonnull ItemStack itemstack) {
		if (itemstack.isEmpty()) {
			return false;
		}
		if (i == 0 || i == 1) {
			return itemstack.getItem() instanceof ItemModule;
		} else if (i == 3) {
			return itemstack.getItem() instanceof LogisticsItemCard;
		}
		return false;
	}

	public void close(EntityPlayer player, int x, int y, int z) {
		inv.dropContents(player.world, x, y, z);
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
}
