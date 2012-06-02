package net.minecraft.src.buildcraft.logisticspipes.modules;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.IInventory;
import net.minecraft.src.ModLoader;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.buildcraft.krapht.SimpleServiceLocator;
import net.minecraft.src.buildcraft.logisticspipes.IInventoryProvider;
import net.minecraft.src.buildcraft.logisticspipes.modules.SinkReply.FixedPriority;
import net.minecraft.src.krapht.InventoryUtil;
import net.minecraft.src.krapht.ItemIdentifier;
import net.minecraft.src.krapht.SimpleInventory;

public class ModulePassiveSupplier implements ILogisticsModule {

	private final SimpleInventory _filterInventory = new SimpleInventory(9, "Requested items", 64);
	private final IInventoryProvider _invProvider;
	
	public ModulePassiveSupplier(IInventoryProvider invProvider) {
		_invProvider = invProvider;
	}
	
	public IInventory getFilterInventory(){
		return _filterInventory;
	}
	
	@Override
	public SinkReply sinksItem(ItemIdentifier item) {
		IInventory targetInventory = _invProvider.getInventory();
		if (targetInventory == null) return null;
		
		InventoryUtil filterUtil = SimpleServiceLocator.inventoryUtilFactory.getInventoryUtil(_filterInventory);
		if (!filterUtil.containsItem(item)) return null;
		
		int targetCount = filterUtil.getItemCount(item);
		InventoryUtil targetUtil = SimpleServiceLocator.inventoryUtilFactory.getInventoryUtil(targetInventory);
		if (targetCount <= targetUtil.getItemCount(item)) return null;
		
		SinkReply reply = new SinkReply();
		reply.fixedPriority = FixedPriority.PassiveSupplier;
		reply.isPassive = true;
		return reply;
	}

	@Override
	public ModuleGuiHandler getGuiHandler() {
		return ModuleGuiHandler.GuiPassiveSupplier;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound, String prefix) {
		_filterInventory.readFromNBT(nbttagcompound, "");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound, String prefix) {
		_filterInventory.writeToNBT(nbttagcompound, "");
	}

	@Override
	public ILogisticsModule getSubModule(int slot) {return null;}

	@Override
	public void tick() {}

}
