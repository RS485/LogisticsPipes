package logisticspipes.pipes;

import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
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

	private ItemStack disk;

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
		if (disk == null) {
			if (entityplayer.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND) != null && entityplayer.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND).getItem().equals(LPItems.disk)) {
				disk = entityplayer.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND);
				entityplayer.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, null);
				flag = false;
			}
		}
		if (flag) {
			entityplayer.openGui(LogisticsPipes.instance, GuiIDs.GUI_Normal_Mk2_Orderer_ID, getWorld(), getX(), getY(), getZ());
		}
	}

	@Override
	public void writeToNBT(CompoundTag nbttagcompound) {
		super.writeToNBT(nbttagcompound);
		if (disk != null) {
			CompoundTag itemNBT = new CompoundTag();
			disk.writeToNBT(itemNBT);
			nbttagcompound.setTag("Disk", itemNBT);
		}
	}

	@Override
	public void readFromNBT(CompoundTag nbttagcompound) {
		super.readFromNBT(nbttagcompound);
		if (nbttagcompound.hasKey("Disk")) {
			CompoundTag item = nbttagcompound.getCompoundTag("Disk");
			disk = ItemStackLoader.loadAndFixItemStackFromNBT(item);
		}
	}

	@Override
	public TextureType getCenterTexture() {
		return Textures.LOGISTICSPIPE_REQUESTERMK2_TEXTURE;
	}

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
		if (disk != null) {
			ItemEntity item = new ItemEntity(getWorld(), getX(), getY(), getZ(), disk);
			getWorld().spawnEntity(item);
			disk = null;
		}
	}

	public void setDisk(ItemStack itemstack) {
		disk = itemstack;
	}
}
