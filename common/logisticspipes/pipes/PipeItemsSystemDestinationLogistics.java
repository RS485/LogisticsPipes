package logisticspipes.pipes;

import java.util.UUID;

import logisticspipes.LogisticsPipes;
import logisticspipes.modules.LogisticsModule;
import logisticspipes.network.GuiIDs;
import logisticspipes.pipefxhandlers.Particles;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.textures.Textures;
import logisticspipes.textures.Textures.TextureType;
import logisticspipes.utils.SimpleInventory;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

public class PipeItemsSystemDestinationLogistics extends CoreRoutedPipe {

	public SimpleInventory inv = new SimpleInventory(1, "Freq Slot", 1);
	
	public PipeItemsSystemDestinationLogistics(int itemID) {
		super(itemID);
	}

	@Override
	public ItemSendMode getItemSendMode() {
		return ItemSendMode.Normal;
	}

	@Override
	public TextureType getCenterTexture() {
		return Textures.LOGISTICSPIPE_DESTINATION_TEXTURE;
	}

	@Override
	public LogisticsModule getLogisticsModule() {
		return null;
	}

	public Object getTargetUUID() {
		if(inv.getStackInSlot(0) == null) return null;
		if(!inv.getStackInSlot(0).hasTagCompound()) return null;
		if(!inv.getStackInSlot(0).getTagCompound().hasKey("UUID")) return null;
		MainProxy.sendSpawnParticlePacket(Particles.WhiteParticle, getX(), getY(), getZ(), this.getWorld(), 2);
		return UUID.fromString(inv.getStackInSlot(0).getTagCompound().getString("UUID"));
	}

	@Override
	public void onAllowedRemoval() {
		dropFreqCard();
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		super.writeToNBT(nbttagcompound);
		inv.writeToNBT(nbttagcompound);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);
		inv.readFromNBT(nbttagcompound);
	}

	private void dropFreqCard() {
		if(inv.getStackInSlot(0) == null) return;
		EntityItem item = new EntityItem(getWorld(),this.getX(), this.getY(), this.getZ(), inv.getStackInSlot(0));
		getWorld().spawnEntityInWorld(item);
		inv.clearInventorySlotContents(0);
	}

	@Override
	public void onWrenchClicked(EntityPlayer entityplayer) {
		if (MainProxy.isServer(entityplayer.worldObj)) {
			entityplayer.openGui(LogisticsPipes.instance, GuiIDs.GUI_Freq_Card_ID, getWorld(), getX(), getY(), getZ());
		}
	}
}
