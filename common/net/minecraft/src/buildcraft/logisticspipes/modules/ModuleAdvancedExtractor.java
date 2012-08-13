package net.minecraft.src.buildcraft.logisticspipes.modules;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.src.IInventory;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.buildcraft.krapht.GuiIDs;
import net.minecraft.src.buildcraft.krapht.SimpleServiceLocator;
import net.minecraft.src.buildcraft.logisticspipes.IInventoryProvider;
import net.minecraft.src.buildcraft.logisticspipes.SidedInventoryAdapter;
import net.minecraft.src.krapht.SimpleInventory;
import net.minecraftforge.common.ISidedInventory;
import buildcraft.api.core.Orientations;
import buildcraft.core.Utils;

public class ModuleAdvancedExtractor implements ILogisticsModule, ISneakyOrientationreceiver, IClientInformationProvider {

	protected int currentTick = 0;

	private final SimpleInventory _filterInventory = new SimpleInventory(9, "Item list", 1);
	private boolean _itemsIncluded = true;
	protected IInventoryProvider _invProvider;
	protected ISendRoutedItem _itemSender;
	protected SneakyOrientation _sneakyOrientation = SneakyOrientation.Default;

	public ModuleAdvancedExtractor() {
		
	}

	@Override
	public void registerHandler(IInventoryProvider invProvider, ISendRoutedItem itemSender) {
		_invProvider = invProvider;
		_itemSender = itemSender;
	}
	
	public SimpleInventory getFilterInventory() {
		return _filterInventory;
	}
	
	public SneakyOrientation getSneakyOrientation(){
		return _sneakyOrientation;
	}
	
	public void setSneakyOrientation(SneakyOrientation sneakyOrientation){
		_sneakyOrientation = sneakyOrientation;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound, String prefix) {
		_filterInventory.readFromNBT(nbttagcompound, prefix);
		setItemsIncluded(nbttagcompound.getBoolean("itemsIncluded"));
		_sneakyOrientation = SneakyOrientation.values()[nbttagcompound.getInteger("sneakyorientation")];
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound, String prefix) {
		_filterInventory.writeToNBT(nbttagcompound, prefix);
		nbttagcompound.setBoolean("itemsIncluded", areItemsIncluded());
		nbttagcompound.setInteger("sneakyorientation", _sneakyOrientation.ordinal());
	}

	@Override
	public int getGuiHandlerID() {
		return GuiIDs.GUI_Module_Advanced_Extractor_ID;
	}

	@Override
	public SinkReply sinksItem(ItemStack item) {
		return null;
	}

	@Override
	public ILogisticsModule getSubModule(int slot) {
		return null;
	}

	protected int ticksToAction() {
		return 100;
	}

	protected int itemsToExtract() {
		return 1;
	}
	
	public boolean connectedToSidedInventory() {
		return _invProvider.getRawInventory() instanceof ISidedInventory;
	}
	
	@Override
	public void tick() {
		if (++currentTick < ticksToAction())
			return;
		currentTick = 0;

		IInventory inventory = _invProvider.getRawInventory();
		if (inventory == null) return;
		if (inventory instanceof ISidedInventory) {
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
			inventory = new SidedInventoryAdapter((ISidedInventory) inventory, extractOrientation);
		}
		
		ItemStack stack = checkExtract(inventory, true, _invProvider.inventoryOrientation().reverse());
		if (stack == null) return;
		_itemSender.sendStack(stack);
	}

	public ItemStack checkExtract(IInventory inventory, boolean doRemove, Orientations from) {
		IInventory inv = Utils.getInventory(inventory);
		ItemStack result = checkExtractGeneric(inv, doRemove, from);
		return result;
	}

	public ItemStack checkExtractGeneric(IInventory inventory, boolean doRemove, Orientations from) {
		for (int k = 0; k < inventory.getSizeInventory(); k++) {
			if ((inventory.getStackInSlot(k) == null) || (inventory.getStackInSlot(k).stackSize <= 0)) {
				continue;
			}
			
			ItemStack slot = inventory.getStackInSlot(k);
			if ((slot != null) && (slot.stackSize > 0) && (CanExtract(slot))) {
				if (doRemove) {
					return inventory.decrStackSize(k, itemsToExtract());
				}
				return slot;
			}
		}
		return null;
	}

	public boolean CanExtract(ItemStack item) {
		if(!shouldSend(item)) {
			return false;
		}
		
		for (int i = 0; i < this._filterInventory.getSizeInventory(); i++) {
			
			ItemStack stack = this._filterInventory.getStackInSlot(i);
			if ((stack != null) && (stack.itemID == item.itemID)) {
				if (Item.itemsList[item.itemID].isDamageable()) {
					return areItemsIncluded();
				}
				if (stack.getItemDamage() == item.getItemDamage()) {
					return areItemsIncluded();
				}
			}
		}
		return !areItemsIncluded();
	}

	protected boolean shouldSend(ItemStack stack) {
		return SimpleServiceLocator.logisticsManager.hasDestination(stack, true, _itemSender.getSourceUUID(), true);
	}

	public boolean areItemsIncluded() {
		return _itemsIncluded;
	}

	public void setItemsIncluded(boolean flag) {
		_itemsIncluded = flag;
	}
	
	@Override
	public List<String> getClientInformation() {
		List<String> list = new ArrayList<String>();
		list.add(areItemsIncluded() ? "Included" : "Excluded");
		list.add("Extraction: " + _sneakyOrientation.name());
		list.add("Filter: ");
		list.add("<inventory>");
		list.add("<that>");
		return list;
	}
}
