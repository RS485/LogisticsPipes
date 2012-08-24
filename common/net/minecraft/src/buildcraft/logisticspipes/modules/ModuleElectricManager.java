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
import net.minecraft.src.krapht.SimpleInventory;

public class ModuleElectricManager implements ILogisticsModule, IClientInformationProvider {

	private final SimpleInventory _filterInventory = new SimpleInventory(9, "Electric Items", 1);
	private boolean _dischargeMode;
	protected IInventoryProvider _invProvider;
	protected ISendRoutedItem _itemSender;
	private int ticksToAction = 100;
	private int currentTick = 0;

	public IInventory getFilterInventory(){
		return _filterInventory;
	}

	public boolean isDischargeMode(){
		return _dischargeMode;
	}
	public void setDischargeMode(boolean isDischargeMode){
		_dischargeMode = isDischargeMode;
	}

	@Override
	public void registerHandler(IInventoryProvider invProvider, ISendRoutedItem itemSender, IWorldProvider world) {
		_invProvider = invProvider;
		_itemSender = itemSender;
	}

	public static int getCharge(ItemStack item)
	{
		if (SimpleServiceLocator.electricItemProxy.isElectricItem(item) && item.hasTagCompound())
			return item.getTagCompound().getInteger("charge");
		else
			return 0;
	}

	public boolean findElectricItem(ItemStack item, boolean discharged, boolean partial)
	{
		if (!SimpleServiceLocator.electricItemProxy.isElectricItem(item)) return false;

		for (int i = 0; i < _filterInventory.getSizeInventory(); i++){
			ItemStack stack = _filterInventory.getStackInSlot(i);
			if (stack == null) continue;
			if (discharged && SimpleServiceLocator.electricItemProxy.isDischarged(item,partial,stack.getItem()))
				return true;
			if (!discharged && SimpleServiceLocator.electricItemProxy.isCharged(item,partial,stack.getItem()))
				return true;
		}
		return false;
	}

	@Override
	public SinkReply sinksItem(ItemStack item) {
		if (findElectricItem(item, !isDischargeMode(), true))
		{
			SinkReply reply = new SinkReply();
			reply.fixedPriority = FixedPriority.ItemSink;
			reply.isPassive = true;
			return reply;
		}
		return null;
	}

	@Override
	public int getGuiHandlerID() {
		return GuiIDs.GUI_Module_ElectricManager_ID;
	}

	@Override
	public ILogisticsModule getSubModule(int slot) {return null;}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound, String prefix) {
		_filterInventory.readFromNBT(nbttagcompound, "");
		setDischargeMode(nbttagcompound.getBoolean("discharge"));
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound, String prefix) {
		_filterInventory.writeToNBT(nbttagcompound, "");
		nbttagcompound.setBoolean("discharge", isDischargeMode());
	}

	@Override
	public void tick() {
		if (++currentTick  < ticksToAction) return;
		currentTick = 0;

		IInventory inv = _invProvider.getInventory();
		if(inv == null) return;
		for(int i=0; i < inv.getSizeInventory(); i++) {
			ItemStack item = inv.getStackInSlot(i);
			if (item != null && findElectricItem(item, isDischargeMode(), false)) {
				_itemSender.sendStack(inv.decrStackSize(i,1));
			}
		}
	}

	@Override
	public List<String> getClientInformation() {
		List<String> list = new ArrayList<String>();
		list.add("Discharge Mode: " + (isDischargeMode() ? "Yes" : "No"));
		list.add("Supplied: ");
		list.add("<inventory>");
		list.add("<that>");
		return list;
	}
}
