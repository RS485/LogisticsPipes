package logisticspipes.modules;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import logisticspipes.gui.hud.modules.HUDItemSink;
import logisticspipes.interfaces.IClientInformationProvider;
import logisticspipes.interfaces.IHUDModuleHandler;
import logisticspipes.interfaces.IHUDModuleRenderer;
import logisticspipes.interfaces.IModuleInventoryReceive;
import logisticspipes.interfaces.IModuleWatchReciver;
import logisticspipes.modules.abstractmodules.LogisticsGuiModule;
import logisticspipes.modules.abstractmodules.LogisticsModule;
import logisticspipes.network.NewGuiHandler;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractguis.ModuleCoordinatesGuiProvider;
import logisticspipes.network.abstractguis.ModuleInHandGuiProvider;
import logisticspipes.network.guis.module.inhand.ItemSinkInHand;
import logisticspipes.network.guis.module.inpipe.ItemSinkSlot;
import logisticspipes.network.packets.hud.HUDStartModuleWatchingPacket;
import logisticspipes.network.packets.hud.HUDStopModuleWatchingPacket;
import logisticspipes.network.packets.module.ModuleInventory;
import logisticspipes.network.packets.modules.ItemSinkDefault;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.cc.interfaces.CCCommand;
import logisticspipes.proxy.cc.interfaces.CCType;
import logisticspipes.utils.ISimpleInventoryEventHandler;
import logisticspipes.utils.PlayerCollectionList;
import logisticspipes.utils.SinkReply;
import logisticspipes.utils.SinkReply.FixedPriority;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.item.ItemIdentifierInventory;
import logisticspipes.utils.item.ItemIdentifierStack;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Icon;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@CCType(name="ItemSink Module")
public class ModuleItemSink extends LogisticsGuiModule implements IClientInformationProvider, IHUDModuleHandler, IModuleWatchReciver, ISimpleInventoryEventHandler, IModuleInventoryReceive {
	
	private final ItemIdentifierInventory _filterInventory = new ItemIdentifierInventory(9, "Requested items", 1);
	private boolean _isDefaultRoute;
	
	private IHUDModuleRenderer HUD = new HUDItemSink(this);
	
	private final PlayerCollectionList localModeWatchers = new PlayerCollectionList();
	
	public ModuleItemSink() {
		_filterInventory.addListener(this);
	}
	
	@CCCommand(description="Returns the FilterInventory of this Module")
	public ItemIdentifierInventory getFilterInventory(){
		return _filterInventory;
	}

	@CCCommand(description="Returns true if the module is a default route")
	public boolean isDefaultRoute(){
		return _isDefaultRoute;
	}
	
	@CCCommand(description="Sets the default route status of this module")
	public void setDefaultRoute(Boolean isDefaultRoute){
		_isDefaultRoute = isDefaultRoute;
		if(!localModeWatchers.isEmpty()) MainProxy.sendToPlayerList(PacketHandler.getPacket(ItemSinkDefault.class).setFlag(_isDefaultRoute).setModulePos(this), localModeWatchers);
	}

	
	private static final SinkReply _sinkReply = new SinkReply(FixedPriority.ItemSink, 0, true, false, 1, 0);
	private static final SinkReply _sinkReplyDefault = new SinkReply(FixedPriority.DefaultRoute, 0, true, true, 1, 0);
	@Override
	public SinkReply sinksItem(ItemIdentifier item, int bestPriority, int bestCustomPriority, boolean allowDefault, boolean includeInTransit) {
		if(_isDefaultRoute && !allowDefault) return null;
		if(bestPriority > _sinkReply.fixedPriority.ordinal() || (bestPriority == _sinkReply.fixedPriority.ordinal() && bestCustomPriority >= _sinkReply.customPriority)) return null;
		if (_filterInventory.containsUndamagedItem(item.getUndamaged())){
			if(_service.canUseEnergy(1)) {
				return _sinkReply;
			}
			return null;
		}
		if (_isDefaultRoute){
			if(bestPriority > _sinkReplyDefault.fixedPriority.ordinal() || (bestPriority == _sinkReplyDefault.fixedPriority.ordinal() && bestCustomPriority >= _sinkReplyDefault.customPriority)) return null;
			if(_service.canUseEnergy(1)) {
				return _sinkReplyDefault;
			}
			return null;
		}
		return null;
	}

	@Override
	public ModuleCoordinatesGuiProvider getPipeGuiProvider() {
		return NewGuiHandler.getGui(ItemSinkSlot.class).setDefaultRoute(_isDefaultRoute);
	}

	@Override
	public ModuleInHandGuiProvider getInHandGuiProvider() {
		return NewGuiHandler.getGui(ItemSinkInHand.class);
	}

	@Override
	public LogisticsModule getSubModule(int slot) {return null;}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		_filterInventory.readFromNBT(nbttagcompound, "");
		_isDefaultRoute = nbttagcompound.getBoolean("defaultdestination");
		//setDefaultRoute(nbttagcompound.getBoolean("defaultdestination"));
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
	public void startHUDWatching() {
		MainProxy.sendPacketToServer(PacketHandler.getPacket(HUDStartModuleWatchingPacket.class).setModulePos(this));
	}

	@Override
	public void stopHUDWatching() {
		MainProxy.sendPacketToServer(PacketHandler.getPacket(HUDStopModuleWatchingPacket.class).setModulePos(this));
	}

	@Override
	public void startWatching(EntityPlayer player) {
		localModeWatchers.add(player);
		MainProxy.sendPacketToPlayer(PacketHandler.getPacket(ModuleInventory.class).setIdentList(ItemIdentifierStack.getListFromInventory(_filterInventory)).setModulePos(this), (Player)player);
		MainProxy.sendPacketToPlayer(PacketHandler.getPacket(ItemSinkDefault.class).setFlag(_isDefaultRoute).setModulePos(this), (Player)player);
	}

	@Override
	public void stopWatching(EntityPlayer player) {
		localModeWatchers.remove(player);
	}

	@Override
	public void InventoryChanged(IInventory inventory) {
		MainProxy.sendToPlayerList(PacketHandler.getPacket(ModuleInventory.class).setIdentList(ItemIdentifierStack.getListFromInventory(inventory)).setModulePos(this), localModeWatchers);
	}

	@Override
	public IHUDModuleRenderer getHUDRenderer() {
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
