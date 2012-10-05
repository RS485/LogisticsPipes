package logisticspipes.pipes;

import logisticspipes.LogisticsPipes;
import logisticspipes.config.Textures;
import logisticspipes.network.GuiIDs;
import logisticspipes.proxy.MainProxy;
import net.minecraft.src.EntityItem;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.World;

public class PipeItemsRequestLogisticsMk2 extends PipeItemsRequestLogistics {
	
	private ItemStack disk;
	
	public PipeItemsRequestLogisticsMk2(int itemID) {
		super(itemID);
	}

	@Override
	public boolean blockActivated(World world, int i, int j, int k,	EntityPlayer entityplayer) {
		if (MainProxy.isServer(this.worldObj)) {
			openGui(entityplayer);
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
			entityplayer.openGui(LogisticsPipes.instance, GuiIDs.GUI_Normal_Mk2_Orderer_ID, this.worldObj, this.xCoord , this.yCoord, this.zCoord);
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
	public int getCenterTexture() {
		return Textures.LOGISTICSPIPE_REQUESTERMK2_TEXTURE;
	}
	
	public ItemStack getDisk() {
		return disk;
	}
	
	@Override
	public void onBlockRemoval() {
		super.onBlockRemoval();
		if(MainProxy.isServer(this.worldObj)) {
			this.dropDisk();
		}
	}
	
	public void dropDisk() {
		if(disk != null) {
			EntityItem item = new EntityItem(worldObj,this.xCoord, this.yCoord, this.zCoord, disk);
			worldObj.spawnEntityInWorld(item);
			disk = null;
		}
	}

	public void setDisk(ItemStack itemstack) {
		this.disk = itemstack;
	}
}
