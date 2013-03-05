package logisticspipes.modules;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import logisticspipes.api.IRoutedPowerProvider;
import logisticspipes.interfaces.IClientInformationProvider;
import logisticspipes.interfaces.ILogisticsGuiModule;
import logisticspipes.interfaces.ILogisticsModule;
import logisticspipes.interfaces.IModuleWatchReciver;
import logisticspipes.interfaces.ISendRoutedItem;
import logisticspipes.interfaces.IWorldProvider;
import logisticspipes.logisticspipes.IInventoryProvider;
import logisticspipes.network.GuiIDs;
import logisticspipes.network.NetworkConstants;
import logisticspipes.network.packets.PacketModuleNBT;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.ItemIdentifier;
import logisticspipes.utils.SinkReply;
import logisticspipes.utils.SinkReply.FixedPriority;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

public class ModuleExpressionSink implements ILogisticsGuiModule, IModuleWatchReciver, IClientInformationProvider{

	private int slot = 0;
	private int xCoord = 0;
	private int yCoord = 0;
	private int zCoord = 0;

	public final List<String> expressionList = new LinkedList<String>();
	public final int maxNumberOfExpressions = 10;
	private final List<EntityPlayer> localModeWatchers = new ArrayList<EntityPlayer>();

	private IRoutedPowerProvider _power;
	private IWorldProvider _world; 

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		expressionList.clear();
		int size = nbt.getInteger("expressionListSize");
		if (size <= 0) return;
		for (int i = 0; i < size; i++) {
			expressionList.add(nbt.getString("expression" + i));
		}
		
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		nbt.setInteger("expressionListSize", expressionList.size());
		if (expressionList.size() <= 0) return;
		for (int i = 0; i < expressionList.size(); i++) {
			nbt.setString("expression" + i, expressionList.get(i));
		}
	}

	@Override
	public void registerHandler(IInventoryProvider invProvider, ISendRoutedItem itemSender, IWorldProvider world, IRoutedPowerProvider powerProvider) {
		_power = powerProvider;
		_world = world;
	}

	@Override
	public void registerPosition(int xCoord, int yCoord, int zCoord, int slot) {
		this.xCoord = xCoord;
		this.yCoord = yCoord;
		this.zCoord = zCoord;
		this.slot = slot;
	}

	private static final SinkReply _sinkReply = new SinkReply(FixedPriority.ItemSink, -10, true, false, 1, 0);
	@Override
	public SinkReply sinksItem(ItemIdentifier stack, int bestPriority, int bestCustomPriority) {
		if(bestPriority > _sinkReply.fixedPriority.ordinal() || (bestPriority == _sinkReply.fixedPriority.ordinal() && bestCustomPriority >= _sinkReply.customPriority)) return null;
		String name = stack.getFriendlyNameCC();
		name = name.toLowerCase();
		if (expressionList.contains(name)) {
			if (_power.canUseEnergy(1)) {
				return _sinkReply;
			}
		}
		return null;
	}

	@Override
	public ILogisticsModule getSubModule(int slot) {
		return null;
	}

	@Override
	public void tick() {}

	@Override
	public boolean hasGenericInterests() {
		return true;
	}

	@Override
	public Collection<ItemIdentifier> getSpecificInterests() {
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
		return GuiIDs.GUI_Module_ExpressionSink_ID;
	}

	@Override
	public void startWatching(EntityPlayer player) {
		localModeWatchers.add(player);
		NBTTagCompound nbt = new NBTTagCompound();
		writeToNBT(nbt);
		// TODO add new nbt packet to the player that just started watching
		
	}

	@Override
	public void stopWatching(EntityPlayer player) {
		localModeWatchers.remove(player);		
	}

	@Override
	public List<String> getClientInformation() {
		List<String> list = new ArrayList<String>();
		list.add("Expressions:");
		if (expressionList.size() <= 0) {
			return list;
		} else {
			for (int i = 0; i < expressionList.size(); i++) {
				list.add("\"" + expressionList.get(i) + "\"");
			}
		}
		return list;
	}
	
	public boolean addExpressionToList(String name) {
		if (name == null) return false;
		if (!(name.isEmpty()) && !(expressionList.size() >= maxNumberOfExpressions) && !expressionList.contains(name)) {
			expressionList.add(name);
			expressionListChanged();
			return true;
		}
		expressionListChanged();
		return false;
	}
	
	public void expressionListChanged() {
		if(MainProxy.isServer(_world.getWorld())) {
			NBTTagCompound nbt = new NBTTagCompound();
			writeToNBT(nbt);
			MainProxy.sendToPlayerList(new PacketModuleNBT(NetworkConstants.EXPRESSIONSINKNBT, xCoord, yCoord, zCoord, slot, nbt).getPacket(), localModeWatchers);
		} else {
			NBTTagCompound nbt = new NBTTagCompound();
			writeToNBT(nbt);
			MainProxy.sendPacketToServer(new PacketModuleNBT(NetworkConstants.EXPRESSIONSINKNBT, xCoord, yCoord, zCoord, slot, nbt).getPacket());	
		}
	}
	
	public void clearExpressionList() {
		if (expressionList.size() <= 0) return;
		expressionList.clear();
		expressionListChanged();
	}

}
