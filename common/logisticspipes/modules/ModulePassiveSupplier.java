package logisticspipes.modules;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import logisticspipes.gui.hud.modules.HUDPassiveSupplier;
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
import logisticspipes.network.packets.PacketModuleInvContent;
import logisticspipes.network.packets.PacketPipeInteger;
import logisticspipes.pipefxhandlers.Particles;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.utils.ISimpleInventoryEventHandler;
import logisticspipes.utils.InventoryUtil;
import logisticspipes.utils.ItemIdentifier;
import logisticspipes.utils.ItemIdentifierStack;
import logisticspipes.utils.SimpleInventory;
import logisticspipes.utils.SinkReply;
import logisticspipes.utils.SinkReply.FixedPriority;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import cpw.mods.fml.common.network.Player;

public class ModulePassiveSupplier implements ILogisticsModule, IClientInformationProvider, IHUDModuleHandler, IModuleWatchReciver, IModuleInventoryReceive, ISimpleInventoryEventHandler {

	private final SimpleInventory _filterInventory = new SimpleInventory(9, "Requested items", 64);
	private IInventoryProvider _invProvider;
	private IChassiePowerProvider _power;
	private int slot = 0;
	private int xCoord = 0;
	private int yCoord = 0;
	private int zCoord = 0;
	private IWorldProvider _world;
	
	private IHUDModuleRenderer HUD = new HUDPassiveSupplier(this);
	
	private final List<EntityPlayer> localModeWatchers = new ArrayList<EntityPlayer>();
	
	
	public ModulePassiveSupplier() {
		_filterInventory.addListener(this);
	}

	@Override
	public void registerHandler(IInventoryProvider invProvider, ISendRoutedItem itemSender, IWorldProvider world, IChassiePowerProvider powerprovider) {
		_invProvider = invProvider;
		_power = powerprovider;
		_world = world;
	}

	public IInventory getFilterInventory(){
		return _filterInventory;
	}
	
	@Override
	public SinkReply sinksItem(ItemStack item) {
		IInventory targetInventory = _invProvider.getInventory();
		if (targetInventory == null) return null;
		
		InventoryUtil filterUtil = SimpleServiceLocator.inventoryUtilFactory.getInventoryUtil(_filterInventory);
		if (!filterUtil.containsItem(ItemIdentifier.get(item))) return null;
		
		int targetCount = filterUtil.itemCount(ItemIdentifier.get(item));
		InventoryUtil targetUtil = SimpleServiceLocator.inventoryUtilFactory.getInventoryUtil(targetInventory);
		if (targetCount <= targetUtil.itemCount(ItemIdentifier.get(item))) return null;
		
		SinkReply reply = new SinkReply();
		reply.fixedPriority = FixedPriority.PassiveSupplier;
		reply.isPassive = true;
		if(_power.useEnergy(2)) {
			MainProxy.sendSpawnParticlePacket(Particles.BlueParticle, xCoord, yCoord, this.zCoord, _world.getWorld(), 2);
			return reply;
		}
		return null;
	}

	@Override
	public int getGuiHandlerID() {
		return GuiIDs.GUI_Module_PassiveSupplier_ID;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound, String prefix) {
		_filterInventory.readFromNBT(nbttagcompound, "");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound, String prefix) {
		_filterInventory.writeToNBT(nbttagcompound, "");
	}

	@Override
	public ILogisticsModule getSubModule(int slot) {return null;}

	@Override
	public void tick() {}

	@Override
	public List<String> getClientInformation() {
		List<String> list = new ArrayList<String>();
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
	public void handleInvContent(LinkedList<ItemIdentifierStack> list) {
		_filterInventory.handleItemIdentifierList(list);
	}

	@Override
	public void InventoryChanged(SimpleInventory inventory) {
		MainProxy.sendToPlayerList(new PacketModuleInvContent(NetworkConstants.MODULE_INV_CONTENT, xCoord, yCoord, zCoord, slot, ItemIdentifierStack.getListFromInventory(_filterInventory)).getPacket(), localModeWatchers);	
	}
}
