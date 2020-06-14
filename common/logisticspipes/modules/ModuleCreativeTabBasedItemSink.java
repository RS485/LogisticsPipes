package logisticspipes.modules;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import logisticspipes.gui.hud.modules.HUDStringBasedItemSink;
import logisticspipes.interfaces.IClientInformationProvider;
import logisticspipes.interfaces.IHUDModuleHandler;
import logisticspipes.interfaces.IHUDModuleRenderer;
import logisticspipes.interfaces.IModuleWatchReciver;
import logisticspipes.interfaces.IStringBasedModule;
import logisticspipes.network.NewGuiHandler;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractguis.ModuleCoordinatesGuiProvider;
import logisticspipes.network.abstractguis.ModuleInHandGuiProvider;
import logisticspipes.network.guis.module.inhand.StringBasedItemSinkModuleGuiInHand;
import logisticspipes.network.guis.module.inpipe.StringBasedItemSinkModuleGuiSlot;
import logisticspipes.network.packets.hud.HUDStartModuleWatchingPacket;
import logisticspipes.network.packets.hud.HUDStopModuleWatchingPacket;
import logisticspipes.network.packets.module.ModuleBasedItemSinkList;
import logisticspipes.pipes.PipeLogisticsChassi.ChassiTargetInformation;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.PlayerCollectionList;
import logisticspipes.utils.SinkReply;
import logisticspipes.utils.SinkReply.FixedPriority;
import logisticspipes.utils.item.ItemIdentifier;
import network.rs485.logisticspipes.module.Gui;

public class ModuleCreativeTabBasedItemSink extends LogisticsModule implements IStringBasedModule, IClientInformationProvider, IHUDModuleHandler, IModuleWatchReciver, Gui {

	public final List<String> tabList = new LinkedList<>();
	private final Set<String> tabSet = new HashSet<>();

	private IHUDModuleRenderer HUD = new HUDStringBasedItemSink(this);

	private final PlayerCollectionList localModeWatchers = new PlayerCollectionList();

	private SinkReply _sinkReply;

	public static String getName() {
		return "item_sink_creativetab";
	}

	@Override
	public void registerPosition(ModulePositionType slot, int positionInt) {
		super.registerPosition(slot, positionInt);
		_sinkReply = new SinkReply(FixedPriority.ModBasedItemSink, 0, true, false, 5, 0, new ChassiTargetInformation(getPositionInt()));
	}

	@Override
	public SinkReply sinksItem(@Nonnull ItemStack stack, ItemIdentifier item, int bestPriority, int bestCustomPriority, boolean allowDefault, boolean includeInTransit, boolean forcePassive) {
		if (bestPriority > _sinkReply.fixedPriority.ordinal() || (bestPriority == _sinkReply.fixedPriority.ordinal() && bestCustomPriority >= _sinkReply.customPriority)) {
			return null;
		}
		if (tabSet.isEmpty()) {
			tabSet.addAll(tabList);
		}
		if (tabSet.contains(item.getCreativeTabName())) {
			if (_service.canUseEnergy(5)) {
				return _sinkReply;
			}
		}
		return null;
	}

	@Override
	public void readFromNBT(@Nonnull NBTTagCompound nbttagcompound) {
		tabList.clear();
		int limit = nbttagcompound.getInteger("listSize");
		for (int i = 0; i < limit; i++) {
			tabList.add(nbttagcompound.getString("Mod" + i));
		}
		tabSet.clear();
		tabSet.addAll(tabList);
	}

	@Override
	public void writeToNBT(@Nonnull NBTTagCompound nbttagcompound) {
		nbttagcompound.setInteger("listSize", tabList.size());
		for (int i = 0; i < tabList.size(); i++) {
			nbttagcompound.setString("Mod" + i, tabList.get(i));
		}
	}

	@Override
	public void tick() {}

	@Override
	public @Nonnull List<String> getClientInformation() {
		List<String> list = new ArrayList<>();
		list.add("Mods: ");
		list.addAll(tabList);
		return list;
	}

	@Override
	public void startHUDWatching() {
		MainProxy.sendPacketToServer(PacketHandler.getPacket(HUDStartModuleWatchingPacket.class).setModulePos(this));
	}

	@Override
	public void stopHUDWatching() {
		MainProxy.sendPacketToServer(PacketHandler.getPacket(HUDStopModuleWatchingPacket.class).setModulePos(this));
	}

	@Override
	public void startWatching(EntityPlayer player) {
		localModeWatchers.add(player);
		NBTTagCompound nbt = new NBTTagCompound();
		writeToNBT(nbt);
		MainProxy.sendPacketToPlayer(PacketHandler.getPacket(ModuleBasedItemSinkList.class).setNbt(nbt).setModulePos(this), player);
	}

	@Override
	public void stopWatching(EntityPlayer player) {
		localModeWatchers.remove(player);
	}

	@Override
	public void listChanged() {
		if (MainProxy.isServer(_world.getWorld())) {
			NBTTagCompound nbt = new NBTTagCompound();
			writeToNBT(nbt);
			MainProxy.sendToPlayerList(PacketHandler.getPacket(ModuleBasedItemSinkList.class).setNbt(nbt).setModulePos(this), localModeWatchers);
		} else {
			NBTTagCompound nbt = new NBTTagCompound();
			writeToNBT(nbt);
			MainProxy.sendPacketToServer(PacketHandler.getPacket(ModuleBasedItemSinkList.class).setNbt(nbt).setModulePos(this));
		}
	}

	@Override
	public IHUDModuleRenderer getHUDRenderer() {
		return HUD;
	}

	@Override
	public boolean hasGenericInterests() {
		return true;
	}

	@Override
	public boolean interestedInAttachedInventory() {
		return false;
	}

	@Override
	public boolean interestedInUndamagedID() {
		return false;
	}

	@Override
	public boolean recievePassive() {
		return true;
	}

	@Override
	public List<String> getStringList() {
		return tabList;
	}

	@Override
	public String getStringForItem(ItemIdentifier ident) {
		return ident.getCreativeTabName();
	}

	@Nonnull
	@Override
	public ModuleCoordinatesGuiProvider getPipeGuiProvider() {
		NBTTagCompound nbt = new NBTTagCompound();
		writeToNBT(nbt);
		return NewGuiHandler.getGui(StringBasedItemSinkModuleGuiSlot.class).setNbt(nbt);
	}

	@Nonnull
	@Override
	public ModuleInHandGuiProvider getInHandGuiProvider() {
		return NewGuiHandler.getGui(StringBasedItemSinkModuleGuiInHand.class);
	}

}
