package logisticspipes.modules;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import logisticspipes.api.IRoutedPowerProvider;
import logisticspipes.gui.hud.modules.HUDItemSink;
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
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.ISimpleInventoryEventHandler;
import logisticspipes.utils.ItemIdentifier;
import logisticspipes.utils.ItemIdentifierStack;
import logisticspipes.utils.SimpleInventory;
import logisticspipes.utils.SinkReply;
import logisticspipes.utils.SinkReply.FixedPriority;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Icon;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ModuleItemSink implements ILogisticsGuiModule, IClientInformationProvider, IHUDModuleHandler, IModuleWatchReciver, ISimpleInventoryEventHandler, IModuleInventoryReceive {
	
	private final SimpleInventory _filterInventory = new SimpleInventory(9, "Requested items", 1);
	private boolean _isDefaultRoute;
	private int slot = 0;
	
	private IHUDModuleRenderer HUD = new HUDItemSink(this);
	
	private IRoutedPowerProvider _power;
	
	private final List<EntityPlayer> localModeWatchers = new ArrayList<EntityPlayer>();
	
	public ModuleItemSink() {
		_filterInventory.addListener(this);
	}
	
	public IInventory getFilterInventory(){
		return _filterInventory;
	}
	
	public boolean isDefaultRoute(){
		return _isDefaultRoute;
	}
	public void setDefaultRoute(boolean isDefaultRoute){
		_isDefaultRoute = isDefaultRoute;
		MainProxy.sendToPlayerList(new PacketModuleInteger(NetworkConstants.ITEM_SINK_STATUS, getX(), getY(), getZ(), slot, isDefaultRoute() ? 1 : 0).getPacket(), localModeWatchers);
	}

	@Override
	public void registerHandler(IInventoryProvider invProvider, ISendRoutedItem itemSender, IWorldProvider world, IRoutedPowerProvider powerprovider) {
		_power = powerprovider;
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
			return this._power.getX();
		else 
			return -1;
	}
	
	@Override 
	public final int getZ() {
		if(slot>=0)
			return this._power.getX();
		else 
			return -1-slot;
	}

	
	private static final SinkReply _sinkReply = new SinkReply(FixedPriority.ItemSink, 0, true, false, 1, 0);
	private static final SinkReply _sinkReplyDefault = new SinkReply(FixedPriority.DefaultRoute, 0, true, true, 1, 0);
	@Override
	public SinkReply sinksItem(ItemIdentifier item, int bestPriority, int bestCustomPriority, boolean allowDefault, boolean includeInTransit) {
		if(_isDefaultRoute && !allowDefault) return null;
		if(bestPriority > _sinkReply.fixedPriority.ordinal() || (bestPriority == _sinkReply.fixedPriority.ordinal() && bestCustomPriority >= _sinkReply.customPriority)) return null;
		if (_filterInventory.containsUndamagedItem(item.getUndamaged())){
			if(_power.canUseEnergy(1)) {
				return _sinkReply;
			}
			return null;
		}
		if (_isDefaultRoute){
			if(bestPriority > _sinkReplyDefault.fixedPriority.ordinal() || (bestPriority == _sinkReplyDefault.fixedPriority.ordinal() && bestCustomPriority >= _sinkReplyDefault.customPriority)) return null;
			if(_power.canUseEnergy(1)) {
				return _sinkReplyDefault;
			}
			return null;
		}
		return null;
	}

	@Override
	public int getGuiHandlerID() {
		return GuiIDs.GUI_Module_ItemSink_ID;
	}
	
	@Override
	public ILogisticsModule getSubModule(int slot) {return null;}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		_filterInventory.readFromNBT(nbttagcompound, "");
		setDefaultRoute(nbttagcompound.getBoolean("defaultdestination"));
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
    	_filterInventory.writeToNBT(nbttagcompound, "");
    	nbttagcompound.setBoolean("defaultdestination", isDefaultRoute());
	}

	@Override
	public void tick() {}

	@Override
	public List<String> getClientInformation() {
		List<String> list = new ArrayList<String>();
		list.add("Default: " + (isDefaultRoute() ? "Yes" : "No"));
		list.add("Filter: ");
		list.add("<inventory>");
		list.add("<that>");
		return list;
	}

	@Override
	public void startWatching() {
		MainProxy.sendPacketToServer(new PacketPipeInteger(NetworkConstants.HUD_START_WATCHING_MODULE, getX(), getY(), getZ(), slot).getPacket());
	}

	@Override
	public void stopWatching() {
		MainProxy.sendPacketToServer(new PacketPipeInteger(NetworkConstants.HUD_START_WATCHING_MODULE, getX(), getY(), getZ(), slot).getPacket());
	}

	@Override
	public void startWatching(EntityPlayer player) {
		localModeWatchers.add(player);
		MainProxy.sendPacketToPlayer(new PacketModuleInvContent(NetworkConstants.MODULE_INV_CONTENT, getX(), getY(), getZ(), slot, ItemIdentifierStack.getListFromInventory(_filterInventory)).getPacket(), (Player)player);
		MainProxy.sendPacketToPlayer(new PacketModuleInteger(NetworkConstants.ITEM_SINK_STATUS, getX(), getY(), getZ(), slot, isDefaultRoute() ? 1 : 0).getPacket(), (Player)player);
	}

	@Override
	public void stopWatching(EntityPlayer player) {
		localModeWatchers.remove(player);
	}

	@Override
	public void InventoryChanged(SimpleInventory inventory) {
		MainProxy.sendToPlayerList(new PacketModuleInvContent(NetworkConstants.MODULE_INV_CONTENT, getX(), getY(), getZ(), slot, ItemIdentifierStack.getListFromInventory(inventory)).getPacket(), localModeWatchers);
	}

	@Override
	public IHUDModuleRenderer getRenderer() {
		return HUD;
	}

	@Override
	public void handleInvContent(Collection<ItemIdentifierStack> list) {
		_filterInventory.handleItemIdentifierList(list);
	}

	@Override
	public boolean hasGenericInterests() {
		return this._isDefaultRoute;
	}

	@Override
	public List<ItemIdentifier> getSpecificInterests() {
		if(this._isDefaultRoute)
			return null;
		Map<ItemIdentifier, Integer> mapIC = _filterInventory.getItemsAndCount();
		List<ItemIdentifier> li= new ArrayList<ItemIdentifier>(mapIC.size());
		li.addAll(mapIC.keySet());
		for(ItemIdentifier id:mapIC.keySet()){
			li.add(id.getUndamaged());
		}
		return li;
	}

	@Override
	public boolean interestedInAttachedInventory() {		
		return false;
		// when we are default we are interested in everything anyway, otherwise we're only interested in our filter.
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
		return register.registerIcon("logisticspipes:itemModule/ModuleItemSink");
	}
}
