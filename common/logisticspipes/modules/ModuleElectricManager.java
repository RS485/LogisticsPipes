package logisticspipes.modules;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import logisticspipes.gui.hud.modules.HUDElectricManager;
import logisticspipes.interfaces.IChassiePowerProvider;
import logisticspipes.interfaces.IClientInformationProvider;
import logisticspipes.interfaces.IHUDModuleHandler;
import logisticspipes.interfaces.IHUDModuleRenderer;
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
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.utils.ISimpleInventoryEventHandler;
import logisticspipes.utils.ItemIdentifierStack;
import logisticspipes.utils.SimpleInventory;
import logisticspipes.utils.SinkReply;
import logisticspipes.utils.SinkReply.FixedPriority;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import cpw.mods.fml.common.network.Player;

public class ModuleElectricManager implements ILogisticsModule, IClientInformationProvider, IHUDModuleHandler, IModuleWatchReciver, ISimpleInventoryEventHandler, IModuleInventoryReceive {

	private final SimpleInventory _filterInventory = new SimpleInventory(9, "Electric Items", 1);
	private boolean _dischargeMode;
	protected IInventoryProvider _invProvider;
	protected ISendRoutedItem _itemSender;
	protected IChassiePowerProvider _power;
	private int ticksToAction = 100;
	private int currentTick = 0;
	
	private int slot = 0;
	private int xCoord = 0;
	private int yCoord = 0;
	private int zCoord = 0;
	
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
	}

	public static int getCharge(ItemStack item)
	{
		if (SimpleServiceLocator.electricItemProxy.isElectricItem(item) && item.hasTagCompound())
			return item.getTagCompound().getInteger("charge");
		else
			return 0;
	}

	public boolean findElectricItem(ItemStack item, boolean discharged, boolean partial) {
		if (!SimpleServiceLocator.electricItemProxy.isElectricItem(item)) return false;

		for (int i = 0; i < _filterInventory.getSizeInventory(); i++){
			ItemStack stack = _filterInventory.getStackInSlot(i);
			if (stack == null) continue;
			if (discharged && SimpleServiceLocator.electricItemProxy.isDischarged(item,partial,stack.getItem()))
				return true;
			if (!discharged && SimpleServiceLocator.electricItemProxy.isCharged(item,partial,stack.getItem()))
				return true;
		}
		return false;
	}

	@Override
	public SinkReply sinksItem(ItemStack item) {
		if (findElectricItem(item, !isDischargeMode(), true)) {
			SinkReply reply = new SinkReply();
			reply.fixedPriority = FixedPriority.ItemSink;
			reply.isPassive = true;
			if(_power.useEnergy(2)) {
				MainProxy.proxy.spawnGenericParticle("BlueParticle", this.xCoord, this.yCoord, this.zCoord, 2);
				return reply;
			}
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
	public void readFromNBT(NBTTagCompound nbttagcompound, String prefix) {
		_filterInventory.readFromNBT(nbttagcompound, "");
		setDischargeMode(nbttagcompound.getBoolean("discharge"));
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound, String prefix) {
		_filterInventory.writeToNBT(nbttagcompound, "");
		nbttagcompound.setBoolean("discharge", isDischargeMode());
	}

	@Override
	public void tick() {
		if(MainProxy.isClient()) return;
		if (++currentTick  < ticksToAction) return;
		currentTick = 0;

		IInventory inv = _invProvider.getInventory();
		if(inv == null) return;
		for(int i=0; i < inv.getSizeInventory(); i++) {
			ItemStack item = inv.getStackInSlot(i);
			if (item != null && findElectricItem(item, isDischargeMode(), false)) {
				if(_power.useEnergy(6)) {
					_itemSender.sendStack(inv.decrStackSize(i,1));
				}
			}
		}
	}

	@Override
	public List<String> getClientInformation() {
		List<String> list = new ArrayList<String>();
		list.add("Discharge Mode: " + (isDischargeMode() ? "Yes" : "No"));
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
