package net.minecraft.src.buildcraft.logisticspipes.modules;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;
import net.minecraft.src.ModLoader;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.buildcraft.api.ISpecialInventory;
import net.minecraft.src.buildcraft.api.Orientations;
import net.minecraft.src.buildcraft.krapht.GuiIDs;
import net.minecraft.src.buildcraft.krapht.SimpleServiceLocator;
import net.minecraft.src.buildcraft.logisticspipes.GuiID;
import net.minecraft.src.buildcraft.logisticspipes.IInventoryProvider;
import net.minecraft.src.buildcraft.logisticspipes.SidedInventoryAdapter;
import net.minecraft.src.forge.ISidedInventory;
import net.minecraft.src.krapht.ItemIdentifier;

public class ModuleExtractor implements ILogisticsModule {

	//protected final int ticksToAction = 100;
	private int currentTick = 0;
	
	private final IInventoryProvider _invProvider;
	private final ISendRoutedItem _itemSender;
	private SneakyOrientation _sneakyOrientation = SneakyOrientation.Default;
	
	public ModuleExtractor(IInventoryProvider invProvider, ISendRoutedItem itemSender) {
		_invProvider = invProvider;
		_itemSender = itemSender;
	}
	
	protected int ticksToAction(){
		return 100;
	}
	
	
	public SneakyOrientation getSneakyOrientation(){
		return _sneakyOrientation;
	}
	
	public void setSneakyOrientation(SneakyOrientation sneakyOrientation){
		_sneakyOrientation = sneakyOrientation;
	}
	
	@Override
	public SinkReply sinksItem(ItemIdentifier item) {
		return null;
	}

	@Override
	public int getGuiHandlerID() {
		return GuiIDs.GUI_Module_Extractor_ID;
	}

	@Override
	public ILogisticsModule getSubModule(int slot) {return null;}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound, String prefix) {
		_sneakyOrientation = SneakyOrientation.values()[nbttagcompound.getInteger("sneakyorientation")];
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound, String prefix) {
		nbttagcompound.setInteger("sneakyorientation", _sneakyOrientation.ordinal());
	}

	@Override
	public void tick() {
		if (++currentTick < ticksToAction()) return;
		currentTick = 0;
		
		//Extract Item
		IInventory targetInventory = _invProvider.getRawInventory();
		if (targetInventory == null) return;
		Orientations extractOrientation;
		switch (_sneakyOrientation){
			case Bottom:
				extractOrientation = Orientations.YNeg;
				break;
			case Top:
				extractOrientation = Orientations.YPos;
				break;
			case Side:
				extractOrientation = Orientations.ZPos;
				break;
			default:
				extractOrientation = _invProvider.inventoryOrientation().reverse();
		}
		
		if (targetInventory instanceof ISpecialInventory){
			ItemStack stack = ((ISpecialInventory) targetInventory).extractItem(false, extractOrientation);
			if (stack == null) return;
			if (!shouldSend(stack)) return;
			stack = ((ISpecialInventory) targetInventory).extractItem(true, extractOrientation);
			_itemSender.sendStack(stack);
			return;
		}
		
		if (targetInventory instanceof ISidedInventory){
			targetInventory = new SidedInventoryAdapter((ISidedInventory) targetInventory, extractOrientation);
		}
		
		ItemStack stackToSend;
		
		for (int i = 0; i < targetInventory.getSizeInventory(); i++){
			stackToSend = targetInventory.getStackInSlot(i);
			if (stackToSend == null) continue;
			
			if (!this.shouldSend(stackToSend)) continue;
			
			stackToSend = targetInventory.decrStackSize(i, 1);
			_itemSender.sendStack(stackToSend);
			break;
		}
	}
	
	private boolean shouldSend(ItemStack stack){
		return SimpleServiceLocator.logisticsManager.hasDestination(stack, true, _itemSender.getSourceUUID(), true);
	}
}
