package logisticspipes.modules;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import logisticspipes.api.IRoutedPowerProvider;
import logisticspipes.interfaces.IInventoryUtil;
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
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Icon;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ModuleQuickSort extends LogisticsModule {

	private final int stalledDelay = 24;
	private final int normalDelay = 6;
	private int currentTick = 0;
	private boolean stalled;
	private int lastStackLookedAt = 0;
	private int lastSuceededStack = 0;

	private IInventoryProvider _invProvider;
	private ISendRoutedItem _itemSender;
	private IRoutedPowerProvider _power;



	private IWorldProvider _world;

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
	public SinkReply sinksItem(ItemIdentifier item, int bestPriority, int bestCustomPriority, boolean allowDefault, boolean includeInTransit) {
		return null;
	}

	@Override
	public LogisticsModule getSubModule(int slot) {
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
		IInventoryUtil invUtil = _invProvider.getPointedInventory(true);
		if (invUtil == null) return;

		if(!_power.canUseEnergy(500)) {
			stalled = true;
			return;
		}
		
		if(invUtil instanceof SpecialInventoryHandler){
			Map<ItemIdentifier, Integer> items = invUtil.getItemsAndCount();
			if(lastSuceededStack >= items.size())
				lastSuceededStack = 0;
			if (lastStackLookedAt >= items.size())
				lastStackLookedAt = 0;
			int lookedAt = 0;
			for (Entry<ItemIdentifier, Integer> item :items.entrySet()) {
				// spool to current place
				lookedAt++;
				if(lookedAt <= lastStackLookedAt)
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
		
				//send up to one stack
				int maxItemsToSend = item.getKey().getMaxStackSize();
				int availableItems = Math.min(maxItemsToSend, item.getValue());
				while(reply != null) {
					int count = availableItems;
					if(reply.getValue2().maxNumberOfItems != 0) {
						count = Math.min(count, reply.getValue2().maxNumberOfItems);
					}
					ItemStack stackToSend = invUtil.getMultipleItems(item.getKey(), count);
		
					availableItems -= stackToSend.stackSize;
					_itemSender.sendStack(stackToSend, reply, ItemSendMode.Fast);
					
					MainProxy.sendSpawnParticlePacket(Particles.OrangeParticle, getX(), getY(), getZ(), _world.getWorld(), 8);
		
					if(availableItems <= 0) break;
					if(!SimpleServiceLocator.buildCraftProxy.checkMaxItems()) break;
		
					jamList.add(reply.getValue1());
					reply = _itemSender.hasDestination(item.getKey(), false, jamList);
				}
				if(availableItems > 0) { //if we didn't send maxItemsToSend, try next item next time
					lastSuceededStack = lastStackLookedAt;
					lastStackLookedAt++;
				} else {
					lastSuceededStack = lastStackLookedAt - 1;
					if(lastSuceededStack < 0)
						lastSuceededStack = items.size() - 1;
				}
				return;
			}
		} else {
			
			if((!(invUtil instanceof SpecialInventoryHandler) && invUtil.getSizeInventory() == 0) || !_power.canUseEnergy(500)) {
				stalled = true;
				return;
			}
			
			if(lastSuceededStack >= invUtil.getSizeInventory())
				lastSuceededStack = 0;
			
			//incremented at the end of the previous loop.
			if (lastStackLookedAt >= invUtil.getSizeInventory())
				lastStackLookedAt = 0;
			
			ItemStack slot = invUtil.getStackInSlot(lastStackLookedAt);
	
			while(slot==null) {
				lastStackLookedAt++;
				if (lastStackLookedAt >= invUtil.getSizeInventory())
					lastStackLookedAt = 0;
				slot = invUtil.getStackInSlot(lastStackLookedAt);
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
			int sizePrev;
			slot = slot.copy();
			sizePrev = slot.stackSize;
			boolean partialSend=false;
			while(reply != null) {
				int count = slot.stackSize;
				if(reply.getValue2().maxNumberOfItems > 0) {
					count = Math.min(count, reply.getValue2().maxNumberOfItems);
				}
				ItemStack stackToSend = slot.splitStack(count);
	
				_itemSender.sendStack(stackToSend, reply, ItemSendMode.Fast);
				MainProxy.sendSpawnParticlePacket(Particles.OrangeParticle, getX(), getY(), getZ(), _world.getWorld(), 8);
	
				if(slot.stackSize == 0) break;
				if(!SimpleServiceLocator.buildCraftProxy.checkMaxItems()) break;
	
				jamList.add(reply.getValue1());
				reply = _itemSender.hasDestination(ItemIdentifier.get(slot), false, jamList);
			}
			ItemStack returned = null;
			int amountToExtract = sizePrev - slot.stackSize;
			if(slot.stackSize > 0) {
				partialSend = true;
			}
			returned = invUtil.decrStackSize(lastStackLookedAt, amountToExtract);
			if(returned.stackSize != amountToExtract) {
				throw new UnsupportedOperationException("Couldn't extract the already sended items from the inventory.");
			}
	
			lastSuceededStack=lastStackLookedAt;
			// end duplicate code
			lastStackLookedAt++;
			if(partialSend){
				if (lastStackLookedAt >= invUtil.getSizeInventory())
					lastStackLookedAt = 0;
				while(lastStackLookedAt != lastSuceededStack) {
					ItemStack tstack = invUtil.getStackInSlot(lastStackLookedAt);
					if(tstack != null && !slot.isItemEqual(tstack))
						break;
					lastStackLookedAt++;
					if (lastStackLookedAt >= invUtil.getSizeInventory())
						lastStackLookedAt = 0;
					
				}
			}
		}
	}

	@Override 
	public void registerSlot(int slot) {
	}
	
	@Override 
	public final int getX() {
		return this._power.getX();
	}
	@Override 
	public final int getY() {
		return this._power.getY();
	}
	
	@Override 
	public final int getZ() {
		return this._power.getZ();
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

	@Override
	@SideOnly(Side.CLIENT)
	public Icon getIconTexture(IconRegister register) {
		return register.registerIcon("logisticspipes:itemModule/ModuleQuickSort");
	}
}
