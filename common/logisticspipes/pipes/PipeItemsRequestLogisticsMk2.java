package logisticspipes.pipes;

import javax.annotation.Nonnull;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentTranslation;

import logisticspipes.LPItems;
import logisticspipes.LogisticsPipes;
import logisticspipes.network.GuiIDs;
import logisticspipes.proxy.MainProxy;
import logisticspipes.security.SecuritySettings;
import logisticspipes.textures.Textures;
import logisticspipes.textures.Textures.TextureType;
import network.rs485.logisticspipes.util.items.ItemStackLoader;

public class PipeItemsRequestLogisticsMk2 extends PipeItemsRequestLogistics {

	@Nonnull
	private ItemStack disk = ItemStack.EMPTY;

	public PipeItemsRequestLogisticsMk2(Item item) {
		super(item);
	}

	@Override
	public boolean handleClick(EntityPlayer entityplayer, SecuritySettings settings) {
		//allow using upgrade manager
		if (MainProxy.isPipeControllerEquipped(entityplayer) && !(entityplayer.isSneaking())) {
			return false;
		}
		if (MainProxy.isServer(getWorld())) {
			if (settings == null || settings.openGui) {
				openGui(entityplayer);
			} else {
				entityplayer.sendMessage(new TextComponentTranslation("lp.chat.permissiondenied"));
			}
		}
		return true;
	}

	@Override
	public void openGui(EntityPlayer entityplayer) {
		boolean flag = true;
		if (disk.isEmpty()) {
			if (!entityplayer.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND).isEmpty() && entityplayer.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND).getItem().equals(LPItems.disk)) {
				disk = entityplayer.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND);
				entityplayer.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, ItemStack.EMPTY);
				flag = false;
			}
		}
		if (flag) {
			entityplayer.openGui(LogisticsPipes.instance, GuiIDs.GUI_Normal_Mk2_Orderer_ID, getWorld(), getX(), getY(), getZ());
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		super.writeToNBT(nbttagcompound);
		if (!disk.isEmpty()) {
			NBTTagCompound itemNBT = new NBTTagCompound();
			disk.writeToNBT(itemNBT);
			nbttagcompound.setTag("Disk", itemNBT);
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);
		if (nbttagcompound.hasKey("Disk")) {
			NBTTagCompound item = nbttagcompound.getCompoundTag("Disk");
			disk = ItemStackLoader.loadAndFixItemStackFromNBT(item);
		}
	}

	@Override
	public TextureType getCenterTexture() {
		return Textures.LOGISTICSPIPE_REQUESTERMK2_TEXTURE;
	}

	@Nonnull
	public ItemStack getDisk() {
		return disk;
	}

	@Override
	public void onAllowedRemoval() {
		if (MainProxy.isServer(getWorld())) {
			dropDisk();
		}
	}

	public void dropDisk() {
		if (!disk.isEmpty()) {
			EntityItem item = new EntityItem(getWorld(), getX(), getY(), getZ(), disk);
			getWorld().spawnEntity(item);
			disk = ItemStack.EMPTY;
		}
	}

	public void setDisk(@Nonnull ItemStack itemstack) {
		disk = itemstack;
	}
}
