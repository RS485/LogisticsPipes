package logisticspipes.modules;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import logisticspipes.LogisticsPipes;
import logisticspipes.api.IRoutedPowerProvider;
import logisticspipes.gui.hud.modules.HUDExtractor;
import logisticspipes.interfaces.IClientInformationProvider;
import logisticspipes.interfaces.IHUDModuleHandler;
import logisticspipes.interfaces.IHUDModuleRenderer;
import logisticspipes.interfaces.IInventoryUtil;
import logisticspipes.interfaces.IModuleWatchReciver;
import logisticspipes.interfaces.ISendRoutedItem;
import logisticspipes.interfaces.ISneakyDirectionReceiver;
import logisticspipes.interfaces.IWorldProvider;
import logisticspipes.interfaces.routing.IFilter;
import logisticspipes.logisticspipes.IInventoryProvider;
import logisticspipes.network.GuiIDs;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.hud.HUDStartModuleWatchingPacket;
import logisticspipes.network.packets.modules.ExtractorModuleMode;
import logisticspipes.pipefxhandlers.Particles;
import logisticspipes.pipes.basic.CoreRoutedPipe.ItemSendMode;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.utils.ItemIdentifier;
import logisticspipes.utils.Pair3;
import logisticspipes.utils.PlayerCollectionList;
import logisticspipes.utils.SinkReply;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Icon;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.api.inventory.ISpecialInventory;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ModuleExtractor extends LogisticsGuiModule implements ISneakyDirectionReceiver, IClientInformationProvider, IHUDModuleHandler, IModuleWatchReciver {

	//protected final int ticksToAction = 100;
	private int currentTick = 0;

	private IInventoryProvider _invProvider;
	private ISendRoutedItem _itemSender;
	private IRoutedPowerProvider _power;
	private ForgeDirection _sneakyDirection = ForgeDirection.UNKNOWN;
	private IWorldProvider _world;

	private int slot = 0;




	private IHUDModuleRenderer HUD = new HUDExtractor(this);

	private final PlayerCollectionList localModeWatchers = new PlayerCollectionList();

	public ModuleExtractor() {

	}

	@Override
	public void registerHandler(IInventoryProvider invProvider, ISendRoutedItem itemSender, IWorldProvider world, IRoutedPowerProvider powerprovider) {
		_invProvider = invProvider;
		_itemSender = itemSender;
		_power = powerprovider;
		_world = world;
	}

	protected int ticksToAction(){
		return 100;
	}

	protected int itemsToExtract(){
		return 1;
	}

	protected int neededEnergy() {
		return 5;
	}

	protected ItemSendMode itemSendMode() {
		return ItemSendMode.Normal;
	}

	@Override
	public ForgeDirection getSneakyDirection(){
		return _sneakyDirection;
	}

	@Override
	public void setSneakyDirection(ForgeDirection sneakyDirection){
		_sneakyDirection = sneakyDirection;
//TODO 	MainProxy.sendToPlayerList(new PacketModuleInteger(NetworkConstants.EXTRACTOR_MODULE_RESPONSE, getX(), getY(), getZ(), slot, _sneakyDirection.ordinal()).getPacket(), localModeWatchers);
		MainProxy.sendToPlayerList(PacketHandler.getPacket(ExtractorModuleMode.class).setInteger2(slot).setInteger(_sneakyDirection.ordinal()).setPosX(getX()).setPosY(getY()).setPosZ(getZ()), localModeWatchers);
	}

	@Override
	public SinkReply sinksItem(ItemIdentifier item, int bestPriority, int bestCustomPriority, boolean allowDefault, boolean includeInTransit) {
		return null;
	}

	@Override
	public int getGuiHandlerID() {
		return GuiIDs.GUI_Module_Extractor_ID;
	}

	@Override
	public LogisticsModule getSubModule(int slot) {return null;}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
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
		nbttagcompound.setInteger("sneakydirection", _sneakyDirection.ordinal());
	}

	@Override
	public void tick() {
		if (++currentTick < ticksToAction()) return;
		currentTick = 0;

		//Extract Item
		IInventory realInventory = _invProvider.getRealInventory();
		if (realInventory == null) return;
		ForgeDirection extractOrientation = _sneakyDirection;
		if(extractOrientation == ForgeDirection.UNKNOWN) {
			extractOrientation = _invProvider.inventoryOrientation().getOpposite();
		}

		IInventoryUtil targetUtil = _invProvider.getSneakyInventory(extractOrientation,true);
		
		if (realInventory instanceof ISpecialInventory && !targetUtil.isSpecialInventory()){
			ItemStack[] stack = ((ISpecialInventory) realInventory).extractItem(false, extractOrientation, 1);
			if (stack == null || stack.length < 1 || stack[0] == null || stack[0].stackSize == 0) return;
			Pair3<Integer, SinkReply, List<IFilter>> reply = _itemSender.hasDestination(ItemIdentifier.get(stack[0]), true, new ArrayList<Integer>());
			if (reply == null) return;
			ItemStack[] stacks = ((ISpecialInventory) realInventory).extractItem(true, extractOrientation, 1);
			if (stacks == null || stacks.length < 1 || stacks[0] == null || stacks[0].stackSize == 0) {
				LogisticsPipes.log.info("extractor extractItem(true) got nothing from " + ((Object)realInventory).toString());
				return;
			}
			if(!ItemStack.areItemStacksEqual(stack[0], stacks[0])) {
				LogisticsPipes.log.info("extractor extract got a unexpected item from " + ((Object)realInventory).toString());
			}
			_itemSender.sendStack(stacks[0], reply, itemSendMode());
			return;
		}

		
		for (int i = 0; i < targetUtil.getSizeInventory(); i++){
			
			ItemStack slot = targetUtil.getStackInSlot(i);
			if (slot == null) continue;

			List<Integer> jamList = new LinkedList<Integer>();
			Pair3<Integer, SinkReply, List<IFilter>> reply = _itemSender.hasDestination(ItemIdentifier.get(slot), true, jamList);
			if (reply == null) continue;

			int itemsleft = itemsToExtract();
			while(reply != null) {
				int count = Math.min(itemsleft, slot.stackSize);
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

				ItemStack stackToSend = targetUtil.decrStackSize(i, count);
				_itemSender.sendStack(stackToSend, reply, itemSendMode());
				itemsleft -= count;
				if(itemsleft <= 0) break;
				if(!SimpleServiceLocator.buildCraftProxy.checkMaxItems()) break;
				slot = targetUtil.getStackInSlot(i);
				if (slot == null) break;
				jamList.add(reply.getValue1());
				reply = _itemSender.hasDestination(ItemIdentifier.get(slot), true, jamList);
			}
			break;
		}
	}

	@Override
	public List<String> getClientInformation() {
		List<String> list = new ArrayList<String>(1);
		list.add("Extraction: " + ((_sneakyDirection == ForgeDirection.UNKNOWN) ? "DEFAULT" : _sneakyDirection.name()));
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
//TODO 	MainProxy.sendToPlayerList(new PacketModuleInteger(NetworkConstants.EXTRACTOR_MODULE_RESPONSE, getX(), getY(), getZ(), slot, _sneakyDirection.ordinal()).getPacket(), localModeWatchers);
		MainProxy.sendToPlayerList(PacketHandler.getPacket(ExtractorModuleMode.class).setInteger2(slot).setInteger(_sneakyDirection.ordinal()).setPosX(getX()).setPosY(getY()).setPosZ(getZ()), localModeWatchers);
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
		return register.registerIcon("logisticspipes:itemModule/ModuleExtractor");
	}
}
