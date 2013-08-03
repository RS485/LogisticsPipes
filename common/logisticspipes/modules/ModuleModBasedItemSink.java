package logisticspipes.modules;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.LinkedList;
import java.util.List;

import logisticspipes.api.IRoutedPowerProvider;
import logisticspipes.gui.hud.modules.HUDModBasedItemSink;
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
import logisticspipes.network.packets.module.ModuleBasedItemSinkList;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.ItemIdentifier;
import logisticspipes.utils.PlayerCollectionList;
import logisticspipes.utils.SinkReply;
import logisticspipes.utils.SinkReply.FixedPriority;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Icon;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ModuleModBasedItemSink extends LogisticsGuiModule implements IClientInformationProvider, IHUDModuleHandler, IModuleWatchReciver {
	
	public final List<String> modList = new LinkedList<String>();
	private BitSet modIdSet;
	private int slot = 0;



	
	private IHUDModuleRenderer HUD = new HUDModBasedItemSink(this);
	
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

	
	private static final SinkReply _sinkReply = new SinkReply(FixedPriority.ModBasedItemSink, 0, true, false, 5, 0);
	@Override
	public SinkReply sinksItem(ItemIdentifier item, int bestPriority, int bestCustomPriority, boolean allowDefault, boolean includeInTransit) {
		if(bestPriority > _sinkReply.fixedPriority.ordinal() || (bestPriority == _sinkReply.fixedPriority.ordinal() && bestCustomPriority >= _sinkReply.customPriority)) return null;
		if(modIdSet == null) {
			buildModIdSet();
		}
		if(modIdSet.get(item.getModId())) {
			if(_power.canUseEnergy(5)) {
				return _sinkReply;
			}
		}
		return null;
	}

	@Override
	public int getGuiHandlerID() {
		return GuiIDs.GUI_Module_ModBased_ItemSink_ID;
	}
	
	@Override
	public LogisticsModule getSubModule(int slot) {return null;}

	private void buildModIdSet() {
		modIdSet = new BitSet();
		for(String modname : modList) {
			int modid = ItemIdentifier.getModIdForName(modname);
			modIdSet.set(modid);
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		modList.clear();
		int limit = nbttagcompound.getInteger("listSize");
		for(int i = 0; i < limit; i++) {
			modList.add(nbttagcompound.getString("Mod" + i));
		}
		modIdSet = null;
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		nbttagcompound.setInteger("listSize", modList.size());
		for(int i = 0; i < modList.size(); i++) {
			nbttagcompound.setString("Mod" + i, modList.get(i));
		}
	}

	@Override
	public void tick() {}

	@Override
	public List<String> getClientInformation() {
		List<String> list = new ArrayList<String>();
		list.add("Mods: ");
		list.addAll(modList);
		return list;
	}

	@Override
	public void startWatching() {
//TODO 	MainProxy.sendPacketToServer(new PacketPipeInteger(NetworkConstants.HUD_START_WATCHING_MODULE, getX(), getY(), getZ(), slot).getPacket());
		MainProxy.sendPacketToServer(PacketHandler.getPacket(HUDStartModuleWatchingPacket.class).setInteger(slot).setPosX(getX()).setPosY(getY()).setPosZ(getZ()));
	}

	@Override
	public void stopWatching() {
//TODO 	MainProxy.sendPacketToServer(new PacketPipeInteger(NetworkConstants.HUD_START_WATCHING_MODULE, getX(), getY(), getZ(), slot).getPacket());
		MainProxy.sendPacketToServer(PacketHandler.getPacket(HUDStartModuleWatchingPacket.class).setInteger(slot).setPosX(getX()).setPosY(getY()).setPosZ(getZ()));
	}

	@Override
	public void startWatching(EntityPlayer player) {
		localModeWatchers.add(player);
		NBTTagCompound nbt = new NBTTagCompound();
		writeToNBT(nbt);
//TODO 	MainProxy.sendPacketToPlayer(new PacketModuleNBT(NetworkConstants.MODBASEDITEMSINKLIST, getX(), getY(), getZ(), slot, nbt).getPacket(), (Player)player);
		MainProxy.sendPacketToPlayer(PacketHandler.getPacket(ModuleBasedItemSinkList.class).setSlot(slot).setTag(nbt).setPosX(getX()).setPosY(getY()).setPosZ(getZ()), (Player)player);
	}

	@Override
	public void stopWatching(EntityPlayer player) {
		localModeWatchers.remove(player);
	}
	
	public void ModListChanged() {
		if(MainProxy.isServer(_world.getWorld())) {
			NBTTagCompound nbt = new NBTTagCompound();
			writeToNBT(nbt);
//TODO 		MainProxy.sendToPlayerList(new PacketModuleNBT(NetworkConstants.MODBASEDITEMSINKLIST, getX(), getY(), getZ(), slot, nbt).getPacket(), localModeWatchers);
			MainProxy.sendToPlayerList(PacketHandler.getPacket(ModuleBasedItemSinkList.class).setSlot(slot).setTag(nbt).setPosX(getX()).setPosY(getY()).setPosZ(getZ()), localModeWatchers);
		} else {
			NBTTagCompound nbt = new NBTTagCompound();
			writeToNBT(nbt);
//TODO 		MainProxy.sendPacketToServer(new PacketModuleNBT(NetworkConstants.MODBASEDITEMSINKLIST, getX(), getY(), getZ(), slot, nbt).getPacket());	
			MainProxy.sendPacketToServer(PacketHandler.getPacket(ModuleBasedItemSinkList.class).setSlot(slot).setTag(nbt).setPosX(getX()).setPosY(getY()).setPosZ(getZ()));
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
		return register.registerIcon("logisticspipes:itemModule/ModuleModBasedItemSink");
	}
}
