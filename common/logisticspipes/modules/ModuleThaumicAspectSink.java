package logisticspipes.modules;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import logisticspipes.interfaces.IChassiePowerProvider;
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
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.utils.SinkReply;
import logisticspipes.utils.SinkReply.FixedPriority;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import cpw.mods.fml.common.network.Player;

public class ModuleThaumicAspectSink implements ILogisticsGuiModule, IClientInformationProvider, IModuleWatchReciver {

	private int slot = 0;
	private int xCoord = 0;
	private int yCoord = 0;
	private int zCoord = 0;
	IChassiePowerProvider _power;
	
	public final List<Integer> aspectList = new LinkedList<Integer>();
	
	private final List<EntityPlayer> localModeWatchers = new ArrayList<EntityPlayer>();

	@Override
	public void registerHandler(IInventoryProvider invProvider, ISendRoutedItem itemSender, IWorldProvider world, IChassiePowerProvider powerProvider) {
		_power = powerProvider;
	}

	@Override
	public void registerPosition(int xCoord, int yCoord, int zCoord, int slot) {
		this.xCoord = xCoord;
		this.yCoord = yCoord;
		this.zCoord = zCoord;
		this.slot = slot;		
	}
	
	private static final SinkReply _sinkReply = new SinkReply(FixedPriority.ItemSink, -2, true, false, 5, 0);
	@Override
	public SinkReply sinksItem(ItemStack item, int bestPriority, int bestCustomPriority) {
		if(bestPriority > _sinkReply.fixedPriority.ordinal() || (bestPriority == _sinkReply.fixedPriority.ordinal() && bestCustomPriority >= _sinkReply.customPriority)) return null;
		if(isOfInterest(item)) return _sinkReply;
		return null;
	}

	private boolean isOfInterest(ItemStack stack) {
		if (stack == null || aspectList.size() == 0) return false;
		List<Integer> itemAspectList = SimpleServiceLocator.thaumCraftProxy.getListOfTagIDsForStack(stack);
		if (itemAspectList.size() == 0 || itemAspectList == null) return false;
		for (int i = 0; i < itemAspectList.size(); i++) {
			if (aspectList.contains(itemAspectList.get(i))) return true;
		}
		return false;
	}

	@Override
	public ILogisticsModule getSubModule(int slot) {
		return null;
	}

	@Override
	public void tick() {}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		aspectList.clear();
		int size = nbttagcompound.getInteger("aspectListSize");
		if (size <= 0) return;
		for (int i = 0; i < size; i++) {
			aspectList.add(nbttagcompound.getInteger("aspect" + i));
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		nbttagcompound.setInteger("aspectListSize", aspectList.size());
		if (aspectList.size() <= 0) return;
		for (int i = 0; i < aspectList.size(); i++) {
			nbttagcompound.setInteger("aspect" + i, aspectList.get(i));
		}
	}

	@Override
	public void startWatching(EntityPlayer player) {
		localModeWatchers.add(player);
		NBTTagCompound nbt = new NBTTagCompound();
		writeToNBT(nbt);
		MainProxy.sendPacketToPlayer(new PacketModuleNBT(NetworkConstants.THAUMICASPECTSINKLIST, xCoord, yCoord, zCoord, slot, nbt).getPacket(), (Player)player);		
	}

	@Override
	public void stopWatching(EntityPlayer player) {
		localModeWatchers.remove(player);
	}

	public void aspectListChanged() {
		if(MainProxy.isServer()) {
			NBTTagCompound nbt = new NBTTagCompound();
			writeToNBT(nbt);
			MainProxy.sendToPlayerList(new PacketModuleNBT(NetworkConstants.THAUMICASPECTSINKLIST, xCoord, yCoord, zCoord, slot, nbt).getPacket(), localModeWatchers);
		} else {
			NBTTagCompound nbt = new NBTTagCompound();
			writeToNBT(nbt);
			MainProxy.sendPacketToServer(new PacketModuleNBT(NetworkConstants.THAUMICASPECTSINKLIST, xCoord, yCoord, zCoord, slot, nbt).getPacket());	
		}
	}

	@Override
	public List<String> getClientInformation() {
		List<String> info = new ArrayList<String>();
		info.add("Aspects: ");
		if (aspectList.size() == 0) {
			info.add("none");
		}
		for (int i = 0; i < aspectList.size(); i++) {
			info.add(" - " + SimpleServiceLocator.thaumCraftProxy.getNameForTagID(aspectList.get(i)));
		}
		return info;
	}

	@Override
	public int getGuiHandlerID() {
		return GuiIDs.GUI_Module_Thaumic_AspectSink_ID;
	}

	public void handleItem(ItemStack stack) {
		List<Integer> itemAspectList = SimpleServiceLocator.thaumCraftProxy.getListOfTagIDsForStack(stack);
		if (itemAspectList == null) return;
		boolean listChanged = false;
		for (int i = 0; i < itemAspectList.size(); i++) {
			if (aspectList.contains(itemAspectList.get(i))) continue;
			aspectList.add(itemAspectList.get(i));
			listChanged = true;
		}
		if (listChanged) aspectListChanged();
	}

	public void clearAspectList() {
		NBTTagCompound nbt = new NBTTagCompound();
		readFromNBT(nbt);
		aspectListChanged();
	}

}
