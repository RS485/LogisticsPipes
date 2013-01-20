package logisticspipes.modules;

import logisticspipes.interfaces.IChassiePowerProvider;
import logisticspipes.interfaces.ILogisticsModule;
import logisticspipes.interfaces.ISendRoutedItem;
import logisticspipes.interfaces.IWorldProvider;
import logisticspipes.logisticspipes.IInventoryProvider;
import logisticspipes.pipefxhandlers.Particles;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.utils.SinkReply;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class ModuleQuickSort implements ILogisticsModule {

	private final int ticksToAction = 100;
	private int currentTick = 0;
	private boolean sent;
	private int ticksToResend = 0;
	
	private IInventoryProvider _invProvider;
	private ISendRoutedItem _itemSender;
	private IChassiePowerProvider _power;
	private int xCoord;
	private int yCoord;
	private int zCoord;
	private IWorldProvider _world;
	
	public ModuleQuickSort() {}

	@Override
	public void registerHandler(IInventoryProvider invProvider, ISendRoutedItem itemSender, IWorldProvider world, IChassiePowerProvider powerprovider) {
		_invProvider = invProvider;
		_itemSender = itemSender;
		_power = powerprovider;
		_world = world;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {}
	
	@Override
	public SinkReply sinksItem(ItemStack item) {
		return null;
	}

	@Override
	public ILogisticsModule getSubModule(int slot) {
		return null;
	}

	@Override
	public void tick() {
		if(MainProxy.isClient()) return;
		if (sent){
			ticksToResend = 6;
			sent = false;
		}
		
		if (ticksToResend > 0 && --ticksToResend < 1){
			currentTick = ticksToAction;
		}
		
		if (++currentTick < ticksToAction) return;
		currentTick = 0;
		
		//Extract Item
		IInventory targetInventory = _invProvider.getInventory();
		if (targetInventory == null) return;
		if (targetInventory.getSizeInventory() < 27) return;
		ItemStack stackToSend;
		for (int i = 0; i < targetInventory.getSizeInventory(); i++){
			stackToSend = targetInventory.getStackInSlot(i);
			if (stackToSend == null) continue;
			if (!this.shouldSend(stackToSend)) continue;
			if(!_power.useEnergy(500)) break;
			_itemSender.sendStack(stackToSend);
			MainProxy.sendSpawnParticlePacket(Particles.VioletParticle, xCoord, yCoord, this.zCoord, _world.getWorld(), 8);
			targetInventory.setInventorySlotContents(i, null);
			
			sent = true;
			break;
		}		
	}
	
	private boolean shouldSend(ItemStack stack){
		return SimpleServiceLocator.logisticsManager.hasDestination(stack, false, _itemSender.getRouter().getSimpleID(), true);
	}

	@Override
	public void registerPosition(int xCoord, int yCoord, int zCoord, int slot) {
		this.xCoord = xCoord;
		this.yCoord = yCoord;
		this.zCoord = zCoord;
	}
}
