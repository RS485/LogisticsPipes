package logisticspipes.modules;

import java.util.UUID;

import logisticspipes.interfaces.IChassiePowerProvider;
import logisticspipes.interfaces.ILogisticsModule;
import logisticspipes.interfaces.ISendRoutedItem;
import logisticspipes.interfaces.IWorldProvider;
import logisticspipes.logisticspipes.IInventoryProvider;
import logisticspipes.logisticspipes.IRoutedItem;
import logisticspipes.logisticspipes.IRoutedItem.TransportMode;
import logisticspipes.pipefxhandlers.Particles;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.utils.SinkReply;
import logisticspipes.utils.SinkReply.FixedPriority;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.ForgeDirection;

public class ModuleApiaristRefiller implements ILogisticsModule {
	
	private IInventoryProvider _invProvider;
	private IChassiePowerProvider _power;
	private ISendRoutedItem _itemSender;
	private int xCoord;
	private int yCoord;
	private int zCoord;
	private IWorldProvider _world;
	
	private int currentTickToRefill = 0;
	private int ticksToRefill = 100;
	
	private int currentTickToExtract = 0;
	private int ticksToExtract = 200;
	
	public ModuleApiaristRefiller() {}
	
	@Override
	public void registerHandler(IInventoryProvider invProvider, ISendRoutedItem itemSender, IWorldProvider world, IChassiePowerProvider powerProvider) {
		_invProvider = invProvider;
		_power = powerProvider;
		_world = world;
		_itemSender = itemSender;
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
	public void readFromNBT(NBTTagCompound nbttagcompound) {}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {}
	
	@Override
	public void registerPosition(int xCoord, int yCoord, int zCoord, int slot) {
		this.xCoord = xCoord;
		this.yCoord = yCoord;
		this.zCoord = zCoord;
	}

	@Override
	public void tick() {
		cleanupInventory();
		if (++currentTickToRefill < ticksToRefill) return;
		currentTickToRefill = 0;
		if (!_power.useEnergy(1)) return;
		IInventory inv = _invProvider.getRawInventory();
		if (inv == null) return;
		refillQueenSlot(inv);
		refillDroneSlot(inv);
	}

	private void cleanupInventory() {
		if (++currentTickToExtract < ticksToExtract) return;
		currentTickToExtract = 0;
		if (!_power.useEnergy(1)) return;
		IInventory inv = _invProvider.getRawInventory();
		if (inv == null) return;
		if (inv.getStackInSlot(0) == null) return;
		int size = inv.getSizeInventory();
		for (int i = 2; i < size; i++) {
			ItemStack stackInSlot = inv.getStackInSlot(i);
			if (stackInSlot != null) {
				if (!_power.useEnergy(10)) return;
				_itemSender.sendStack(stackInSlot.splitStack(1));
				if (stackInSlot.stackSize < 1) {
					inv.setInventorySlotContents(i, null);
				} else {
					inv.setInventorySlotContents(i, stackInSlot);
				}
				break;
			}
		}

	}

	private void refillQueenSlot(IInventory inv) {
		if (inv.getStackInSlot(0) != null) return;
		int size = inv.getSizeInventory();
		//Start checking slots, starting at 2, because 0 and 1 are breeding slots.
		for (int i = 2; i < size; i++) {
			ItemStack stackInSlot = inv.getStackInSlot(i);
			if (SimpleServiceLocator.forestryProxy.isBee(stackInSlot)) {
				if (SimpleServiceLocator.forestryProxy.isPrincess(stackInSlot)) {
					//move the stackInSlot to princess slot
					if (!_power.useEnergy(50)) return;
					inv.setInventorySlotContents(0, stackInSlot);
					inv.setInventorySlotContents(i, null);
					break;
				}
			}
		}
	}

	private void refillDroneSlot(IInventory inv) {
		if (SimpleServiceLocator.forestryProxy.isQueen(inv.getStackInSlot(0))) return;
		if (inv.getStackInSlot(1) != null) return;
		int size = inv.getSizeInventory();
		//Start checking slots, starting at 2 because 0 and 1 are breeding slots.
		for (int i = 2; i<size; i++) {
			ItemStack stackInSlot = inv.getStackInSlot(i);
			if (SimpleServiceLocator.forestryProxy.isBee(stackInSlot)) {
				if (SimpleServiceLocator.forestryProxy.isDrone(stackInSlot)) {
					//move the stackInSlot to princess slot
					if (!_power.useEnergy(50)) return;
					inv.setInventorySlotContents(0, stackInSlot);
					inv.setInventorySlotContents(i, null);
					break;
				}
			}

		}
	}
}
