package logisticspipes.modules;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import logisticspipes.gui.hud.modules.HUDModBasedItemSink;
import logisticspipes.interfaces.IChassiePowerProvider;
import logisticspipes.interfaces.IClientInformationProvider;
import logisticspipes.interfaces.IHUDModuleHandler;
import logisticspipes.interfaces.IHUDModuleRenderer;
import logisticspipes.interfaces.ILogisticsGuiModule;
import logisticspipes.interfaces.ILogisticsModule;
import logisticspipes.interfaces.IModuleWatchReciver;
import logisticspipes.interfaces.ISendRoutedItem;
import logisticspipes.interfaces.IWorldProvider;
import logisticspipes.logisticspipes.IInventoryProvider;
import logisticspipes.network.GuiIDs;
import logisticspipes.network.NetworkConstants;
import logisticspipes.network.packets.PacketModuleNBT;
import logisticspipes.network.packets.PacketPipeInteger;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.ItemIdentifier;
import logisticspipes.utils.SinkReply;
import logisticspipes.utils.SinkReply.FixedPriority;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import cpw.mods.fml.common.network.Player;

public class ModuleModBasedItemSink implements ILogisticsGuiModule, IClientInformationProvider, IHUDModuleHandler, IModuleWatchReciver {
	
	public final List<String> modList = new LinkedList<String>();
	private int slot = 0;
	private int xCoord = 0;
	private int yCoord = 0;
	private int zCoord = 0;
	
	private IHUDModuleRenderer HUD = new HUDModBasedItemSink(this);
	
	private IChassiePowerProvider _power;
	
	private final List<EntityPlayer> localModeWatchers = new ArrayList<EntityPlayer>();
	
	@Override
	public void registerHandler(IInventoryProvider invProvider, ISendRoutedItem itemSender, IWorldProvider world, IChassiePowerProvider powerprovider) {
		_power = powerprovider;
	}

	@Override
	public void registerPosition(int xCoord, int yCoord, int zCoord, int slot) {
		this.xCoord = xCoord;
		this.yCoord = yCoord;
		this.zCoord = zCoord;
		this.slot = slot;
	}
	
	private static final SinkReply _sinkReply = new SinkReply(FixedPriority.ItemSink, -1, true, false, 5, 0);
	@Override
	public SinkReply sinksItem(ItemStack item, int bestPriority, int bestCustomPriority) {
		if(bestPriority > _sinkReply.fixedPriority.ordinal() || (bestPriority == _sinkReply.fixedPriority.ordinal() && bestCustomPriority >= _sinkReply.customPriority)) return null;
		ItemIdentifier ident = ItemIdentifier.get(item);
		if(modList.contains(ident.getModId())) {
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
	public ILogisticsModule getSubModule(int slot) {return null;}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		modList.clear();
		int limit = nbttagcompound.getInteger("listSize");
		for(int i = 0; i < limit; i++) {
			modList.add(nbttagcompound.getString("Mod" + i));
		}
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
		MainProxy.sendPacketToServer(new PacketPipeInteger(NetworkConstants.HUD_START_WATCHING_MODULE, xCoord, yCoord, zCoord, slot).getPacket());
	}

	@Override
	public void stopWatching() {
		MainProxy.sendPacketToServer(new PacketPipeInteger(NetworkConstants.HUD_START_WATCHING_MODULE, xCoord, yCoord, zCoord, slot).getPacket());
	}

	@Override
	public void startWatching(EntityPlayer player) {
		localModeWatchers.add(player);
		NBTTagCompound nbt = new NBTTagCompound();
		writeToNBT(nbt);
		MainProxy.sendPacketToPlayer(new PacketModuleNBT(NetworkConstants.MODBASEDITEMSINKLIST, xCoord, yCoord, zCoord, slot, nbt).getPacket(), (Player)player);
	}

	@Override
	public void stopWatching(EntityPlayer player) {
		localModeWatchers.remove(player);
	}
	
	public void ModListChanged() {
		if(MainProxy.isServer()) {
			NBTTagCompound nbt = new NBTTagCompound();
			writeToNBT(nbt);
			MainProxy.sendToPlayerList(new PacketModuleNBT(NetworkConstants.MODBASEDITEMSINKLIST, xCoord, yCoord, zCoord, slot, nbt).getPacket(), localModeWatchers);
		} else {
			NBTTagCompound nbt = new NBTTagCompound();
			writeToNBT(nbt);
			MainProxy.sendPacketToServer(new PacketModuleNBT(NetworkConstants.MODBASEDITEMSINKLIST, xCoord, yCoord, zCoord, slot, nbt).getPacket());	
		}
	}

	@Override
	public IHUDModuleRenderer getRenderer() {
		return HUD;
	}
}
