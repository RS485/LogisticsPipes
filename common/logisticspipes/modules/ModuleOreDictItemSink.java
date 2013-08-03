package logisticspipes.modules;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import logisticspipes.api.IRoutedPowerProvider;
import logisticspipes.gui.hud.modules.HUDOreDictItemSink;
import logisticspipes.interfaces.IClientInformationProvider;
import logisticspipes.interfaces.IHUDModuleHandler;
import logisticspipes.interfaces.IHUDModuleRenderer;
import logisticspipes.interfaces.IModuleWatchReciver;
import logisticspipes.interfaces.ISendRoutedItem;
import logisticspipes.interfaces.IWorldProvider;
import logisticspipes.logisticspipes.IInventoryProvider;
import logisticspipes.network.GuiIDs;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.hud.HUDStartModuleWatchingPacket;
import logisticspipes.network.packets.module.OreDictItemSinkList;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.ItemIdentifier;
import logisticspipes.utils.ItemIdentifierStack;
import logisticspipes.utils.PlayerCollectionList;
import logisticspipes.utils.SinkReply;
import logisticspipes.utils.SinkReply.FixedPriority;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Icon;
import net.minecraftforge.oredict.OreDictionary;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ModuleOreDictItemSink extends LogisticsGuiModule implements IClientInformationProvider, IHUDModuleHandler, IModuleWatchReciver {
	public final List<String> oreList = new LinkedList<String>();
	//map of ItemID:<set of damagevalues>, empty set if wildcard damage
	private Map<Integer, Set<Integer>> oreItemIdMap;
	private int slot = 0;

	private IHUDModuleRenderer HUD = new HUDOreDictItemSink(this);
	private List<ItemIdentifierStack> oreHudList;

	private IRoutedPowerProvider _power;
	private IWorldProvider _world;

	private final PlayerCollectionList localModeWatchers = new PlayerCollectionList();

	@Override
	public void registerHandler(IInventoryProvider invProvider, ISendRoutedItem itemSender, IWorldProvider world, IRoutedPowerProvider powerprovider) {
		_power = powerprovider;
		_world = world;
	}


	@Override
	public void registerSlot(int slot) {
		this.slot = slot;
	}

	@Override
	public final int getX() {
		if(slot>=0)
			return this._power.getX();
		else
			return 0;
	}

	@Override
	public final int getY() {
		if(slot>=0)
			return this._power.getY();
		else
			return -1;
	}

	@Override
	public final int getZ() {
		if(slot>=0)
			return this._power.getZ();
		else
			return -1-slot;
	}


	private static final SinkReply _sinkReply = new SinkReply(FixedPriority.OreDictItemSink, 0, true, false, 5, 0);
	@Override
	public SinkReply sinksItem(ItemIdentifier item, int bestPriority, int bestCustomPriority, boolean allowDefault, boolean includeInTransit) {
		if(bestPriority > _sinkReply.fixedPriority.ordinal() || (bestPriority == _sinkReply.fixedPriority.ordinal() && bestCustomPriority >= _sinkReply.customPriority)) return null;
		if(oreItemIdMap == null) {
			buildOreItemIdMap();
		}
		Set<Integer> damageSet = oreItemIdMap.get(item.itemID);
		if(damageSet == null)
			return null;
		if(damageSet.isEmpty() || damageSet.contains(item.itemDamage))
			return _sinkReply;
		return null;
	}

	@Override
	public int getGuiHandlerID() {
		return GuiIDs.GUI_Module_OreDict_ItemSink_ID;
	}

	public List<ItemIdentifierStack> getHudItemList() {
		if(oreItemIdMap == null) {
			buildOreItemIdMap();
		}
		return oreHudList;
	}

	@Override
	public LogisticsModule getSubModule(int slot) {return null;}

	private void buildOreItemIdMap() {
		oreItemIdMap = new TreeMap<Integer, Set<Integer>>();
		oreHudList = new ArrayList<ItemIdentifierStack>(oreList.size());
		for(String orename : oreList) {
			if(orename == null || orename.equals(""))
				continue;
			List<ItemStack> items = OreDictionary.getOres(orename);
			ItemStack stackForHud = null;
			for(ItemStack stack:items) {
				if(stack != null) {
					if(stackForHud == null)
						stackForHud = stack;
					if(stack.getItemDamage() == OreDictionary.WILDCARD_VALUE) {
						oreItemIdMap.put(stack.itemID, new TreeSet<Integer>());
					} else {
						Set<Integer> damageSet = oreItemIdMap.get(stack.itemID);
						if(damageSet == null) {
							damageSet = new TreeSet<Integer>();
							damageSet.add(stack.getItemDamage());
							oreItemIdMap.put(stack.itemID, damageSet);
						} else if (!damageSet.isEmpty()) {
							damageSet.add(stack.getItemDamage());
						}
					}
				}
			}
			if(stackForHud != null) {
				ItemStack t = stackForHud.copy();
				if(t.getItemDamage() == OreDictionary.WILDCARD_VALUE)
					t.setItemDamage(0);
				oreHudList.add(new ItemIdentifierStack(ItemIdentifier.get(t), 1));
			} else {
				oreHudList.add(new ItemIdentifierStack(ItemIdentifier.get(Block.fire.blockID, 0, null), 1));
			}
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		oreList.clear();
		int limit = nbttagcompound.getInteger("listSize");
		for(int i = 0; i < limit; i++) {
			oreList.add(nbttagcompound.getString("Ore" + i));
		}
		oreItemIdMap = null;
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		nbttagcompound.setInteger("listSize", oreList.size());
		for(int i = 0; i < oreList.size(); i++) {
			nbttagcompound.setString("Ore" + i, oreList.get(i));
		}
	}

	@Override
	public void tick() {}

	@Override
	public List<String> getClientInformation() {
		List<String> list = new ArrayList<String>();
		list.add("Ores: ");
		list.addAll(oreList);
		return list;
	}

	@Override
	public void startWatching() {
		MainProxy.sendPacketToServer(PacketHandler.getPacket(HUDStartModuleWatchingPacket.class).setInteger(slot).setPosX(getX()).setPosY(getY()).setPosZ(getZ()));
	}

	@Override
	public void stopWatching() {
		MainProxy.sendPacketToServer(PacketHandler.getPacket(HUDStartModuleWatchingPacket.class).setInteger(slot).setPosX(getX()).setPosY(getY()).setPosZ(getZ()));
	}

	@Override
	public void startWatching(EntityPlayer player) {
		localModeWatchers.add(player);
		NBTTagCompound nbt = new NBTTagCompound();
		writeToNBT(nbt);
		MainProxy.sendPacketToPlayer(PacketHandler.getPacket(OreDictItemSinkList.class).setSlot(slot).setTag(nbt).setPosX(getX()).setPosY(getY()).setPosZ(getZ()), (Player)player);
	}

	@Override
	public void stopWatching(EntityPlayer player) {
		localModeWatchers.remove(player);
	}

	public void OreListChanged() {
		if(MainProxy.isServer(_world.getWorld())) {
			NBTTagCompound nbt = new NBTTagCompound();
			writeToNBT(nbt);
			MainProxy.sendToPlayerList(PacketHandler.getPacket(OreDictItemSinkList.class).setSlot(slot).setTag(nbt).setPosX(getX()).setPosY(getY()).setPosZ(getZ()), localModeWatchers);
		} else {
			NBTTagCompound nbt = new NBTTagCompound();
			writeToNBT(nbt);
			MainProxy.sendPacketToServer(PacketHandler.getPacket(OreDictItemSinkList.class).setSlot(slot).setTag(nbt).setPosX(getX()).setPosY(getY()).setPosZ(getZ()));
		}
	}

	@Override
	public IHUDModuleRenderer getRenderer() {
		return HUD;
	}
	@Override
	public boolean hasGenericInterests() {
		return true;
	}

	@Override
	public List<ItemIdentifier> getSpecificInterests() {
		return null;
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
	@SideOnly(Side.CLIENT)
	public Icon getIconTexture(IconRegister register) {
		return register.registerIcon("logisticspipes:itemModule/ModuleOreDictItemSink");
	}
}
