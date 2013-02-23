package logisticspipes.modules;

import java.util.LinkedList;
import java.util.List;

import logisticspipes.interfaces.IChassiePowerProvider;
import logisticspipes.interfaces.ILogisticsModule;
import logisticspipes.interfaces.ISendRoutedItem;
import logisticspipes.interfaces.IWorldProvider;
import logisticspipes.interfaces.routing.IFilter;
import logisticspipes.logisticspipes.IInventoryProvider;
import logisticspipes.pipefxhandlers.Particles;
import logisticspipes.pipes.basic.CoreRoutedPipe.ItemSendMode;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.ItemIdentifier;
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
	public SinkReply sinksItem(ItemIdentifier item, int bestPriority, int bestCustomPriority) {
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
		
		ItemStack slot = targetInventory.getStackInSlot(lastStackLookedAt);

		while(slot==null) {
			lastStackLookedAt++;
			if (lastStackLookedAt >= targetInventory.getSizeInventory())
				lastStackLookedAt = 0;
			slot = targetInventory.getStackInSlot(lastStackLookedAt);
			if(lastStackLookedAt == lastSuceededStack) {
				stalled = true;
				return; // then we have been around the list without sending, halt for now
			}
		}

		List<Integer> jamList = new LinkedList<Integer>();
		Pair3<Integer, SinkReply, List<IFilter>> reply = _itemSender.hasDestination(ItemIdentifier.get(slot), false, jamList);
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
		
		stalled = false;

		//don't directly modify the stack in the inv
		slot = slot.copy();
		boolean partialSend=false;
		while(reply != null) {
			int count = slot.stackSize;
			if(reply.getValue2().maxNumberOfItems > 0) {
				count = Math.min(count, reply.getValue2().maxNumberOfItems);
			}
			ItemStack stackToSend = slot.splitStack(count);

			_itemSender.sendStack(stackToSend, reply, ItemSendMode.Fast);
			MainProxy.sendSpawnParticlePacket(Particles.OrangeParticle, xCoord, yCoord, zCoord, _world.getWorld(), 8);

			if(slot.stackSize == 0) break;

			jamList.add(reply.getValue1());
			reply = _itemSender.hasDestination(ItemIdentifier.get(slot), false, jamList);
		}
		if(slot.stackSize > 0) {
			partialSend = true;
			targetInventory.setInventorySlotContents(lastStackLookedAt, slot);
		} else {
			targetInventory.setInventorySlotContents(lastStackLookedAt, null);
		}

		lastSuceededStack=lastStackLookedAt;
		if(partialSend){
			lastStackLookedAt++;
			if (lastStackLookedAt >= targetInventory.getSizeInventory())
				lastStackLookedAt = 0;
			while(slot.isItemEqual(targetInventory.getStackInSlot(lastStackLookedAt)) && lastStackLookedAt != lastSuceededStack) {
				lastStackLookedAt++;
				if (lastStackLookedAt >= targetInventory.getSizeInventory())
					lastStackLookedAt = 0;
				
			}
		}
		else
			lastStackLookedAt++;
	}

	@Override
	public void registerPosition(int xCoord, int yCoord, int zCoord, int slot) {
		this.xCoord = xCoord;
		this.yCoord = yCoord;
		this.zCoord = zCoord;
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
}
