package logisticspipes.buildcraft.logisticspipes.modules;

import java.util.ArrayList;
import java.util.List;

import logisticspipes.buildcraft.krapht.GuiIDs;
import logisticspipes.buildcraft.krapht.SimpleServiceLocator;
import logisticspipes.buildcraft.logisticspipes.IInventoryProvider;
import logisticspipes.buildcraft.logisticspipes.modules.SinkReply.FixedPriority;
import logisticspipes.krapht.InventoryUtil;
import logisticspipes.krapht.ItemIdentifier;
import logisticspipes.krapht.SimpleInventory;


import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.World;

public class ModuleTerminus implements ILogisticsModule, IClientInformationProvider {

	private final SimpleInventory _filterInventory = new SimpleInventory(9, "Terminated items", 1);

	public IInventory getFilterInventory(){
		return _filterInventory;
	}
	
	public ModuleTerminus() {}
	
	@Override
	public void registerHandler(IInventoryProvider invProvider, ISendRoutedItem itemSender, IWorldProvider world) {}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound, String prefix) {
		_filterInventory.readFromNBT(nbttagcompound, "");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound, String prefix) {
    	_filterInventory.writeToNBT(nbttagcompound, "");
    }

	@Override
	public int getGuiHandlerID() {
		return GuiIDs.GUI_Module_Terminus_ID;
	}
	
	@Override
	public SinkReply sinksItem(ItemStack item) {
		InventoryUtil invUtil = SimpleServiceLocator.inventoryUtilFactory.getInventoryUtil(_filterInventory);
		if (invUtil.containsItem(ItemIdentifier.get(item))){
			SinkReply reply = new SinkReply();
			reply.fixedPriority = FixedPriority.Terminus;
			reply.isPassive = true;
			return reply;
		}

		return null;
	}

	@Override
	public ILogisticsModule getSubModule(int slot) {return null;}

	@Override
	public void tick() {}

	@Override
	public List<String> getClientInformation() {
		List<String> list = new ArrayList<String>();
		list.add("Terminated: ");
		list.add("<inventory>");
		list.add("<that>");
		return list;
	}
}
