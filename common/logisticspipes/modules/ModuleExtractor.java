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
import logisticspipes.interfaces.ILogisticsGuiModule;
import logisticspipes.interfaces.ILogisticsModule;
import logisticspipes.interfaces.IModuleWatchReciver;
import logisticspipes.interfaces.ISendRoutedItem;
import logisticspipes.interfaces.ISneakyDirectionReceiver;
import logisticspipes.interfaces.IWorldProvider;
import logisticspipes.interfaces.routing.IFilter;
import logisticspipes.logisticspipes.IInventoryProvider;
import logisticspipes.logisticspipes.SidedInventoryAdapter;
import logisticspipes.network.GuiIDs;
import logisticspipes.network.NetworkConstants;
import logisticspipes.network.packets.PacketModuleInteger;
import logisticspipes.network.packets.PacketPipeInteger;
import logisticspipes.pipefxhandlers.Particles;
import logisticspipes.pipes.basic.CoreRoutedPipe.ItemSendMode;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.utils.ItemIdentifier;
import logisticspipes.utils.Pair3;
import logisticspipes.utils.SinkReply;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.ISidedInventory;
import buildcraft.api.inventory.ISpecialInventory;

public class ModuleExtractor implements ILogisticsGuiModule, ISneakyDirectionReceiver, IClientInformationProvider, IHUDModuleHandler, IModuleWatchReciver {

	//protected final int ticksToAction = 100;
	private int currentTick = 0;

	private IInventoryProvider _invProvider;
	private ISendRoutedItem _itemSender;
	private IRoutedPowerProvider _power;
	private ForgeDirection _sneakyDirection = ForgeDirection.UNKNOWN;
	private IWorldProvider _world;

	private int slot = 0;
	private int xCoord = 0;
	private int yCoord = 0;
	private int zCoord = 0;

	private IHUDModuleRenderer HUD = new HUDExtractor(this);

	private final List<EntityPlayer> localModeWatchers = new ArrayList<EntityPlayer>();

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
		MainProxy.sendToPlayerList(new PacketModuleInteger(NetworkConstants.EXTRACTOR_MODULE_RESPONSE, xCoord, yCoord, zCoord, slot, _sneakyDirection.ordinal()).getPacket(), localModeWatchers);
	}

	@Override
	public SinkReply sinksItem(ItemIdentifier item, int bestPriority, int bestCustomPriority) {
		return null;
	}

	@Override
	public int getGuiHandlerID() {
		return GuiIDs.GUI_Module_Extractor_ID;
	}

	@Override
	public ILogisticsModule getSubModule(int slot) {return null;}

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
		IInventory targetInventory = _invProvider.getRawInventory();
		if (targetInventory == null) return;
		ForgeDirection extractOrientation = _sneakyDirection;
		if(extractOrientation == ForgeDirection.UNKNOWN) {
			extractOrientation = _invProvider.inventoryOrientation().getOpposite();
		}

		if (targetInventory instanceof ISpecialInventory){
			ItemStack[] stack = ((ISpecialInventory) targetInventory).extractItem(false, extractOrientation, 1);
			if (stack == null || stack.length < 1 || stack[0] == null || stack[0].stackSize == 0) return;
			Pair3<Integer, SinkReply, List<IFilter>> reply = _itemSender.hasDestination(ItemIdentifier.get(stack[0]), true, new ArrayList<Integer>());
			if (reply == null) return;
			ItemStack[] stacks = ((ISpecialInventory) targetInventory).extractItem(true, extractOrientation, 1);
			if (stacks == null || stacks.length < 1 || stacks[0] == null || stacks[0].stackSize == 0) {
				LogisticsPipes.log.info("extractor extractItem(true) got nothing from " + ((Object)targetInventory).toString());
				return;
			}
			if(!ItemStack.areItemStacksEqual(stack[0], stacks[0])) {
				LogisticsPipes.log.info("extractor extract got a unexpected item from " + ((Object)targetInventory).toString());
			}
			_itemSender.sendStack(stacks[0], reply, itemSendMode());
			return;
		}

		if (targetInventory instanceof ISidedInventory){
			targetInventory = new SidedInventoryAdapter((ISidedInventory) targetInventory, extractOrientation);
		}

		for (int i = 0; i < targetInventory.getSizeInventory(); i++){
			ItemStack slot = targetInventory.getStackInSlot(i);
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
					MainProxy.sendSpawnParticlePacket(Particles.OrangeParticle, this.xCoord, this.yCoord, this.zCoord, _world.getWorld(), 2);
					count--;
				}

				if(count <= 0) {
					break;
				}

				ItemStack stackToSend = targetInventory.decrStackSize(i, count);
				_itemSender.sendStack(stackToSend, reply, itemSendMode());
				itemsleft -= count;
				if(itemsleft <= 0) break;
				if(!SimpleServiceLocator.buildCraftProxy.checkMaxItems()) break;
				slot = targetInventory.getStackInSlot(i);
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
		MainProxy.sendToPlayerList(new PacketModuleInteger(NetworkConstants.EXTRACTOR_MODULE_RESPONSE, xCoord, yCoord, zCoord, slot, _sneakyDirection.ordinal()).getPacket(), localModeWatchers);
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
	public int getZPos() {
		return zCoord;
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
}
