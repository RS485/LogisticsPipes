package logisticspipes.modules;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import logisticspipes.api.IRoutedPowerProvider;
import logisticspipes.interfaces.IInventoryUtil;
import logisticspipes.interfaces.ILogisticsModule;
import logisticspipes.interfaces.ISendRoutedItem;
import logisticspipes.interfaces.IWorldProvider;
import logisticspipes.interfaces.routing.IFilter;
import logisticspipes.logisticspipes.IInventoryProvider;
import logisticspipes.pipefxhandlers.Particles;
import logisticspipes.pipes.basic.CoreRoutedPipe.ItemSendMode;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.proxy.specialinventoryhandler.SpecialInventoryHandler;
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
	private IRoutedPowerProvider _power;
	private int xCoord;
	private int yCoord;
	private int zCoord;
	private IWorldProvider _world;
	private ItemIdentifier lastItemKey;

	public ModuleQuickSort() {}

	@Override
	public void registerHandler(IInventoryProvider invProvider, ISendRoutedItem itemSender, IWorldProvider world, IRoutedPowerProvider powerprovider) {
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
//		if (targetInventory.getSizeInventory() < 27) return;

		if(!_power.canUseEnergy(500)) {
			stalled = true;
			return;
		}
		IInventoryUtil invUtil = SimpleServiceLocator.inventoryUtilFactory.getInventoryUtil(targetInventory);
		if(invUtil instanceof SpecialInventoryHandler){
			int maxItemsToSend = 128;
			HashMap<ItemIdentifier, Integer> items = invUtil.getItemsAndCount();
			if(lastStackLookedAt>items.size())
				lastStackLookedAt=0;
			int lookedAt = 0;
			for (Entry<ItemIdentifier, Integer> item :items.entrySet()) {
				lookedAt++;
				if(item.getKey()==lastItemKey) {// spool to current place
					lastStackLookedAt=lookedAt;
					break;
				}
			}
			lookedAt=0;
			for (Entry<ItemIdentifier, Integer> item :items.entrySet()) {
				if(lookedAt < lastStackLookedAt) // spool to current place
					continue;
				
				LinkedList<Integer> jamList =  new LinkedList<Integer>();
				Pair3<Integer, SinkReply, List<IFilter>> reply = _itemSender.hasDestination(item.getKey(), false, jamList);
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
		
				boolean partialSend=false;
				int availableItems = Math.min(maxItemsToSend, item.getValue());
				while(reply != null) {
					int count = Math.min(availableItems, reply.getValue2().maxNumberOfItems);
					ItemStack stackToSend = invUtil.getMultipleItems(item.getKey(), availableItems);
		
					_itemSender.sendStack(stackToSend, reply, ItemSendMode.Fast);
					availableItems-=stackToSend.stackSize;
					
					MainProxy.sendSpawnParticlePacket(Particles.OrangeParticle, xCoord, yCoord, zCoord, _world.getWorld(), 8);
		
					if(availableItems <= 0) break;
					if(!SimpleServiceLocator.buildCraftProxy.checkMaxItems()) break;
		
					jamList.add(reply.getValue1());
					reply = _itemSender.hasDestination(item.getKey(), false, jamList);
				}
				lastSuceededStack=lastStackLookedAt-1;
				if(lastStackLookedAt==-1)
					lastStackLookedAt = items.size();

				// no need to increment, as removing an item type from the list will pull the earlier ones down, or there is more left of this type to send, so we shouldn't move on.
			}
		} else {
			
			if((!(invUtil instanceof SpecialInventoryHandler) && targetInventory.getSizeInventory() == 0) || !_power.canUseEnergy(500)) {
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
	
			// begin duplicate code
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
				if(!SimpleServiceLocator.buildCraftProxy.checkMaxItems()) break;
	
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
			// end duplicate code
			lastStackLookedAt++;
			if(partialSend){
				if (lastStackLookedAt >= targetInventory.getSizeInventory())
					lastStackLookedAt = 0;
				while(lastStackLookedAt != lastSuceededStack) {
					ItemStack tstack = targetInventory.getStackInSlot(lastStackLookedAt);
					if(tstack != null && !slot.isItemEqual(tstack))
						break;
					lastStackLookedAt++;
					if (lastStackLookedAt >= targetInventory.getSizeInventory())
						lastStackLookedAt = 0;
					
				}
			}
		}
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

	@Override
	public boolean recievePassive() {
		return true;
	}
}
