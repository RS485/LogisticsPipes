package logisticspipes.blocks;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.Direction;

import logisticspipes.LPConstants;
import logisticspipes.api.IRoutedPowerProvider;
import logisticspipes.interfaces.IGuiOpenController;
import logisticspipes.interfaces.IGuiTileEntity;
import logisticspipes.interfaces.ISecurityProvider;
import logisticspipes.network.NewGuiHandler;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractguis.CoordinatesGuiProvider;
import logisticspipes.network.guis.block.SecurityStationGui;
import logisticspipes.network.packets.block.SecurityStationAutoDestroy;
import logisticspipes.network.packets.block.SecurityStationId;
import logisticspipes.network.packets.block.SecurityStationOpenPlayer;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.security.SecuritySettings;
import logisticspipes.utils.OrientationsUtil;
import logisticspipes.utils.PlayerCollectionList;
import logisticspipes.utils.item.ItemIdentifierInventory;
import network.rs485.logisticspipes.init.Items;

public class LogisticsSecurityTileEntity extends LogisticsSolidTileEntity implements IGuiOpenController, ISecurityProvider, IGuiTileEntity {

	public ItemIdentifierInventory inv = new ItemIdentifierInventory(1, "ID Slots", 64);
	private PlayerCollectionList listener = new PlayerCollectionList();
	private UUID secId = null;
	private Map<String, SecuritySettings> settingsList = new HashMap<>();
	public boolean allowAutoDestroy = false;

	public static PlayerCollectionList byPassed = new PlayerCollectionList();
	public static final SecuritySettings allowAll = new SecuritySettings("");

	static {
		LogisticsSecurityTileEntity.allowAll.openGui = true;
		LogisticsSecurityTileEntity.allowAll.openRequest = true;
		LogisticsSecurityTileEntity.allowAll.openUpgrades = true;
		LogisticsSecurityTileEntity.allowAll.openNetworkMonitor = true;
		LogisticsSecurityTileEntity.allowAll.removePipes = true;
	}

	@Override
	public void invalidate() {
		super.invalidate();
		if (MainProxy.isServer(getWorld())) {
			SimpleServiceLocator.securityStationManager.remove(this);
		}
	}

	@Override
	public void validate() {
		super.validate();
		if (MainProxy.isServer(getWorld())) {
			SimpleServiceLocator.securityStationManager.add(this);
		}
	}

	@Override
	public void onChunkUnload() {
		super.onChunkUnload();
		if (MainProxy.isServer(getWorld())) {
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
	public void guiOpenedByPlayer(PlayerEntity player) {
		MainProxy.sendPacketToPlayer(PacketHandler.getPacket(SecurityStationAutoDestroy.class).setInteger(allowAutoDestroy ? 1 : 0).setBlockPos(pos), player);
		MainProxy.sendPacketToPlayer(PacketHandler.getPacket(SecurityStationId.class).setUuid(getSecId()).setBlockPos(pos), player);
		SimpleServiceLocator.securityStationManager.sendClientAuthorizationList();
		listener.add(player);
	}

	@Override
	public void guiClosedByPlayer(PlayerEntity player) {
		listener.remove(player);
	}

	public UUID getSecId() {
		if (!getWorld().isClient) {
			if (secId == null) {
				secId = UUID.randomUUID();
			}
		}
		return secId;
	}

	public void setClientUUID(UUID id) {
		secId = id;
	}

	public void setClientDestroy(boolean flag) {
		allowAutoDestroy = flag;
	}

	@Override
	public void fromTag(CompoundTag tag) {
		super.fromTag(tag);
		if (tag.containsKey("UUID")) {
			secId = UUID.fromString(tag.getString("UUID"));
		}
		allowAutoDestroy = tag.getBoolean("allowAutoDestroy");
		inv.readFromNBT(tag);
		settingsList.clear();
		ListTag list = tag.getList("settings", 10);
		while (list.size() > 0) {
			Tag base = list.remove(0);
			String name = ((CompoundTag) base).getString("name");
			CompoundTag value = ((CompoundTag) base).getCompound("content");
			SecuritySettings settings = new SecuritySettings(name);
			settings.readFromNBT(value);
			settingsList.put(name, settings);
		}
	}

	@Override
	public CompoundTag toTag(CompoundTag tag) {
		tag = super.toTag(tag);
		tag.putString("UUID", getSecId().toString());
		tag.putBoolean("allowAutoDestroy", allowAutoDestroy);
		inv.writeToNBT(tag);
		ListTag list = new ListTag();
		for (Entry<String, SecuritySettings> entry : settingsList.entrySet()) {
			CompoundTag nbt = new CompoundTag();
			nbt.putString("name", entry.getKey());
			CompoundTag value = new CompoundTag();
			entry.getValue().writeToNBT(value);
			nbt.put("content", value);
			list.add(nbt);
		}
		tag.put("settings", list);
		return tag;
	}

	public void buttonFreqCard(int integer, PlayerEntity player) {
		switch (integer) {
			case 0: //--
				inv.clearInventorySlotContents(0);
				break;
			case 1: //-
				inv.decrStackSize(0, 1);
				break;
			case 2: //+
				if (!useEnergy(10)) {
					player.sendMessage(new TranslatableText("lp.misc.noenergy"));
					return;
				}
				if (inv.getIDStackInSlot(0) == null) {
					ItemStack stack = new ItemStack(Items.INSTANCE.getSecurityCard());
					stack.setTag(new CompoundTag());
					stack.getTag().putUuid("UUID", getSecId());
					inv.setInventorySlotContents(0, stack);
				} else {
					ItemStack slot = inv.getStackInSlot(0);
					if (slot.getCount() < 64) {
						slot.increment(1);
						slot.setTag(new CompoundTag());
						slot.getTag().putUuid("UUID", getSecId());
						inv.setInventorySlotContents(0, slot);
					}
				}
				break;
			case 3: //++
				if (!useEnergy(640)) {
					player.sendMessage(new TranslatableText("lp.misc.noenergy"));
					return;
				}
				ItemStack stack = new ItemStack(Items.INSTANCE.getSecurityCard(), 64);
				stack.setTag(new CompoundTag());
				stack.getTag().putUuid("UUID", getSecId());
				inv.setInventorySlotContents(0, stack);
				break;
		}
	}

	public void handleOpenSecurityPlayer(PlayerEntity player, String string) {
		SecuritySettings setting = settingsList.get(string);
		if (setting == null && string != null && !string.isEmpty()) {
			setting = new SecuritySettings(string);
			settingsList.put(string, setting);
		}
		CompoundTag nbt = new CompoundTag();
		setting.writeToNBT(nbt);
		MainProxy.sendPacketToPlayer(PacketHandler.getPacket(SecurityStationOpenPlayer.class).setTag(nbt), player);
	}

	public void saveNewSecuritySettings(CompoundTag tag) {
		SecuritySettings setting = settingsList.get(tag.getString("name"));
		if (setting == null) {
			setting = new SecuritySettings(tag.getString("name"));
			settingsList.put(tag.getString("name"), setting);
		}
		setting.readFromNBT(tag);
	}

	public SecuritySettings getSecuritySettingsForPlayer(PlayerEntity entityplayer, boolean usePower) {
		if (LogisticsSecurityTileEntity.byPassed.contains(entityplayer)) {
			return LogisticsSecurityTileEntity.allowAll;
		}
		if (usePower && !useEnergy(10)) {
			entityplayer.sendMessage(new TranslatableText("lp.misc.noenergy"));
			return new SecuritySettings("No Energy");
		}
		SecuritySettings setting = settingsList.get(entityplayer.getDisplayName().asString());
		// TODO Change to GameProfile based Authentication
		if (setting == null) {
			setting = new SecuritySettings(entityplayer.getDisplayName().asString());
			settingsList.put(entityplayer.getDisplayName().asString(), setting);
		}
		return setting;
	}

	public void changeDestroy() {
		allowAutoDestroy = !allowAutoDestroy;
		MainProxy.sendToPlayerList(PacketHandler.getPacket(SecurityStationAutoDestroy.class).setInteger(allowAutoDestroy ? 1 : 0).setBlockPos(pos), listener);
	}

	@Override
	public boolean canAutomatedDestroy() {
		if (!useEnergy(10)) {
			return false;
		}
		return allowAutoDestroy;
	}

	private boolean useEnergy(int amount) {
		for (int i = 0; i < 4; i++) {
			BlockEntity tile = OrientationsUtil.getTileNextToThis(this, Direction.values()[i + 2]);
			if (tile instanceof IRoutedPowerProvider) {
				if (((IRoutedPowerProvider) tile).useEnergy(amount)) {
					return true;
				}
			}
			if (tile instanceof LogisticsTileGenericPipe) {
				if (((LogisticsTileGenericPipe) tile).pipe instanceof IRoutedPowerProvider) {
					if (((IRoutedPowerProvider) ((LogisticsTileGenericPipe) tile).pipe).useEnergy(amount)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	@Override
	public void populateCrashReport(CrashReportSection crashReportSection_1) {
		super.populateCrashReport(crashReportSection_1);
		crashReportSection_1.add("LP-Version", LPConstants.VERSION);
	}

	@Override
	public CoordinatesGuiProvider getGuiProvider() {
		return NewGuiHandler.getGui(SecurityStationGui.class);
	}
}
