package net.minecraft.src.buildcraft.logisticspipes.modules;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.World;
import net.minecraft.src.buildcraft.krapht.GuiIDs;
import net.minecraft.src.buildcraft.krapht.SimpleServiceLocator;
import net.minecraft.src.buildcraft.logisticspipes.IInventoryProvider;
import net.minecraft.src.buildcraft.logisticspipes.modules.SinkReply.FixedPriority;
import net.minecraft.src.krapht.InventoryUtil;
import net.minecraft.src.krapht.ItemIdentifier;
import net.minecraft.src.krapht.SimpleInventory;

public class ModulePassiveSupplier implements ILogisticsModule, IClientInformationProvider {

	private final SimpleInventory _filterInventory = new SimpleInventory(9, "Requested items", 64);
	private IInventoryProvider _invProvider;
	
	public ModulePassiveSupplier() {}

	@Override
	public void registerHandler(IInventoryProvider invProvider, ISendRoutedItem itemSender, IWorldProvider world) {
		_invProvider = invProvider;
	}

	public IInventory getFilterInventory(){
		return _filterInventory;
	}
	
	@Override
	public SinkReply sinksItem(ItemStack item) {
		IInventory targetInventory = _invProvider.getInventory();
		if (targetInventory == null) return null;
		
		InventoryUtil filterUtil = SimpleServiceLocator.inventoryUtilFactory.getInventoryUtil(_filterInventory);
		if (!filterUtil.containsItem(ItemIdentifier.get(item))) return null;
		
		int targetCount = filterUtil.getItemCount(ItemIdentifier.get(item));
		InventoryUtil targetUtil = SimpleServiceLocator.inventoryUtilFactory.getInventoryUtil(targetInventory);
		if (targetCount <= targetUtil.getItemCount(ItemIdentifier.get(item))) return null;
		
		SinkReply reply = new SinkReply();
		reply.fixedPriority = FixedPriority.PassiveSupplier;
		reply.isPassive = true;
		return reply;
	}

	@Override
	public int getGuiHandlerID() {
		return GuiIDs.GUI_Module_PassiveSupplier_ID;
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

	@Override
	public List<String> getClientInformation() {
		List<String> list = new ArrayList<String>();
		list.add("Supplied: ");
		list.add("<inventory>");
		list.add("<that>");
		return list;
	}
}
