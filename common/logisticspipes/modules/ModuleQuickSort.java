package logisticspipes.modules;

import logisticspipes.interfaces.IChassiePowerProvider;
import logisticspipes.interfaces.ILogisticsModule;
import logisticspipes.interfaces.ISendRoutedItem;
import logisticspipes.interfaces.IWorldProvider;
import logisticspipes.logisticspipes.IInventoryProvider;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.utils.SinkReply;
import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;

public class ModuleQuickSort implements ILogisticsModule {

	private final int ticksToAction = 100;
	private int currentTick = 0;
	private boolean sent;
	private int ticksToResend = 0;
	
	private IInventoryProvider _invProvider;
	private ISendRoutedItem _itemSender;
	private IChassiePowerProvider _power;
	int x;
	int y;
	int z;
	
	public ModuleQuickSort() {}

	@Override
	public void registerHandler(IInventoryProvider invProvider, ISendRoutedItem itemSender, IWorldProvider world, IChassiePowerProvider powerprovider) {
		_invProvider = invProvider;
		_itemSender = itemSender;
		_power = powerprovider;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound, String prefix) {}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound, String prefix) {}

	@Override
	public int getGuiHandlerID() {
		return -1;
	}
	
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
			MainProxy.proxy.spawnGenericParticle("VioletParticle", this.x, this.y, this.z, 8);
			targetInventory.setInventorySlotContents(i, null);
			
			sent = true;
			break;
		}		
	}
	
	private boolean shouldSend(ItemStack stack){
		return SimpleServiceLocator.logisticsManager.hasDestination(stack, false, _itemSender.getSourceUUID(), true);
	}

	@Override
	public void registerPosition(int xCoord, int yCoord, int zCoord, int slot) {
		this.x = xCoord;
		this.y = yCoord;
		this.z = zCoord;
	}
}
