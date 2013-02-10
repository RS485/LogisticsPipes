package logisticspipes.blocks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import logisticspipes.LogisticsPipes;
import logisticspipes.interfaces.IGuiOpenControler;
import logisticspipes.items.LogisticsItemCard;
import logisticspipes.network.NetworkConstants;
import logisticspipes.network.packets.PacketCoordinatesUUID;
import logisticspipes.network.packets.PacketNBT;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.security.SecuritySettings;
import logisticspipes.utils.SimpleInventory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import cpw.mods.fml.common.network.Player;

public class LogisticsSecurityTileEntity extends TileEntity implements IGuiOpenControler {
	
	public SimpleInventory inv = new SimpleInventory(1, "ID Slots", 64);
	private List<EntityPlayer> listener = new ArrayList<EntityPlayer>();
	private UUID secId = null;
	private Map<String, SecuritySettings> settingsList = new HashMap<String, SecuritySettings>();
	
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
		MainProxy.sendPacketToPlayer(new PacketCoordinatesUUID(NetworkConstants.SECURITY_STATION_ID, xCoord, yCoord, zCoord, getSecId()).getPacket(), (Player) player);
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
	
	public void setClientUUID(UUID id) {
		if(MainProxy.isClient(worldObj)) {
			secId = id;
		}
	}
	
	@Override
	public void readFromNBT(NBTTagCompound par1nbtTagCompound) {
		super.readFromNBT(par1nbtTagCompound);
		if(par1nbtTagCompound.hasKey("UUID")) {
			secId = UUID.fromString(par1nbtTagCompound.getString("UUID"));
		}
		inv.readFromNBT(par1nbtTagCompound);
		settingsList.clear();
		NBTTagList list = par1nbtTagCompound.getTagList("settings");
		while(list.tagCount() > 0) {
			NBTBase base = list.removeTag(0);
			String name = ((NBTTagCompound)base).getString("name");
			NBTTagCompound value = ((NBTTagCompound)base).getCompoundTag("content");
			SecuritySettings settings = new SecuritySettings(name);
			settings.readFromNBT(value);
			settingsList.put(name, settings);
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound par1nbtTagCompound) {
		super.writeToNBT(par1nbtTagCompound);
		par1nbtTagCompound.setString("UUID", getSecId().toString());
		inv.writeToNBT(par1nbtTagCompound);
		NBTTagList list = new NBTTagList();
		for(Entry<String, SecuritySettings> entry:settingsList.entrySet()) {
			NBTTagCompound nbt = new NBTTagCompound();
			nbt.setString("name", entry.getKey());
			NBTTagCompound value = new NBTTagCompound();
			entry.getValue().writeToNBT(value);
			nbt.setCompoundTag("content", value);
			list.appendTag(nbt);
		}
		par1nbtTagCompound.setTag("settings", list);
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

	public void handleOpenSecurityPlayer(EntityPlayerMP player, String string) {
		SecuritySettings setting = settingsList.get(string);
		if(setting == null) {
			setting = new SecuritySettings(string);
			settingsList.put(string, setting);
		}
		NBTTagCompound nbt = new NBTTagCompound();
		setting.writeToNBT(nbt);
		MainProxy.sendPacketToPlayer(new PacketNBT(NetworkConstants.OPEN_SECURITY_PLAYER, nbt).getPacket(), (Player)player);
	}
}
