package logisticspipes.blocks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import logisticspipes.LogisticsPipes;
import logisticspipes.api.IRoutedPowerProvider;
import logisticspipes.interfaces.IGuiOpenControler;
import logisticspipes.interfaces.ISecurityProvider;
import logisticspipes.items.LogisticsItemCard;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.block.SecurityStationAutoDestroy;
import logisticspipes.network.packets.block.SecurityStationCC;
import logisticspipes.network.packets.block.SecurityStationCCIDs;
import logisticspipes.network.packets.block.SecurityStationId;
import logisticspipes.network.packets.block.SecurityStationOpenPlayer;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.security.SecuritySettings;
import logisticspipes.utils.PlayerCollectionList;
import logisticspipes.utils.SimpleInventory;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.api.core.Position;
import buildcraft.transport.TileGenericPipe;
import cpw.mods.fml.common.network.Player;

public class LogisticsSecurityTileEntity extends TileEntity implements IGuiOpenControler, ISecurityProvider {
	
	public SimpleInventory inv = new SimpleInventory(1, "ID Slots", 64);
	private PlayerCollectionList listener = new PlayerCollectionList();
	private UUID secId = null;
	private Map<String, SecuritySettings> settingsList = new HashMap<String, SecuritySettings>();
	public List<Integer> excludedCC = new ArrayList<Integer>();
	public boolean allowCC = false;
	public boolean allowAutoDestroy = false;
	
	public static PlayerCollectionList byPassed = new PlayerCollectionList();
	public static final SecuritySettings allowAll = new SecuritySettings("");
	static {
		allowAll.openGui = true;
		allowAll.openRequest = true;
		allowAll.openUpgrades = true;
		allowAll.openNetworkMonitor = true;
		allowAll.removePipes = true;
	}
	
	@Override
	public void invalidate() {
		super.invalidate();
		if(MainProxy.isServer(this.getWorld())) {
			SimpleServiceLocator.securityStationManager.remove(this);
		}
	}

	@Override
	public void validate() {
		super.validate();
		if(MainProxy.isServer(this.getWorld())) {
			SimpleServiceLocator.securityStationManager.add(this);
		}
	}

	@Override
	public void onChunkUnload() {
		super.onChunkUnload();
		if(MainProxy.isServer(this.getWorld())) {
			SimpleServiceLocator.securityStationManager.remove(this);
		}
	}
	
	public void deauthorizeStation() {
		SimpleServiceLocator.securityStationManager.deauthorizeUUID(getSecId());
	}
	
	public void authorizeStation() {
		SimpleServiceLocator.securityStationManager.authorizeUUID(getSecId());
	}

	@Override
	public void guiOpenedByPlayer(EntityPlayer player) {
//TODO 	MainProxy.sendPacketToPlayer(new PacketPipeInteger(NetworkConstants.SET_SECURITY_CC, xCoord, yCoord, zCoord, allowCC?1:0).getPacket(), (Player) player);
		MainProxy.sendPacketToPlayer(PacketHandler.getPacket(SecurityStationCC.class).setInteger(allowCC?1:0).setPosX(xCoord).setPosY(yCoord).setPosZ(zCoord), (Player) player);
//TODO 	MainProxy.sendPacketToPlayer(new PacketPipeInteger(NetworkConstants.SET_SECURITY_DESTROY, xCoord, yCoord, zCoord, allowAutoDestroy?1:0).getPacket(), (Player) player);
		MainProxy.sendPacketToPlayer(PacketHandler.getPacket(SecurityStationAutoDestroy.class).setInteger(allowAutoDestroy?1:0).setPosX(xCoord).setPosY(yCoord).setPosZ(zCoord), (Player) player);
//TODO 	MainProxy.sendPacketToPlayer(new PacketCoordinatesUUID(NetworkConstants.SECURITY_STATION_ID, xCoord, yCoord, zCoord, getSecId()).getPacket(), (Player) player);
		MainProxy.sendPacketToPlayer(PacketHandler.getPacket(SecurityStationId.class).setUuid(getSecId()).setPosX(xCoord).setPosY(yCoord).setPosZ(zCoord), (Player) player);
		SimpleServiceLocator.securityStationManager.sendClientAuthorizationList();
		listener.add(player);
	}

	@Override
	public void guiClosedByPlayer(EntityPlayer player) {
		listener.remove(player);
	}

	public UUID getSecId() {
		if(MainProxy.isServer(getWorld())) {
			if(secId == null) {
				secId = UUID.randomUUID();
			}
		}
		return secId;
	}
	
	public void setClientUUID(UUID id) {
		if(MainProxy.isClient(getWorld())) {
			secId = id;
		}
	}

	public void setClientCC(boolean flag) {
		if(MainProxy.isClient(getWorld())) {
			allowCC = flag;
		}
	}

	public void setClientDestroy(boolean flag) {
		if(MainProxy.isClient(getWorld())) {
			allowAutoDestroy = flag;
		}
	}
	
	@Override
	public void readFromNBT(NBTTagCompound par1nbtTagCompound) {
		super.readFromNBT(par1nbtTagCompound);
		if(par1nbtTagCompound.hasKey("UUID")) {
			secId = UUID.fromString(par1nbtTagCompound.getString("UUID"));
		}
		allowCC = par1nbtTagCompound.getBoolean("allowCC");
		allowAutoDestroy = par1nbtTagCompound.getBoolean("allowAutoDestroy");
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
		excludedCC.clear();
		list = par1nbtTagCompound.getTagList("excludedCC");
		while(list.tagCount() > 0) {
			NBTBase base = list.removeTag(0);
			excludedCC.add(((NBTTagInt)base).data);
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound par1nbtTagCompound) {
		super.writeToNBT(par1nbtTagCompound);
		par1nbtTagCompound.setString("UUID", getSecId().toString());
		par1nbtTagCompound.setBoolean("allowCC", allowCC);
		par1nbtTagCompound.setBoolean("allowAutoDestroy", allowAutoDestroy);
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
		list = new NBTTagList();
		int count = 0;
		for(Integer i:excludedCC) {
			list.appendTag(new NBTTagInt("Part: " + count++, i));
		}
		par1nbtTagCompound.setTag("excludedCC", list);
	}

	public void buttonFreqCard(int integer, EntityPlayer player) {
		switch(integer) {
		case 0: //--
			inv.clearInventorySlotContents(0);
			break;
		case 1: //-
			inv.decrStackSize(0, 1);
			break;
		case 2: //+
			if(!useEnergy(10)) {
				player.sendChatToPlayer(ChatMessageComponent.func_111066_d("No Energy"));
				return;
			}
			if(inv.getStackInSlot(0) == null) {
				ItemStack stack = new ItemStack(LogisticsPipes.LogisticsItemCard, 1, LogisticsItemCard.SEC_CARD);
				stack.setTagCompound(new NBTTagCompound("tag"));
				stack.getTagCompound().setString("UUID", getSecId().toString());
				inv.setInventorySlotContents(0, stack);
			} else {
				ItemStack slot=inv.getStackInSlot(0);
				if(slot.stackSize < 64) {
					slot.stackSize++;
					slot.setTagCompound(new NBTTagCompound("tag"));
					slot.getTagCompound().setString("UUID", getSecId().toString());
					inv.setInventorySlotContents(0, slot);
				}
			}
			break;
		case 3: //++
			if(!useEnergy(640)) {
				player.sendChatToPlayer(ChatMessageComponent.func_111066_d("No Energy"));
				return;
			}
			ItemStack stack = new ItemStack(LogisticsPipes.LogisticsItemCard, 64, LogisticsItemCard.SEC_CARD);
			stack.setTagCompound(new NBTTagCompound("tag"));
			stack.getTagCompound().setString("UUID", getSecId().toString());
			inv.setInventorySlotContents(0, stack);
			break;
		}
	}

	public void handleOpenSecurityPlayer(EntityPlayer player, String string) {
		SecuritySettings setting = settingsList.get(string);
		if(setting == null && string != "" && string != null) {
			setting = new SecuritySettings(string);
			settingsList.put(string, setting);
		}
		NBTTagCompound nbt = new NBTTagCompound();
		setting.writeToNBT(nbt);
//TODO 	MainProxy.sendPacketToPlayer(new PacketNBT(NetworkConstants.OPEN_SECURITY_PLAYER, nbt).getPacket(), (Player)player);
		MainProxy.sendPacketToPlayer(PacketHandler.getPacket(SecurityStationOpenPlayer.class).setTag(nbt), (Player)player);
	}

	public void saveNewSecuritySettings(NBTTagCompound tag) {
		SecuritySettings setting = settingsList.get(tag.getString("name"));
		if(setting == null) {
			setting = new SecuritySettings(tag.getString("name"));
			settingsList.put(tag.getString("name"), setting);
		}
		setting.readFromNBT(tag);
	}

	public SecuritySettings getSecuritySettingsForPlayer(EntityPlayer entityplayer, boolean usePower) {
		if(byPassed.contains(entityplayer)) return allowAll;
		if(usePower && !useEnergy(10)) {
			entityplayer.sendChatToPlayer(ChatMessageComponent.func_111066_d("No Energy"));
			return new SecuritySettings("No Energy");
		}
		SecuritySettings setting = settingsList.get(entityplayer.username);
		if(setting == null) {
			setting = new SecuritySettings(entityplayer.username);
			settingsList.put(entityplayer.username, setting);
		}
		return setting;
	}

	public void changeCC() {
		allowCC = !allowCC;
//TODO 	MainProxy.sendToPlayerList(new PacketPipeInteger(NetworkConstants.SET_SECURITY_CC, xCoord, yCoord, zCoord, allowCC?1:0).getPacket(), listener);
		MainProxy.sendToPlayerList(PacketHandler.getPacket(SecurityStationCC.class).setInteger(allowCC?1:0).setPosX(xCoord).setPosY(yCoord).setPosZ(zCoord), listener);
	}

	public void changeDestroy() {
		allowAutoDestroy = !allowAutoDestroy;
//TODO 	MainProxy.sendToPlayerList(new PacketPipeInteger(NetworkConstants.SET_SECURITY_DESTROY, xCoord, yCoord, zCoord, allowAutoDestroy?1:0).getPacket(), listener);
		MainProxy.sendToPlayerList(PacketHandler.getPacket(SecurityStationAutoDestroy.class).setInteger(allowAutoDestroy?1:0).setPosX(xCoord).setPosY(yCoord).setPosZ(zCoord), listener);
	}
	
	public void addCCToList(Integer id) {
		if(!excludedCC.contains(id)) {
			excludedCC.add(id);
		}
		Collections.sort(excludedCC);
	}
	
	public void removeCCFromList(Integer id) {
		excludedCC.remove(id);
	}

	public void requestList(EntityPlayer player) {
		NBTTagCompound tag = new NBTTagCompound();
		NBTTagList list = new NBTTagList();
		for(Integer i:excludedCC) {
			list.appendTag(new NBTTagInt("" + i, i));
		}
		tag.setTag("list", list);
//TODO 	MainProxy.sendPacketToPlayer(new PacketNBT(NetworkConstants.SEND_CC_IDS, xCoord, yCoord, zCoord, tag).getPacket(), (Player)player);
		MainProxy.sendPacketToPlayer(PacketHandler.getPacket(SecurityStationCCIDs.class).setTag(tag).setPosX(xCoord).setPosY(yCoord).setPosZ(zCoord), (Player)player);
	}

	public void handleListPacket(NBTTagCompound tag) {
		excludedCC.clear();
		NBTTagList list = tag.getTagList("list");
		while(list.tagCount() > 0) {
			NBTBase base = list.removeTag(0);
			excludedCC.add(((NBTTagInt)base).data);
		}
	}

	@Override
	public boolean getAllowCC(int id) {
		if(!useEnergy(10)) return false;
		return allowCC != excludedCC.contains(id);
	}

	@Override
	public boolean canAutomatedDestroy() {
		if(!useEnergy(10)) return false;
		return allowAutoDestroy;
	}
	
	private boolean useEnergy(int amount) {
		for(int i=0;i<4;i++) {
			Position pos = new Position(this);
			pos.orientation = ForgeDirection.VALID_DIRECTIONS[i + 2];
			pos.moveForwards(1);
			TileEntity tile = this.getWorld().getBlockTileEntity((int)pos.x, (int)pos.y, (int)pos.z);
			if(tile instanceof IRoutedPowerProvider) {
				if(((IRoutedPowerProvider)tile).useEnergy(amount)) {
					return true;
				}
			}
			if(tile instanceof TileGenericPipe) {
				if(((TileGenericPipe)tile).pipe instanceof IRoutedPowerProvider) {
					if(((IRoutedPowerProvider)((TileGenericPipe)tile).pipe).useEnergy(amount)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	@Override
	public void func_85027_a(CrashReportCategory par1CrashReportCategory) {
		super.func_85027_a(par1CrashReportCategory);
		par1CrashReportCategory.addCrashSection("LP-Version", LogisticsPipes.VERSION);
	}
	
	public World getWorld() {
		return this.getWorldObj();
	
	}
}
