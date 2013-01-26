package logisticspipes.blocks;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import logisticspipes.LogisticsPipes;
import logisticspipes.interfaces.IGuiOpenControler;
import logisticspipes.items.LogisticsItemCard;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.utils.SimpleInventory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

public class LogisticsSecurityTileEntity extends TileEntity implements IGuiOpenControler {
	
	public SimpleInventory inv = new SimpleInventory(1, "ID Slots", 64);
	private List<EntityPlayer> listener = new ArrayList<EntityPlayer>();
	private UUID secId = null;
	
	public LogisticsSecurityTileEntity() {
		if(MainProxy.isServer()) {
			SimpleServiceLocator.securityStationManager.add(this);
		}
	}
	
	@Override
	public void invalidate() {
		super.invalidate();
		if(MainProxy.isServer()) {
			SimpleServiceLocator.securityStationManager.remove(this);
		}
	}

	@Override
	public void validate() {
		super.validate();
		if(MainProxy.isServer()) {
			SimpleServiceLocator.securityStationManager.add(this);
		}
	}

	@Override
	public void onChunkUnload() {
		super.onChunkUnload();
		if(MainProxy.isServer()) {
			SimpleServiceLocator.securityStationManager.remove(this);
		}
	}

	@Override
	public void guiOpenedByPlayer(EntityPlayer player) {
		listener.add(player);
	}

	@Override
	public void guiClosedByPlayer(EntityPlayer player) {
		listener.remove(player);
	}

	public UUID getSecId() {
		if(secId == null) {
			secId = UUID.randomUUID();
		}
		return secId;
	}

	@Override
	public void readFromNBT(NBTTagCompound par1nbtTagCompound) {
		super.readFromNBT(par1nbtTagCompound);
		if(par1nbtTagCompound.hasKey("UUID")) {
			secId = UUID.fromString(par1nbtTagCompound.getString("UUID"));
		}
		inv.readFromNBT(par1nbtTagCompound);
	}

	@Override
	public void writeToNBT(NBTTagCompound par1nbtTagCompound) {
		super.writeToNBT(par1nbtTagCompound);
		par1nbtTagCompound.setString("UUID", getSecId().toString());
		inv.writeToNBT(par1nbtTagCompound);
	}

	public void buttonFreqCard(int integer) {
		switch(integer) {
		case 0: //--
			inv.setInventorySlotContents(0, null);
			break;
		case 1: //-
			if(inv.getStackInSlot(0) == null) return;
			inv.getStackInSlot(0).stackSize--;
			if(inv.getStackInSlot(0).stackSize <= 0) {
				inv.setInventorySlotContents(0, null);
			}
			break;
		case 2: //+
			if(inv.getStackInSlot(0) == null) {
				ItemStack stack = new ItemStack(LogisticsPipes.LogisticsItemCard, 1, LogisticsItemCard.SEC_CARD);
				stack.setTagCompound(new NBTTagCompound("tag"));
				stack.getTagCompound().setString("UUID", getSecId().toString());
				inv.setInventorySlotContents(0, stack);
			} else {
				if(inv.getStackInSlot(0).stackSize < 64) {
					inv.getStackInSlot(0).stackSize++;
					inv.getStackInSlot(0).setTagCompound(new NBTTagCompound("tag"));
					inv.getStackInSlot(0).getTagCompound().setString("UUID", getSecId().toString());
				}
			}
			break;
		case 3: //++
			ItemStack stack = new ItemStack(LogisticsPipes.LogisticsItemCard, 64, LogisticsItemCard.SEC_CARD);
			stack.setTagCompound(new NBTTagCompound("tag"));
			stack.getTagCompound().setString("UUID", getSecId().toString());
			inv.setInventorySlotContents(0, stack);
			break;
		}
	}
}
