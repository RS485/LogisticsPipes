package logisticspipes.modules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import javax.annotation.Nonnull;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import net.minecraftforge.oredict.OreDictionary;

import logisticspipes.gui.hud.modules.HUDOreDictItemSink;
import logisticspipes.interfaces.IClientInformationProvider;
import logisticspipes.interfaces.IHUDModuleHandler;
import logisticspipes.interfaces.IHUDModuleRenderer;
import logisticspipes.interfaces.IModuleWatchReciver;
import logisticspipes.network.NewGuiHandler;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractguis.ModuleCoordinatesGuiProvider;
import logisticspipes.network.abstractguis.ModuleInHandGuiProvider;
import logisticspipes.network.guis.module.inhand.OreDictItemSinkModuleInHand;
import logisticspipes.network.guis.module.inpipe.OreDictItemSinkModuleSlot;
import logisticspipes.network.packets.hud.HUDStartModuleWatchingPacket;
import logisticspipes.network.packets.hud.HUDStopModuleWatchingPacket;
import logisticspipes.network.packets.module.OreDictItemSinkList;
import logisticspipes.pipes.PipeLogisticsChassi.ChassiTargetInformation;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.PlayerCollectionList;
import logisticspipes.utils.SinkReply;
import logisticspipes.utils.SinkReply.FixedPriority;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.item.ItemIdentifierStack;
import network.rs485.logisticspipes.module.Gui;

public class ModuleOreDictItemSink extends LogisticsModule implements IClientInformationProvider, IHUDModuleHandler, IModuleWatchReciver, Gui {

	public final List<String> oreList = new LinkedList<>();
	//map of Item:<set of damagevalues>, empty set if wildcard damage
	private Map<Item, Set<Integer>> oreItemIdMap;

	private IHUDModuleRenderer HUD = new HUDOreDictItemSink(this);
	private List<ItemIdentifierStack> oreHudList;

	private final PlayerCollectionList localModeWatchers = new PlayerCollectionList();

	private SinkReply _sinkReply;

	public static String getName() {
		return "item_sink_oredict";
	}

	@Override
	public void registerPosition(ModulePositionType slot, int positionInt) {
		super.registerPosition(slot, positionInt);
		_sinkReply = new SinkReply(FixedPriority.OreDictItemSink, 0, true, false, 5, 0, new ChassiTargetInformation(getPositionInt()));
	}

	@Override
	public SinkReply sinksItem(@Nonnull ItemStack stack, ItemIdentifier item, int bestPriority, int bestCustomPriority, boolean allowDefault, boolean includeInTransit, boolean forcePassive) {
		if (bestPriority > _sinkReply.fixedPriority.ordinal() || (bestPriority == _sinkReply.fixedPriority.ordinal() && bestCustomPriority >= _sinkReply.customPriority)) {
			return null;
		}
		if (oreItemIdMap == null) {
			buildOreItemIdMap();
		}
		Set<Integer> damageSet = oreItemIdMap.get(item.item);
		if (damageSet == null) {
			return null;
		}
		if (damageSet.isEmpty() || damageSet.contains(item.itemDamage)) {
			return _sinkReply;
		}
		return null;
	}

	public List<ItemIdentifierStack> getHudItemList() {
		if (oreItemIdMap == null) {
			buildOreItemIdMap();
		}
		return oreHudList;
	}

	private void buildOreItemIdMap() {
		oreItemIdMap = new HashMap<>();
		oreHudList = new ArrayList<>(oreList.size());
		for (String orename : oreList) {
			List<ItemStack> items = OreDictionary.getOres(orename);
			ItemStack stackForHud = ItemStack.EMPTY;
			for (ItemStack stack : items) {
				if (stackForHud.isEmpty()) {
					stackForHud = stack;
				}
				if (stack.getItemDamage() == OreDictionary.WILDCARD_VALUE) {
					oreItemIdMap.put(stack.getItem(), new TreeSet<>());
				} else {
					Set<Integer> damageSet = oreItemIdMap.get(stack.getItem());
					if (damageSet == null) {
						damageSet = new TreeSet<>();
						damageSet.add(stack.getItemDamage());
						oreItemIdMap.put(stack.getItem(), damageSet);
					} else if (!damageSet.isEmpty()) {
						damageSet.add(stack.getItemDamage());
					}
				}
			}
			if (!stackForHud.isEmpty()) {
				ItemStack t = stackForHud.copy();
				if (t.getItemDamage() == OreDictionary.WILDCARD_VALUE) {
					t.setItemDamage(0);
				}
				oreHudList.add(new ItemIdentifierStack(ItemIdentifier.get(t), 1));
			} else {
				oreHudList.add(new ItemIdentifierStack(ItemIdentifier.get(Item.getItemFromBlock(Blocks.FIRE), 0, null), 1));
			}
		}
	}

	@Override
	public void readFromNBT(@Nonnull NBTTagCompound nbttagcompound) {
		oreList.clear();
		int limit = nbttagcompound.getInteger("listSize");
		for (int i = 0; i < limit; i++) {
			String oreName = nbttagcompound.getString("Ore" + i);
			if (!oreName.equals("")) {
				oreList.add(nbttagcompound.getString("Ore" + i));
			}
		}
		oreItemIdMap = null;
	}

	@Override
	public void writeToNBT(@Nonnull NBTTagCompound nbttagcompound) {
		nbttagcompound.setInteger("listSize", oreList.size());
		for (int i = 0; i < oreList.size(); i++) {
			nbttagcompound.setString("Ore" + i, oreList.get(i));
		}
	}

	@Override
	public void tick() {}

	@Override
	public @Nonnull List<String> getClientInformation() {
		List<String> list = new ArrayList<>();
		list.add("Ores: ");
		list.addAll(oreList);
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
		MainProxy.sendPacketToPlayer(PacketHandler.getPacket(OreDictItemSinkList.class).setTag(nbt).setModulePos(this), player);
	}

	@Override
	public void stopWatching(EntityPlayer player) {
		localModeWatchers.remove(player);
	}

	public void OreListChanged() {
		if (MainProxy.isServer(_world.getWorld())) {
			NBTTagCompound nbt = new NBTTagCompound();
			writeToNBT(nbt);
			MainProxy.sendToPlayerList(PacketHandler.getPacket(OreDictItemSinkList.class).setTag(nbt).setModulePos(this), localModeWatchers);
		} else {
			NBTTagCompound nbt = new NBTTagCompound();
			writeToNBT(nbt);
			MainProxy.sendPacketToServer(PacketHandler.getPacket(OreDictItemSinkList.class).setTag(nbt).setModulePos(this));
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

	@Nonnull
	@Override
	public ModuleCoordinatesGuiProvider getPipeGuiProvider() {
		NBTTagCompound nbt = new NBTTagCompound();
		writeToNBT(nbt);
		return NewGuiHandler.getGui(OreDictItemSinkModuleSlot.class).setNbt(nbt);
	}

	@Nonnull
	@Override
	public ModuleInHandGuiProvider getInHandGuiProvider() {
		return NewGuiHandler.getGui(OreDictItemSinkModuleInHand.class);
	}

}
