package logisticspipes.pipes;

import java.util.UUID;

import net.minecraft.src.EntityItem;

import logisticspipes.config.Textures;
import logisticspipes.interfaces.ILogisticsModule;
import logisticspipes.logic.EntrencsLogic;
import logisticspipes.logic.TemporaryLogic;
import logisticspipes.main.RoutedPipe;
import logisticspipes.transport.EntrencsTransport;
import buildcraft.core.utils.SimpleInventory;

public class PipeItemsSystemEntranceLogistics extends RoutedPipe {
	
	public SimpleInventory inv = new SimpleInventory(1, "Freq Slot", 1);
	
	public PipeItemsSystemEntranceLogistics(int itemID) {
		super(new EntrencsTransport(), new EntrencsLogic(), itemID);
		((EntrencsTransport)this.transport).pipe = this;
	}
	
	public UUID getLocalFreqUUID() {
		if(inv.getStackInSlot(0) == null) return null;
		if(!inv.getStackInSlot(0).hasTagCompound()) return null;
		if(!inv.getStackInSlot(0).getTagCompound().hasKey("UUID")) return null;
		return UUID.fromString(inv.getStackInSlot(0).getTagCompound().getString("UUID"));
	}
	
	@Override
	public ItemSendMode getItemSendMode() {
		return ItemSendMode.Normal;
	}

	@Override
	public int getCenterTexture() {
		return Textures.LOGISTICSPIPE_ENTRANCE_TEXTURE;
	}

	@Override
	public ILogisticsModule getLogisticsModule() {
		return null;
	}

	@Override
	public void onBlockRemoval() {
		dropFreqCard();
	}

	private void dropFreqCard() {
		if(inv.getStackInSlot(0) == null) return;
		EntityItem item = new EntityItem(worldObj,this.xCoord, this.yCoord, this.zCoord, inv.getStackInSlot(0));
		worldObj.spawnEntityInWorld(item);
		inv.setInventorySlotContents(0, null);
	}
}
