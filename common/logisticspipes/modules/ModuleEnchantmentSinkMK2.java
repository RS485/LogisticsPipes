package logisticspipes.modules;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;


import logisticspipes.api.IRoutedPowerProvider;
import logisticspipes.gui.hud.modules.HUDSimpleFilterModule;
import logisticspipes.interfaces.IClientInformationProvider;
import logisticspipes.interfaces.IHUDModuleHandler;
import logisticspipes.interfaces.IHUDModuleRenderer;
import logisticspipes.interfaces.IModuleInventoryReceive;
import logisticspipes.interfaces.IModuleSimpleFilter;
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
import logisticspipes.utils.PlayerCollectionList;
import logisticspipes.utils.SinkReply;
import logisticspipes.utils.SinkReply.FixedPriority;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.item.ItemIdentifierInventory;
import logisticspipes.utils.item.ItemIdentifierStack;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.NBTTagCompound;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.util.IIcon;

public class ModuleEnchantmentSinkMK2 extends LogisticsGuiModule implements IClientInformationProvider, IHUDModuleHandler, IModuleWatchReciver, ISimpleInventoryEventHandler, IModuleInventoryReceive, IModuleSimpleFilter {

	private final ItemIdentifierInventory _filterInventory = new ItemIdentifierInventory(9, "Requested Enchanted items", 1);
	private int slot = 0;

	public ModuleEnchantmentSinkMK2() {
		_filterInventory.addListener(this);
	}
	
		private IHUDModuleRenderer HUD = new HUDSimpleFilterModule(this);
		
		private IRoutedPowerProvider _power;
		
		private final PlayerCollectionList localModeWatchers = new PlayerCollectionList();
		
		public ItemIdentifierInventory getFilterInventory(){
			return _filterInventory;
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

		
		private static final SinkReply _sinkReply = new SinkReply(FixedPriority.EnchantmentItemSink, 1, true, false, 1, 0);
		@Override
		public SinkReply sinksItem(ItemIdentifier item, int bestPriority, int bestCustomPriority, boolean allowDefault, boolean includeInTransit) {
			if(bestPriority > _sinkReply.fixedPriority.ordinal() || (bestPriority == _sinkReply.fixedPriority.ordinal() && bestCustomPriority >= _sinkReply.customPriority)) return null;
			if (_filterInventory.containsExcludeNBTItem(item.getUndamaged().getIgnoringNBT())){
				if(item.makeNormalStack(1).isItemEnchanted())
				{
					return _sinkReply;
				}
				return null;
			}
			return null;
		}

		@Override
		public int getGuiHandlerID() {
			return GuiIDs.GUI_Module_Simple_Filter_ID;
		}
		
		@Override
		public LogisticsModule getSubModule(int slot) {return null;}

		@Override
		public void readFromNBT(NBTTagCompound nbttagcompound) {
			_filterInventory.readFromNBT(nbttagcompound, "");
		}

		@Override
		public void writeToNBT(NBTTagCompound nbttagcompound) {
	    	_filterInventory.writeToNBT(nbttagcompound, "");
		}

		@Override
		public void tick() {}

		@Override
		public List<String> getClientInformation() {
			List<String> list = new ArrayList<String>();
			list.add("Filter: ");
			list.add("<inventory>");
			list.add("<that>");
			return list;
		}

		@Override
		public void startWatching() {
			MainProxy.sendPacketToServer(PacketHandler.getPacket(HUDStartModuleWatchingPacket.class).setInteger(slot).setPosX(getX()).setPosY(getY()).setPosZ(getZ()));
		}

		@Override
		public void stopWatching() {
			MainProxy.sendPacketToServer(PacketHandler.getPacket(HUDStartModuleWatchingPacket.class).setInteger(slot).setPosX(getX()).setPosY(getY()).setPosZ(getZ()));
		}

		@Override
		public void startWatching(EntityPlayer player) {
			localModeWatchers.add(player);
			MainProxy.sendPacketToPlayer(PacketHandler.getPacket(ModuleInventory.class).setSlot(slot).setIdentList(ItemIdentifierStack.getListFromInventory(_filterInventory)).setPosX(getX()).setPosY(getY()).setPosZ(getZ()), player);
		}

		@Override
		public void stopWatching(EntityPlayer player) {
			localModeWatchers.remove(player);
		}

		@Override
		public void InventoryChanged(IInventory inventory) {
			MainProxy.sendToPlayerList(PacketHandler.getPacket(ModuleInventory.class).setSlot(slot).setIdentList(ItemIdentifierStack.getListFromInventory(inventory)).setPosX(getX()).setPosY(getY()).setPosZ(getZ()), localModeWatchers);
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
		/*
		 * (non-Javadoc)
		 * @see logisticspipes.modules.LogisticsModule#hasGenericInterests()
		 * Only looking for items in filter
		 */
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
				li.add(id.getUndamaged().getIgnoringNBT());
			}
			return li;
		}

		@Override
		public boolean interestedInAttachedInventory() {		
			return false;
		}

		@Override
		public boolean interestedInUndamagedID() {
			return true;
		}


		@Override
		public boolean recievePassive() {
			return true;
		}

		@Override
		@SideOnly(Side.CLIENT)
		public IIcon getIconTexture(IIconRegister register) {
			return register.registerIcon("logisticspipes:itemModule/ModuleEnchantmentSinkMK2");
		}
		@Override
		public boolean hasEffect() {
			return true;
		}
	}
	