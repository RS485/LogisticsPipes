package logisticspipes.modules;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import logisticspipes.api.IRoutedPowerProvider;
import logisticspipes.gui.hud.modules.HUDElectricManager;
import logisticspipes.interfaces.IClientInformationProvider;
import logisticspipes.interfaces.IHUDModuleHandler;
import logisticspipes.interfaces.IHUDModuleRenderer;
import logisticspipes.interfaces.IInventoryUtil;
import logisticspipes.interfaces.IModuleInventoryReceive;
import logisticspipes.interfaces.IModuleWatchReciver;
import logisticspipes.interfaces.ISendRoutedItem;
import logisticspipes.interfaces.IWorldProvider;
import logisticspipes.interfaces.routing.IFilter;
import logisticspipes.logisticspipes.IInventoryProvider;
import logisticspipes.network.GuiIDs;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.hud.HUDStartModuleWatchingPacket;
import logisticspipes.network.packets.module.ElectricManagetMode;
import logisticspipes.network.packets.module.ModuleInventory;
import logisticspipes.pipefxhandlers.Particles;
import logisticspipes.pipes.basic.CoreRoutedPipe.ItemSendMode;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.utils.ISimpleInventoryEventHandler;
import logisticspipes.utils.ItemIdentifier;
import logisticspipes.utils.ItemIdentifierStack;
import logisticspipes.utils.Pair3;
import logisticspipes.utils.PlayerCollectionList;
import logisticspipes.utils.SimpleInventory;
import logisticspipes.utils.SinkReply;
import logisticspipes.utils.SinkReply.FixedPriority;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Icon;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ModuleElectricManager extends LogisticsGuiModule implements IClientInformationProvider, IHUDModuleHandler, IModuleWatchReciver, ISimpleInventoryEventHandler, IModuleInventoryReceive {

	private final SimpleInventory _filterInventory = new SimpleInventory(9, "Electric Items", 1);
	private boolean _dischargeMode;
	protected IInventoryProvider _invProvider;
	protected ISendRoutedItem _itemSender;
	protected IRoutedPowerProvider _power;
	private int ticksToAction = 100;
	private int currentTick = 0;

	private int slot = 0;

	private IWorldProvider _world;

	private IHUDModuleRenderer HUD = new HUDElectricManager(this);

	private final PlayerCollectionList localModeWatchers = new PlayerCollectionList();

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
//TODO 	MainProxy.sendToPlayerList(new PacketModuleInteger(NetworkConstants.ELECTRIC_MANAGER_STATE, getX(), getY(), getZ(), slot, isDischargeMode() ? 1 : 0).getPacket(), localModeWatchers);
		MainProxy.sendToPlayerList(PacketHandler.getPacket(ElectricManagetMode.class).setInteger(slot).setInteger(isDischargeMode() ? 1 : 0).setPosX(getX()).setPosY(getY()).setPosZ(getZ()), localModeWatchers);
	}

	@Override
	public void registerHandler(IInventoryProvider invProvider, ISendRoutedItem itemSender, IWorldProvider world, IRoutedPowerProvider powerprovider) {
		_invProvider = invProvider;
		_itemSender = itemSender;
		_power = powerprovider;
		_world = world;
	}


	private final SinkReply _sinkReply = new SinkReply(FixedPriority.ElectricManager, 0, true, false, 1, 1);
	@Override
	public SinkReply sinksItem(ItemIdentifier stackID, int bestPriority, int bestCustomPriority, boolean allowDefault, boolean includeInTransit) {
		if (bestPriority >= FixedPriority.ElectricManager.ordinal()) return null;
		if (!_power.canUseEnergy(1)) return null;
		ItemStack stack = stackID.makeNormalStack(1);
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
	public LogisticsModule getSubModule(int slot) {return null;}

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

		IInventoryUtil inv = _invProvider.getSneakyInventory(true);
		if(inv == null) return;
		for(int i=0; i < inv.getSizeInventory(); i++) {
			ItemStack stack = inv.getStackInSlot(i);
			if (stack == null) return;
			if (isOfInterest(stack)) {
				//If item set to discharge and its fully discharged, then extract it.
				if (_dischargeMode && SimpleServiceLocator.IC2Proxy.isFullyDischarged(stack)) {
					Pair3<Integer, SinkReply, List<IFilter>> reply = SimpleServiceLocator.logisticsManager.hasDestinationWithMinPriority(ItemIdentifier.get(stack), _itemSender.getSourceID(), true, FixedPriority.ElectricBuffer);
					if(reply == null) continue;
					if(_power.useEnergy(10)) {
						MainProxy.sendSpawnParticlePacket(Particles.OrangeParticle, getX(), getY(), getZ(), _world.getWorld(), 2);
						_itemSender.sendStack(inv.decrStackSize(i,1), reply, ItemSendMode.Normal);
						return;
					}
				}
				//If item set to charge  and its fully charged, then extract it.
				if (!_dischargeMode && SimpleServiceLocator.IC2Proxy.isFullyCharged(stack)) {
					Pair3<Integer, SinkReply, List<IFilter>> reply = SimpleServiceLocator.logisticsManager.hasDestinationWithMinPriority(ItemIdentifier.get(stack), _itemSender.getSourceID(), true, FixedPriority.ElectricBuffer);
					if(reply == null) continue;
					if(_power.useEnergy(10)) {
						MainProxy.sendSpawnParticlePacket(Particles.OrangeParticle, getX(), getY(), getZ(), _world.getWorld(), 2);
						_itemSender.sendStack(inv.decrStackSize(i,1), reply, ItemSendMode.Normal);
						return;
					}
				}
			}
		}
	}

	private boolean isOfInterest(ItemStack stack) {
		if (!SimpleServiceLocator.IC2Proxy.isElectricItem(stack)) return false;
		for (int i = 0; i < _filterInventory.getSizeInventory(); i++) {
			ItemStack fStack = _filterInventory.getStackInSlot(i);
			if (fStack == null) continue;
			if (SimpleServiceLocator.IC2Proxy.isSimilarElectricItem(stack, fStack)) return true;
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
//TODO 	MainProxy.sendPacketToPlayer(new PacketModuleInvContent(NetworkConstants.MODULE_INV_CONTENT, getX(), getY(), getZ(), slot, ItemIdentifierStack.getListFromInventory(_filterInventory)).getPacket(), (Player)player);
		MainProxy.sendPacketToPlayer(PacketHandler.getPacket(ModuleInventory.class).setSlot(slot).setIdentList(ItemIdentifierStack.getListFromInventory(_filterInventory)).setPosX(getX()).setPosY(getY()).setPosZ(getZ()), (Player)player);
//TODO 	MainProxy.sendPacketToPlayer(new PacketModuleInteger(NetworkConstants.ELECTRIC_MANAGER_STATE, getX(), getY(), getZ(), slot, isDischargeMode() ? 1 : 0).getPacket(), (Player)player);
		MainProxy.sendPacketToPlayer(PacketHandler.getPacket(ElectricManagetMode.class).setInteger2(slot).setInteger(isDischargeMode() ? 1 : 0).setPosX(getX()).setPosY(getY()).setPosZ(getZ()), (Player)player);
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
//TODO 	MainProxy.sendToPlayerList(new PacketModuleInvContent(NetworkConstants.MODULE_INV_CONTENT, getX(), getY(), getZ(), slot, ItemIdentifierStack.getListFromInventory(inventory)).getPacket(), localModeWatchers);
		MainProxy.sendToPlayerList(PacketHandler.getPacket(ModuleInventory.class).setSlot(slot).setIdentList(ItemIdentifierStack.getListFromInventory(inventory)).setPosX(getX()).setPosY(getY()).setPosZ(getZ()), localModeWatchers);
	}

	@Override
	public void handleInvContent(Collection<ItemIdentifierStack> list) {
		_filterInventory.handleItemIdentifierList(list);
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
		return false; // would be true, but hasGenericInterests means this is interested anyway.
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
		return register.registerIcon("logisticspipes:itemModule/ModuleElectricManager");
	}
}
