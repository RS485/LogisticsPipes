package net.minecraft.src.buildcraft.logisticspipes.modules;

import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.World;
import net.minecraft.src.buildcraft.krapht.SimpleServiceLocator;
import net.minecraft.src.buildcraft.logisticspipes.IInventoryProvider;

public class ModuleQuickSort implements ILogisticsModule {

	private final int ticksToAction = 100;
	private int currentTick = 0;
	private boolean sent;
	private int ticksToResend = 0;
	
	private IInventoryProvider _invProvider;
	private ISendRoutedItem _itemSender;
	
	public ModuleQuickSort() {}

	@Override
	public void registerHandler(IInventoryProvider invProvider, ISendRoutedItem itemSender, IWorldProvider world) {
		_invProvider = invProvider;
		_itemSender = itemSender;
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
			_itemSender.sendStack(stackToSend);
			targetInventory.setInventorySlotContents(i, null);
			
			sent = true;
			break;
		}		
	}
	
	private boolean shouldSend(ItemStack stack){
		return SimpleServiceLocator.logisticsManager.hasDestination(stack, false, _itemSender.getSourceUUID(), true);
	}

}
