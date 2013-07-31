package logisticspipes.modules;

import java.util.ArrayList;
import java.util.List;

import logisticspipes.api.IRoutedPowerProvider;
import logisticspipes.interfaces.IClientInformationProvider;
import logisticspipes.interfaces.IInventoryUtil;
import logisticspipes.interfaces.IModuleWatchReciver;
import logisticspipes.interfaces.ISendRoutedItem;
import logisticspipes.interfaces.IWorldProvider;
import logisticspipes.interfaces.routing.IFilter;
import logisticspipes.logisticspipes.IInventoryProvider;
import logisticspipes.network.GuiIDs;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.module.ApiaristAnalyserMode;
import logisticspipes.pipes.basic.CoreRoutedPipe.ItemSendMode;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.utils.ItemIdentifier;
import logisticspipes.utils.Pair3;
import logisticspipes.utils.PlayerCollectionList;
import logisticspipes.utils.SinkReply;
import logisticspipes.utils.SinkReply.FixedPriority;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Icon;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
public class ModuleApiaristAnalyser extends LogisticsGuiModule implements IClientInformationProvider, IModuleWatchReciver {

	private IInventoryProvider _invProvider;
	private ISendRoutedItem _itemSender;
	private int ticksToAction = 100;
	private int currentTick = 0;
	private int slot = 0;

	private final PlayerCollectionList localModeWatchers = new PlayerCollectionList();

	private IRoutedPowerProvider _power;
	private IWorldProvider _world;

	public boolean extractMode = true;

	public ModuleApiaristAnalyser() {

	}

	@Override
	public void registerHandler(IInventoryProvider invProvider, ISendRoutedItem itemSender, IWorldProvider world, IRoutedPowerProvider powerprovider) {
		_invProvider = invProvider;
		_itemSender = itemSender;
		_power = powerprovider;
		_world = world;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		extractMode = nbt.getBoolean("extractMode");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		nbt.setBoolean("extractMode", extractMode);
	}

	private static final SinkReply _sinkReply = new SinkReply(FixedPriority.APIARIST_Analyser, 0, true, false, 3, 0);
	@Override
	public SinkReply sinksItem(ItemIdentifier itemID, int bestPriority, int bestCustomPriority, boolean allowDefault, boolean includeInTransit) {
		if(bestPriority > _sinkReply.fixedPriority.ordinal() || (bestPriority == _sinkReply.fixedPriority.ordinal() && bestCustomPriority >= _sinkReply.customPriority)) return null;
		ItemStack item = itemID.makeNormalStack(1);
		if(SimpleServiceLocator.forestryProxy.isBee(item)) {
			if(!SimpleServiceLocator.forestryProxy.isAnalysedBee(item)) {
				if(_power.canUseEnergy(3)) {
					return _sinkReply;
				}
			}
		}
		return null;
	}

	@Override
	public LogisticsModule getSubModule(int slot) {
		return null;
	}

	@Override
	public void tick() {
		if (extractMode) {
			if (++currentTick < ticksToAction) return;
			currentTick = 0;
			IInventoryUtil inv = _invProvider.getUnsidedInventory();
			if (inv == null) return;
			for (int i = 0; i < inv.getSizeInventory(); i++) {
				ItemStack item = inv.getStackInSlot(i);
				if (SimpleServiceLocator.forestryProxy.isBee(item)) {
					if (SimpleServiceLocator.forestryProxy.isAnalysedBee(item)) {
						Pair3<Integer, SinkReply, List<IFilter>> reply = _itemSender.hasDestination(ItemIdentifier.get(item), true, new ArrayList<Integer>());
						if (reply == null)
							continue;
						if (_power.useEnergy(6)) {
							_itemSender.sendStack(inv.decrStackSize(i, 1), reply, ItemSendMode.Normal);
						}
					}
				}
			}
		}
	}


	@Override 
	public void registerSlot(int slot) {
		this.slot = slot;
	}
	
	@Override 
	public final int getX() {
		if(slot>=0)
			return this._invProvider.getX();
		else 
			return 0;
	}
	@Override 
	public final int getY() {
		if(slot>=0)
			return this._invProvider.getY();
		else 
			return -1;
	}
	
	@Override 
	public final int getZ() {
		if(slot>=0)
			return this._invProvider.getZ();
		else 
			return -1-slot;
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
	public int getGuiHandlerID() {
		return GuiIDs.GUI_Module_Apiarist_Analyzer;
	}
	
	public void setExtractMode(int mode) {
		if (getExtractMode() == mode) return;
		
		if (mode == 1) {
			extractMode = true;
		} else if (mode == 0) {
			extractMode = false;
		}
		modeChanged();
	}
		
	public int getExtractMode() {
		return extractMode?1:0;
	}
	
	public void modeChanged() {
		if(_world != null) {
			if(MainProxy.isServer(_world.getWorld())) {
//TODO 			MainProxy.sendToPlayerList(new PacketModuleInteger(NetworkConstants.APIRARIST_ANALYZER_EXTRACTMODE, getX(), getY(), getZ(), slot, getExtractMode()).getPacket(), localModeWatchers);
				MainProxy.sendToPlayerList(PacketHandler.getPacket(ApiaristAnalyserMode.class).setInteger2(slot).setInteger(getExtractMode()).setPosX(getX()).setPosY(getY()).setPosZ(getZ()), localModeWatchers);
			} else {
//TODO 			MainProxy.sendPacketToServer(new PacketModuleInteger(NetworkConstants.APIRARIST_ANALYZER_EXTRACTMODE, getX(), getY(), getZ(), slot, getExtractMode()).getPacket());
				MainProxy.sendPacketToServer(PacketHandler.getPacket(ApiaristAnalyserMode.class).setInteger2(slot).setInteger(getExtractMode()).setPosX(getX()).setPosY(getY()).setPosZ(getZ()));
			}
		} else {
//TODO 		MainProxy.sendPacketToServer(new PacketModuleInteger(NetworkConstants.APIRARIST_ANALYZER_EXTRACTMODE, getX(), getY(), getZ(), slot, getExtractMode()).getPacket());
			MainProxy.sendPacketToServer(PacketHandler.getPacket(ApiaristAnalyserMode.class).setInteger2(slot).setInteger(getExtractMode()).setPosX(getX()).setPosY(getY()).setPosZ(getZ()));
		}
	}

	@Override
	public List<String> getClientInformation() {
		List<String> info = new ArrayList<String>();
		info.add("Extract Mode:");
		info.add(" - " + (extractMode?"on":"off"));
		return info;
	}

	@Override
	public void startWatching(EntityPlayer player) {
		localModeWatchers.add(player);
//TODO 	MainProxy.sendPacketToPlayer(new PacketModuleInteger(NetworkConstants.APIRARIST_ANALYZER_EXTRACTMODE, getX(), getY(), getZ(), slot, getExtractMode()).getPacket(), (Player)player);
		MainProxy.sendPacketToPlayer(PacketHandler.getPacket(ApiaristAnalyserMode.class).setInteger2(slot).setInteger(getExtractMode()).setPosX(getX()).setPosY(getY()).setPosZ(getZ()), (Player)player);
	}

	@Override
	public void stopWatching(EntityPlayer player) {
		localModeWatchers.remove(player);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Icon getIconTexture(IconRegister register) {
		return register.registerIcon("logisticspipes:itemModule/ModuleApiaristAnalyser");
	}
}
