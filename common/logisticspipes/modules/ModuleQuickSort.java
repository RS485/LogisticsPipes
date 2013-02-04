package logisticspipes.modules;

import java.util.List;

import logisticspipes.interfaces.IChassiePowerProvider;
import logisticspipes.interfaces.ILogisticsModule;
import logisticspipes.interfaces.ISendRoutedItem;
import logisticspipes.interfaces.IWorldProvider;
import logisticspipes.interfaces.routing.IFilter;
import logisticspipes.logisticspipes.IInventoryProvider;
import logisticspipes.pipefxhandlers.Particles;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.Pair3;
import logisticspipes.utils.SinkReply;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class ModuleQuickSort implements ILogisticsModule {

	private final int stalledDelay = 24;
	private final int normalDelay = 6;
	private int currentTick = 0;
	private boolean stalled;
	private int lastStackLookedAt = 0;
	private int lastSuceededStack = 0;

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
	public SinkReply sinksItem(ItemStack item, int bestPriority, int bestCustomPriority) {
		return null;
	}

	@Override
	public ILogisticsModule getSubModule(int slot) {
		return null;
	}

	@Override
	public void tick() {

		if (--currentTick > 0) return;
		if(stalled)
			currentTick = stalledDelay;
		else
			currentTick = normalDelay;
		
		//Extract Item
		IInventory targetInventory = _invProvider.getPointedInventory();
		if (targetInventory == null) return;
		if (targetInventory.getSizeInventory() < 27) return;

		if(!_power.canUseEnergy(500)) {
			stalled = true;
			return;
		}
		
		if(lastSuceededStack >= targetInventory.getSizeInventory())
			lastSuceededStack = 0;
		
		//incremented at the end of the previous loop.
		if (lastStackLookedAt >= targetInventory.getSizeInventory())
			lastStackLookedAt = 0;
		
		ItemStack stackToSend = targetInventory.getStackInSlot(lastStackLookedAt);

		while(stackToSend==null) {
			lastStackLookedAt++;
			stackToSend = targetInventory.getStackInSlot(lastStackLookedAt);
			if (lastStackLookedAt >= targetInventory.getSizeInventory())
				lastStackLookedAt = 0;
			if(lastStackLookedAt == lastSuceededStack) {
				stalled = true;
				return; // then we have been around the list without sending, halt for now
			}
		}

		Pair3<Integer, SinkReply, List<IFilter>> reply = _itemSender.hasDestination(stackToSend, false);
		if (reply == null) {
			if(lastStackLookedAt == lastSuceededStack) {
				stalled = true;
			}
			lastStackLookedAt++;
			return;
		}
		if(!_power.useEnergy(500)) {
			stalled = true;
			lastStackLookedAt++;
			return;
		}
		lastSuceededStack=lastStackLookedAt;
		lastStackLookedAt++;
		
		stalled = false;
		_itemSender.sendStack(stackToSend, reply);
		MainProxy.sendSpawnParticlePacket(Particles.OrangeParticle, xCoord, yCoord, zCoord, _world.getWorld(), 8);
		targetInventory.setInventorySlotContents(lastStackLookedAt, null);
				
	}

	@Override
	public void registerPosition(int xCoord, int yCoord, int zCoord, int slot) {
		this.xCoord = xCoord;
		this.yCoord = yCoord;
		this.zCoord = zCoord;
	}
}
