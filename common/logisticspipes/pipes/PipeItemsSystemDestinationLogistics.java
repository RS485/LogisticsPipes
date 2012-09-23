package logisticspipes.pipes;

import java.util.UUID;

import net.minecraft.src.EntityItem;

import buildcraft.core.utils.SimpleInventory;
import logisticspipes.config.Textures;
import logisticspipes.interfaces.ILogisticsModule;
import logisticspipes.logic.BaseRoutingLogic;
import logisticspipes.logic.DestinationLogic;
import logisticspipes.logic.TemporaryLogic;
import logisticspipes.main.CoreRoutedPipe;
import logisticspipes.main.RoutedPipe;
import logisticspipes.main.SimpleServiceLocator;

public class PipeItemsSystemDestinationLogistics extends RoutedPipe {

	public SimpleInventory inv = new SimpleInventory(1, "Freq Slot", 1);
	
	public PipeItemsSystemDestinationLogistics(int itemID) {
		super(new DestinationLogic(), itemID);
	}

	@Override
	public ItemSendMode getItemSendMode() {
		return ItemSendMode.Normal;
	}

	@Override
	public int getCenterTexture() {
		return Textures.LOGISTICSPIPE_DESTINATION_TEXTURE;
	}

	@Override
	public ILogisticsModule getLogisticsModule() {
		return null;
	}

	public Object getTargetUUID() {
		if(inv.getStackInSlot(0) == null) return null;
		if(!inv.getStackInSlot(0).hasTagCompound()) return null;
		if(!inv.getStackInSlot(0).getTagCompound().hasKey("UUID")) return null;
		return UUID.fromString(inv.getStackInSlot(0).getTagCompound().getString("UUID"));
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
