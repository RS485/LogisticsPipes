package logisticspipes.modules;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import logisticspipes.api.IRoutedPowerProvider;
import logisticspipes.gui.hud.modules.HUDAdvancedExtractor;
import logisticspipes.interfaces.IClientInformationProvider;
import logisticspipes.interfaces.IHUDModuleHandler;
import logisticspipes.interfaces.IHUDModuleRenderer;
import logisticspipes.interfaces.IInventoryUtil;
import logisticspipes.interfaces.IModuleInventoryReceive;
import logisticspipes.interfaces.IModuleWatchReciver;
import logisticspipes.interfaces.ISendRoutedItem;
import logisticspipes.interfaces.ISneakyDirectionReceiver;
import logisticspipes.interfaces.IWorldProvider;
import logisticspipes.interfaces.routing.IFilter;
import logisticspipes.logisticspipes.IInventoryProvider;
import logisticspipes.network.GuiIDs;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.hud.HUDStartModuleWatchingPacket;
import logisticspipes.network.packets.module.ModuleInventory;
import logisticspipes.network.packets.modules.AdvancedExtractorInclude;
import logisticspipes.network.packets.modules.ExtractorModuleMode;
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
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Icon;
import net.minecraftforge.common.ForgeDirection;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ModuleAdvancedExtractor extends LogisticsGuiModule implements ISneakyDirectionReceiver, IClientInformationProvider, IHUDModuleHandler, IModuleWatchReciver, IModuleInventoryReceive, ISimpleInventoryEventHandler {

	protected int currentTick = 0;

	private final SimpleInventory _filterInventory = new SimpleInventory(9, "Item list", 1);
	private boolean _itemsIncluded = true;
	protected IInventoryProvider _invProvider;
	protected ISendRoutedItem _itemSender;
	protected IRoutedPowerProvider _power;
	private ForgeDirection _sneakyDirection = ForgeDirection.UNKNOWN;

	private int slot = 0;
	private IWorldProvider _world;

	private IHUDModuleRenderer HUD = new HUDAdvancedExtractor(this);

	private final PlayerCollectionList localModeWatchers = new PlayerCollectionList();


	public ModuleAdvancedExtractor() {
		_filterInventory.addListener(this);
	}

	@Override
	public void registerHandler(IInventoryProvider invProvider, ISendRoutedItem itemSender, IWorldProvider world, IRoutedPowerProvider powerprovider) {
		_invProvider = invProvider;
		_itemSender = itemSender;
		_power = powerprovider;
		_world = world;
	}

	public SimpleInventory getFilterInventory() {
		return _filterInventory;
	}

	@Override
	public ForgeDirection getSneakyDirection(){
		return _sneakyDirection;
	}

	@Override
	public void setSneakyDirection(ForgeDirection sneakyDirection){
		_sneakyDirection = sneakyDirection;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		_filterInventory.readFromNBT(nbttagcompound);
		setItemsIncluded(nbttagcompound.getBoolean("itemsIncluded"));
		if(nbttagcompound.hasKey("sneakydirection")) {
			_sneakyDirection = ForgeDirection.values()[nbttagcompound.getInteger("sneakydirection")];
		} else if(nbttagcompound.hasKey("sneakyorientation")) {
			//convert sneakyorientation to sneakydirection
			int t = nbttagcompound.getInteger("sneakyorientation");
			switch(t) {
			default:
			case 0:
				_sneakyDirection = ForgeDirection.UNKNOWN;
				break;
			case 1:
				_sneakyDirection = ForgeDirection.UP;
				break;
			case 2:
				_sneakyDirection = ForgeDirection.SOUTH;
				break;
			case 3:
				_sneakyDirection = ForgeDirection.DOWN;
				break;
			}
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		_filterInventory.writeToNBT(nbttagcompound);
		nbttagcompound.setBoolean("itemsIncluded", areItemsIncluded());
		nbttagcompound.setInteger("sneakydirection", _sneakyDirection.ordinal());
	}

	@Override
	public int getGuiHandlerID() {
		return GuiIDs.GUI_Module_Advanced_Extractor_ID;
	}

	@Override
	public SinkReply sinksItem(ItemIdentifier item, int bestPriority, int bestCustomPriority, boolean allowDefault, boolean includeInTransit) {
		return null;
	}

	@Override
	public LogisticsModule getSubModule(int slot) {
		return null;
	}

	protected int ticksToAction() {
		return 100;
	}

	protected int itemsToExtract() {
		return 1;
	}

	protected int neededEnergy() {
		return 6;
	}

	protected ItemSendMode itemSendMode() {
		return ItemSendMode.Normal;
	}

	@Override
	public void tick() {
		if (++currentTick < ticksToAction())
			return;
		currentTick = 0;

		ForgeDirection extractOrientation = _sneakyDirection;
		if(extractOrientation == ForgeDirection.UNKNOWN) {
			extractOrientation = _invProvider.inventoryOrientation().getOpposite();
		}
		IInventoryUtil inventory = _invProvider.getSneakyInventory(extractOrientation,true);
		if (inventory == null) return;

		checkExtract(inventory);
	}

	private void checkExtract(IInventoryUtil invUtil) {
		Map<ItemIdentifier, Integer> items = invUtil.getItemsAndCount();
		for (Entry<ItemIdentifier, Integer> item :items.entrySet()) {
			if(!CanExtract(item.getKey().makeNormalStack(item.getValue())))
				continue;
			List<Integer> jamList = new LinkedList<Integer>();
			Pair3<Integer, SinkReply, List<IFilter>> reply = _itemSender.hasDestination(item.getKey(), true, jamList);
			if (reply == null) continue;

			int itemsleft = itemsToExtract();
			while(reply != null) {
				int count = Math.min(itemsleft, item.getValue());
				if(reply.getValue2().maxNumberOfItems > 0) {
					count = Math.min(count, reply.getValue2().maxNumberOfItems);
				}

				while(!_power.useEnergy(neededEnergy() * count) && count > 0) {
					MainProxy.sendSpawnParticlePacket(Particles.OrangeParticle, this.getX(), this.getY(), this.getZ(), _world.getWorld(), 2);
					count--;
				}

				if(count <= 0) {
					break;
				}

				ItemStack stackToSend = invUtil.getMultipleItems(item.getKey(), item.getValue());
				_itemSender.sendStack(stackToSend, reply, itemSendMode());
				itemsleft -= count;
				if(itemsleft <= 0) break;
				if(!SimpleServiceLocator.buildCraftProxy.checkMaxItems()) break;


				jamList.add(reply.getValue1());
				reply = _itemSender.hasDestination(item.getKey(), true, jamList);
			}
			return;
		}
	}

	public boolean CanExtract(ItemStack item) {
		for (int i = 0; i < this._filterInventory.getSizeInventory(); i++) {

			ItemStack stack = this._filterInventory.getStackInSlot(i);
			if ((stack != null) && (stack.itemID == item.itemID)) {
				if (Item.itemsList[item.itemID].isDamageable()) {
					return areItemsIncluded();
				}
				if (stack.getItemDamage() == item.getItemDamage()) {
					return areItemsIncluded();
				}
			}
		}
		return !areItemsIncluded();
	}

	public boolean areItemsIncluded() {
		return _itemsIncluded;
	}

	public void setItemsIncluded(boolean flag) {
		_itemsIncluded = flag;
//TODO 	MainProxy.sendToPlayerList(new PacketModuleInteger(NetworkConstants.ADVANCED_EXTRACTOR_MODULE_INCLUDED_RESPONSE, getX(), getY(), getZ(), slot, areItemsIncluded() ? 1 : 0).getPacket(), localModeWatchers);
		MainProxy.sendToPlayerList(PacketHandler.getPacket(AdvancedExtractorInclude.class).setInteger2(slot).setInteger(areItemsIncluded() ? 1 : 0).setPosX(getX()).setPosY(getY()).setPosZ(getZ()), localModeWatchers);
	}

	@Override
	public List<String> getClientInformation() {
		List<String> list = new ArrayList<String>(5);
		list.add(areItemsIncluded() ? "Included" : "Excluded");
		list.add("Extraction: " + ((_sneakyDirection == ForgeDirection.UNKNOWN) ? "DEFAULT" : _sneakyDirection.name()));
		list.add("Filter: ");
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
	public void InventoryChanged(SimpleInventory inventory) {
//TODO 	MainProxy.sendToPlayerList(new PacketModuleInvContent(NetworkConstants.MODULE_INV_CONTENT, getX(), getY(), getZ(), slot, ItemIdentifierStack.getListFromInventory(inventory)).getPacket(), localModeWatchers);
		MainProxy.sendToPlayerList(PacketHandler.getPacket(ModuleInventory.class).setSlot(slot).setIdentList(ItemIdentifierStack.getListFromInventory(inventory)).setPosX(getX()).setPosY(getY()).setPosZ(getZ()), localModeWatchers);
	}

	@Override
	public void handleInvContent(Collection<ItemIdentifierStack> list) {
		_filterInventory.handleItemIdentifierList(list);
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
//TODO 	MainProxy.sendPacketToPlayer(new PacketModuleInteger(NetworkConstants.EXTRACTOR_MODULE_RESPONSE, getX(), getY(), getZ(), slot, _sneakyDirection.ordinal()).getPacket(), (Player)player);
		MainProxy.sendPacketToPlayer(PacketHandler.getPacket(ExtractorModuleMode.class).setInteger2(slot).setInteger(_sneakyDirection.ordinal()).setPosX(getX()).setPosY(getY()).setPosZ(getZ()), (Player)player);
//TODO 	MainProxy.sendPacketToPlayer(new PacketModuleInteger(NetworkConstants.ADVANCED_EXTRACTOR_MODULE_INCLUDED_RESPONSE, getX(), getY(), getZ(), slot, areItemsIncluded() ? 1 : 0).getPacket(), (Player)player);
		MainProxy.sendPacketToPlayer(PacketHandler.getPacket(AdvancedExtractorInclude.class).setInteger2(slot).setInteger(areItemsIncluded() ? 1 : 0).setPosX(getX()).setPosY(getY()).setPosZ(getZ()), (Player)player);
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
	public boolean hasGenericInterests() {
		return false;
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
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Icon getIconTexture(IconRegister register) {
		return register.registerIcon("logisticspipes:itemModule/ModuleAdvancedExtractor");
	}
}
