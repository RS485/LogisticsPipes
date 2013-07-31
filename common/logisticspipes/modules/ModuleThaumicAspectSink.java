package logisticspipes.modules;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import logisticspipes.api.IRoutedPowerProvider;
import logisticspipes.interfaces.IClientInformationProvider;
import logisticspipes.interfaces.IModuleWatchReciver;
import logisticspipes.interfaces.ISendRoutedItem;
import logisticspipes.interfaces.IWorldProvider;
import logisticspipes.logisticspipes.IInventoryProvider;
import logisticspipes.network.GuiIDs;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.module.ThaumicAspectsSinkList;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.utils.ItemIdentifier;
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

public class ModuleThaumicAspectSink extends LogisticsGuiModule implements IClientInformationProvider, IModuleWatchReciver {

	private int slot = 0;



	
	private IRoutedPowerProvider _power;
	private IWorldProvider _world;
	
	public final List<Integer> aspectList = new LinkedList<Integer>();
	
	private final PlayerCollectionList localModeWatchers = new PlayerCollectionList();

	@Override
	public void registerHandler(IInventoryProvider invProvider, ISendRoutedItem itemSender, IWorldProvider world, IRoutedPowerProvider powerProvider) {
		_power = powerProvider;
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

	
	private static final SinkReply _sinkReply = new SinkReply(FixedPriority.ItemSink, -2, true, false, 5, 0);
	@Override
	public SinkReply sinksItem(ItemIdentifier item, int bestPriority, int bestCustomPriority, boolean allowDefault, boolean includeInTransit) {
		if(bestPriority > _sinkReply.fixedPriority.ordinal() || (bestPriority == _sinkReply.fixedPriority.ordinal() && bestCustomPriority >= _sinkReply.customPriority)) return null;
		if(isOfInterest(item)) return _sinkReply;
		return null;
	}

	private boolean isOfInterest(ItemIdentifier itemID) {
		if (itemID == null || aspectList.size() == 0) return false;
		ItemStack item = itemID.makeNormalStack(1);
		List<Integer> itemAspectList = SimpleServiceLocator.thaumCraftProxy.getListOfTagIDsForStack(item);
		if (itemAspectList.size() == 0 || itemAspectList == null) return false;
		for (int i = 0; i < itemAspectList.size(); i++) {
			if (aspectList.contains(itemAspectList.get(i))) return true;
		}
		return false;
	}

	@Override
	public LogisticsModule getSubModule(int slot) {
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
//TODO 	MainProxy.sendPacketToPlayer(new PacketModuleNBT(NetworkConstants.THAUMICASPECTSINKLIST, getX(), getY(), getZ(), slot, nbt).getPacket(), (Player)player);		
		MainProxy.sendPacketToPlayer(PacketHandler.getPacket(ThaumicAspectsSinkList.class).setSlot(slot).setTag(nbt).setPosX(getX()).setPosY(getY()).setPosZ(getZ()), (Player)player);
	}

	@Override
	public void stopWatching(EntityPlayer player) {
		localModeWatchers.remove(player);
	}

	public void aspectListChanged() {
		if(MainProxy.isServer(_world.getWorld())) {
			NBTTagCompound nbt = new NBTTagCompound();
			writeToNBT(nbt);
//TODO 		MainProxy.sendToPlayerList(new PacketModuleNBT(NetworkConstants.THAUMICASPECTSINKLIST, getX(), getY(), getZ(), slot, nbt).getPacket(), localModeWatchers);
			MainProxy.sendToPlayerList(PacketHandler.getPacket(ThaumicAspectsSinkList.class).setSlot(slot).setTag(nbt).setPosX(getX()).setPosY(getY()).setPosZ(getZ()), localModeWatchers);
		} else {
			NBTTagCompound nbt = new NBTTagCompound();
			writeToNBT(nbt);
//TODO 		MainProxy.sendPacketToServer(new PacketModuleNBT(NetworkConstants.THAUMICASPECTSINKLIST, getX(), getY(), getZ(), slot, nbt).getPacket());	
			MainProxy.sendPacketToServer(PacketHandler.getPacket(ThaumicAspectsSinkList.class).setSlot(slot).setTag(nbt).setPosX(getX()).setPosY(getY()).setPosZ(getZ()));
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
			if (aspectList.contains(itemAspectList.get(i)) || aspectList.size() >= 9) continue;
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
		return register.registerIcon("logisticspipes:itemModule/ModuleThaumicAspectSink");
	}
}
