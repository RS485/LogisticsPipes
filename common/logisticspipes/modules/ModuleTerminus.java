package logisticspipes.modules;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import logisticspipes.api.IRoutedPowerProvider;
import logisticspipes.gui.hud.modules.HUDTerminatorModule;
import logisticspipes.interfaces.IClientInformationProvider;
import logisticspipes.interfaces.IHUDModuleHandler;
import logisticspipes.interfaces.IHUDModuleRenderer;
import logisticspipes.interfaces.IModuleInventoryReceive;
import logisticspipes.interfaces.IModuleWatchReciver;
import logisticspipes.interfaces.ISendRoutedItem;
import logisticspipes.interfaces.IWorldProvider;
import logisticspipes.logisticspipes.IInventoryProvider;
import logisticspipes.network.GuiIDs;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.hud.HUDStartModuleWatchingPacket;
import logisticspipes.network.packets.module.ModuleInventory;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.ISimpleInventoryEventHandler;
import logisticspipes.utils.ItemIdentifier;
import logisticspipes.utils.ItemIdentifierStack;
import logisticspipes.utils.PlayerCollectionList;
import logisticspipes.utils.SimpleInventory;
import logisticspipes.utils.SinkReply;
import logisticspipes.utils.SinkReply.FixedPriority;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Icon;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ModuleTerminus extends LogisticsGuiModule implements IClientInformationProvider, IHUDModuleHandler, IModuleWatchReciver, ISimpleInventoryEventHandler, IModuleInventoryReceive {

	private final SimpleInventory _filterInventory = new SimpleInventory(9, "Terminated items", 1);
	



	private int slot;
	
	private IRoutedPowerProvider _power;
	
	private IHUDModuleRenderer HUD = new HUDTerminatorModule(this);

	private final PlayerCollectionList localModeWatchers = new PlayerCollectionList();
	
	public ModuleTerminus() {
		_filterInventory.addListener(this);
	}
	
	public IInventory getFilterInventory(){
		return _filterInventory;
	}
	
	@Override
	public void registerHandler(IInventoryProvider invProvider, ISendRoutedItem itemSender, IWorldProvider world, IRoutedPowerProvider powerprovider) {
		_power = powerprovider;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		_filterInventory.readFromNBT(nbttagcompound, "");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
    	_filterInventory.writeToNBT(nbttagcompound, "");
    }

	@Override
	public int getGuiHandlerID() {
		return GuiIDs.GUI_Module_Terminus_ID;
	}
	
	private static final SinkReply _sinkReply = new SinkReply(FixedPriority.Terminus, 0, true, false, 2, 0);
	@Override
	public SinkReply sinksItem(ItemIdentifier item, int bestPriority, int bestCustomPriority, boolean allowDefault, boolean includeInTransit) {
		if(bestPriority > _sinkReply.fixedPriority.ordinal() || (bestPriority == _sinkReply.fixedPriority.ordinal() && bestCustomPriority >= _sinkReply.customPriority)) return null;
		if (_filterInventory.containsUndamagedItem(item.getUndamaged())){
			if(_power.canUseEnergy(2)) {
				return _sinkReply;
			}
		}

		return null;
	}

	@Override
	public LogisticsModule getSubModule(int slot) {return null;}

	@Override
	public void tick() {}

	@Override
	public List<String> getClientInformation() {
		List<String> list = new ArrayList<String>();
		list.add("Terminated: ");
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
	public IHUDModuleRenderer getRenderer() {
		return HUD;
	}

	@Override
	public void startWatching(EntityPlayer player) {
		localModeWatchers.add(player);
//TODO 	MainProxy.sendToPlayerList(new PacketModuleInvContent(NetworkConstants.MODULE_INV_CONTENT, getX(), getY(), getZ(), slot, ItemIdentifierStack.getListFromInventory(_filterInventory)).getPacket(), localModeWatchers);
		MainProxy.sendToPlayerList(PacketHandler.getPacket(ModuleInventory.class).setSlot(slot).setIdentList(ItemIdentifierStack.getListFromInventory(_filterInventory)).setPosX(getX()).setPosY(getY()).setPosZ(getZ()), localModeWatchers);
	}

	@Override
	public void stopWatching(EntityPlayer player) {
		localModeWatchers.remove(player);
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
		return false;
	}

	@Override
	public List<ItemIdentifier> getSpecificInterests() {
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
		return register.registerIcon("logisticspipes:itemModule/ModuleTerminus");
	}
}
