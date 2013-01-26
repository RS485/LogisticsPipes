package logisticspipes.modules;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import logisticspipes.gui.hud.modules.HUDElectricManager;
import logisticspipes.interfaces.IChassiePowerProvider;
import logisticspipes.interfaces.IClientInformationProvider;
import logisticspipes.interfaces.IHUDModuleHandler;
import logisticspipes.interfaces.IHUDModuleRenderer;
import logisticspipes.interfaces.ILogisticsGuiModule;
import logisticspipes.interfaces.ILogisticsModule;
import logisticspipes.interfaces.IModuleInventoryReceive;
import logisticspipes.interfaces.IModuleWatchReciver;
import logisticspipes.interfaces.ISendRoutedItem;
import logisticspipes.interfaces.IWorldProvider;
import logisticspipes.logisticspipes.IInventoryProvider;
import logisticspipes.network.GuiIDs;
import logisticspipes.network.NetworkConstants;
import logisticspipes.network.packets.PacketModuleInteger;
import logisticspipes.network.packets.PacketModuleInvContent;
import logisticspipes.network.packets.PacketPipeInteger;
import logisticspipes.pipefxhandlers.Particles;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.utils.ISimpleInventoryEventHandler;
import logisticspipes.utils.ItemIdentifierStack;
import logisticspipes.utils.SimpleInventory;
import logisticspipes.utils.SinkReply;
import logisticspipes.utils.SinkReply.FixedPriority;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import cpw.mods.fml.common.network.Player;

public class ModuleElectricManager implements ILogisticsGuiModule, IClientInformationProvider, IHUDModuleHandler, IModuleWatchReciver, ISimpleInventoryEventHandler, IModuleInventoryReceive {

	private final SimpleInventory _filterInventory = new SimpleInventory(9, "Electric Items", 1);
	private boolean _dischargeMode;
	protected IInventoryProvider _invProvider;
	protected ISendRoutedItem _itemSender;
	protected IChassiePowerProvider _power;
	private int ticksToAction = 100;
	private int currentTick = 0;
	
	private int slot = 0;
	public int xCoord = 0;
	public int yCoord = 0;
	public int zCoord = 0;
	private IWorldProvider _world;
	
	private IHUDModuleRenderer HUD = new HUDElectricManager(this);
	
	private final List<EntityPlayer> localModeWatchers = new ArrayList<EntityPlayer>();
	
	public ModuleElectricManager() {
		_filterInventory.addListener(this);
	}

	public IInventory getFilterInventory(){
		return _filterInventory;
	}

	public boolean isDischargeMode(){
		return _dischargeMode;
	}
	public void setDischargeMode(boolean isDischargeMode){
		_dischargeMode = isDischargeMode;
		MainProxy.sendToPlayerList(new PacketModuleInteger(NetworkConstants.ELECTRIC_MANAGER_STATE, xCoord, yCoord, zCoord, slot, isDischargeMode() ? 1 : 0).getPacket(), localModeWatchers);
	}

	@Override
	public void registerHandler(IInventoryProvider invProvider, ISendRoutedItem itemSender, IWorldProvider world, IChassiePowerProvider powerprovider) {
		_invProvider = invProvider;
		_itemSender = itemSender;
		_power = powerprovider;
		_world = world;
	}


	private final SinkReply _sinkReply = new SinkReply(FixedPriority.ElectricNetwork, 0, true, false, 1, 1);
	@Override
	public SinkReply sinksItem(ItemStack stack, int bestPriority, int bestCustomPriority) {
		if (bestPriority >= FixedPriority.ElectricNetwork.ordinal()) return null;
		IInventory inv = _invProvider.getInventory();
		if (inv == null) return null;
		if (!_power.canUseEnergy(1)) return null;
		if (isOfInterest(stack)) {
			//If item is full and in discharge mode, sink.
			if (_dischargeMode && SimpleServiceLocator.IC2Proxy.isFullyCharged(stack)) return _sinkReply;
			
			//If item is empty and in charge mode, sink.
			if (!_dischargeMode && SimpleServiceLocator.IC2Proxy.isFullyDischarged(stack)) return _sinkReply;
			
			//If item is partially charged, sink.
			if (SimpleServiceLocator.IC2Proxy.isPartiallyCharged(stack)) return _sinkReply;
		}
		return null;
	}

	@Override
	public int getGuiHandlerID() {
		return GuiIDs.GUI_Module_ElectricManager_ID;
	}

	@Override
	public ILogisticsModule getSubModule(int slot) {return null;}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		_filterInventory.readFromNBT(nbttagcompound, "");
		setDischargeMode(nbttagcompound.getBoolean("discharge"));
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		_filterInventory.writeToNBT(nbttagcompound, "");
		nbttagcompound.setBoolean("discharge", isDischargeMode());
	}

	@Override
	public void tick() {
		if (++currentTick  < ticksToAction) return;
		currentTick = 0;

		IInventory inv = _invProvider.getInventory();
		if(inv == null) return;
		for(int i=0; i < inv.getSizeInventory(); i++) {
			ItemStack stack = inv.getStackInSlot(i);
			if (stack == null) return;
			if (isOfInterest(stack)) {
				//If item set to discharge and its fully discharged, then extract it.
				if (_dischargeMode && SimpleServiceLocator.IC2Proxy.isFullyDischarged(stack) && SimpleServiceLocator.logisticsManager.hasDestinationWithMinPriority(stack, _itemSender.getSourceID(), true, FixedPriority.ElectricNetwork)) {
					if(_power.useEnergy(10)) {
						MainProxy.sendSpawnParticlePacket(Particles.OrangeParticle, xCoord, yCoord, zCoord, _world.getWorld(), 2);
						_itemSender.sendStack(inv.decrStackSize(i,1));
						return;
					}
				}
				//If item set to charge  and its fully charged, then extract it.
				if (!_dischargeMode && SimpleServiceLocator.IC2Proxy.isFullyCharged(stack) && SimpleServiceLocator.logisticsManager.hasDestinationWithMinPriority(stack, _itemSender.getSourceID(), true, FixedPriority.ElectricNetwork)) {
					if(_power.useEnergy(10)) {
						MainProxy.sendSpawnParticlePacket(Particles.OrangeParticle, xCoord, yCoord, zCoord, _world.getWorld(), 2);
						_itemSender.sendStack(inv.decrStackSize(i,1));
						return;
					}
				}
			}
		}
	}

	private boolean isOfInterest(ItemStack stack) {
		if (!SimpleServiceLocator.IC2Proxy.isElectricItem(stack)) return false;
		String stackName = stack.getItemName();
		for (int i = 0; i < _filterInventory.getSizeInventory(); i++) {
			ItemStack fStack = _filterInventory.getStackInSlot(i);
			if (fStack == null) continue;
			String fStackName = fStack.getItemName();
			if (stackName.equals(fStackName)) return true;
		}
		return false;
	}	

	@Override
	public List<String> getClientInformation() {
		List<String> list = new ArrayList<String>();
		list.add("Mode: " + (isDischargeMode() ? "Discharge Items" : "Charge Items"));
		list.add("Supplied: ");
		list.add("<inventory>");
		list.add("<that>");
		return list;
	}


	@Override
	public void registerPosition(int xCoord, int yCoord, int zCoord, int slot) {
		this.xCoord = xCoord;
		this.yCoord = yCoord;
		this.zCoord = zCoord;
		this.slot = slot;
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
		MainProxy.sendPacketToPlayer(new PacketModuleInvContent(NetworkConstants.MODULE_INV_CONTENT, xCoord, yCoord, zCoord, slot, ItemIdentifierStack.getListFromInventory(_filterInventory)).getPacket(), (Player)player);
		MainProxy.sendPacketToPlayer(new PacketModuleInteger(NetworkConstants.ELECTRIC_MANAGER_STATE, xCoord, yCoord, zCoord, slot, isDischargeMode() ? 1 : 0).getPacket(), (Player)player);
	}

	@Override
	public void stopWatching(EntityPlayer player) {
		localModeWatchers.remove(player);
	}

	@Override
	public IHUDModuleRenderer getRenderer() {
		return HUD;
	}

	@Override
	public void InventoryChanged(SimpleInventory inventory) {
		MainProxy.sendToPlayerList(new PacketModuleInvContent(NetworkConstants.MODULE_INV_CONTENT, xCoord, yCoord, zCoord, slot, ItemIdentifierStack.getListFromInventory(inventory)).getPacket(), localModeWatchers);
	}

	@Override
	public void handleInvContent(LinkedList<ItemIdentifierStack> list) {
		_filterInventory.handleItemIdentifierList(list);
	}
}
