package logisticspipes.pipes;

import logisticspipes.LogisticsPipes;
import logisticspipes.network.GuiIDs;
import logisticspipes.proxy.MainProxy;
import logisticspipes.security.SecuritySettings;
import logisticspipes.textures.Textures;
import logisticspipes.textures.Textures.TextureType;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentTranslation;

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
				entityplayer.addChatComponentMessage(new ChatComponentTranslation("lp.chat.permissiondenied"));
			}
		}
		return true;
	}

	@Override
	public void openGui(EntityPlayer entityplayer) {
		boolean flag = true;
		if (disk == null) {
			if (entityplayer.getCurrentEquippedItem() != null && entityplayer.getCurrentEquippedItem().getItem().equals(LogisticsPipes.LogisticsItemDisk)) {
				disk = entityplayer.getCurrentEquippedItem();
				entityplayer.destroyCurrentEquippedItem();
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
		if (disk != null) {
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
			disk = new ItemStack(LogisticsPipes.LogisticsItemDisk, 1);
			disk.readFromNBT(item);
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
			EntityItem item = new EntityItem(getWorld(), getX(), getY(), getZ(), disk);
			getWorld().spawnEntityInWorld(item);
			disk = null;
		}
	}

	public void setDisk(ItemStack itemstack) {
		disk = itemstack;
	}
}
