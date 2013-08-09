package logisticspipes.pipes;

import logisticspipes.LogisticsPipes;
import logisticspipes.network.GuiIDs;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.security.SecuritySettings;
import logisticspipes.textures.Textures;
import logisticspipes.textures.Textures.TextureType;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatMessageComponent;

public class PipeItemsRequestLogisticsMk2 extends PipeItemsRequestLogistics {
	
	private ItemStack disk;
	
	public PipeItemsRequestLogisticsMk2(int itemID) {
		super(itemID);
	}

	@Override
	public boolean handleClick(EntityPlayer entityplayer, SecuritySettings settings) {
		//allow using upgrade manager
		if(SimpleServiceLocator.buildCraftProxy.isUpgradeManagerEquipped(entityplayer) && !(entityplayer.isSneaking())) {
			return false;
		}
		if(MainProxy.isServer(getWorld())) {
			if(settings == null || settings.openGui) {
				openGui(entityplayer);
			} else {
				entityplayer.sendChatToPlayer(ChatMessageComponent.func_111066_d("Permission denied"));
			}
		}
		return true;
	}

	@Override
	public void openGui(EntityPlayer entityplayer) {
		boolean flag = true;
		if(disk == null) {
			if(entityplayer.getCurrentEquippedItem() != null && entityplayer.getCurrentEquippedItem().getItem().equals(LogisticsPipes.LogisticsItemDisk)) {
				disk = entityplayer.getCurrentEquippedItem();
				entityplayer.destroyCurrentEquippedItem();
				flag = false;
			}
		}
		if(flag) {
			entityplayer.openGui(LogisticsPipes.instance, GuiIDs.GUI_Normal_Mk2_Orderer_ID, this.getWorld(), this.getX() , this.getY(), this.getZ());
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		super.writeToNBT(nbttagcompound);
		if(disk != null) {
			NBTTagCompound itemNBT = new NBTTagCompound();
			disk.writeToNBT(itemNBT);
			nbttagcompound.setCompoundTag("Disk", itemNBT);
		}
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);
		if(nbttagcompound.hasKey("Disk")) {
			NBTTagCompound item = nbttagcompound.getCompoundTag("Disk");
			disk = new ItemStack(LogisticsPipes.LogisticsItemDisk,1);
			disk.readFromNBT(item);
			if(disk.itemID == 0) {
				disk = null;
			}
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
		if(MainProxy.isServer(this.getWorld())) {
			this.dropDisk();
		}
	}
	
	public void dropDisk() {
		if(disk != null) {
			EntityItem item = new EntityItem(getWorld(),this.getX(), this.getY(), this.getZ(), disk);
			getWorld().spawnEntityInWorld(item);
			disk = null;
		}
	}

	public void setDisk(ItemStack itemstack) {
		this.disk = itemstack;
	}
}
