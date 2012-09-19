package logisticspipes.modules;

import java.util.ArrayList;
import java.util.List;

import logisticspipes.interfaces.IClientInformationProvider;
import logisticspipes.interfaces.ILogisticsModule;
import logisticspipes.interfaces.ISendRoutedItem;
import logisticspipes.interfaces.ISneakyOrientationreceiver;
import logisticspipes.interfaces.IWorldProvider;
import logisticspipes.logisticspipes.IInventoryProvider;
import logisticspipes.logisticspipes.SidedInventoryAdapter;
import logisticspipes.logisticspipes.modules.SinkReply;
import logisticspipes.logisticspipes.modules.SneakyOrientation;
import logisticspipes.main.GuiIDs;
import logisticspipes.main.SimpleServiceLocator;
import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import net.minecraftforge.common.ISidedInventory;
import buildcraft.api.core.Orientations;
import buildcraft.api.inventory.ISpecialInventory;

public class ModuleExtractor implements ILogisticsModule, ISneakyOrientationreceiver, IClientInformationProvider {

	//protected final int ticksToAction = 100;
	private int currentTick = 0;
	
	private IInventoryProvider _invProvider;
	private ISendRoutedItem _itemSender;
	private SneakyOrientation _sneakyOrientation = SneakyOrientation.Default;
	
	public ModuleExtractor() {
		
	}

	@Override
	public void registerHandler(IInventoryProvider invProvider, ISendRoutedItem itemSender, IWorldProvider world) {
		_invProvider = invProvider;
		_itemSender = itemSender;
	}

	protected int ticksToAction(){
		return 100;
	}

	protected int itemsToExtract(){
		return 1;
	}
	
	public SneakyOrientation getSneakyOrientation(){
		return _sneakyOrientation;
	}
	
	public void setSneakyOrientation(SneakyOrientation sneakyOrientation){
		_sneakyOrientation = sneakyOrientation;
	}
	
	@Override
	public SinkReply sinksItem(ItemStack item) {
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
			ItemStack[] stack = ((ISpecialInventory) targetInventory).extractItem(false, extractOrientation,1);
			if (stack == null) return;
			if (stack.length < 1) return;
			if (stack[0] == null) return;
			if (!shouldSend(stack[0])) return;
			stack = ((ISpecialInventory) targetInventory).extractItem(true, extractOrientation,1);
			_itemSender.sendStack(stack[0]);
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
			
			stackToSend = targetInventory.decrStackSize(i, itemsToExtract());
			_itemSender.sendStack(stackToSend);
			break;
		}
	}
	
	protected boolean shouldSend(ItemStack stack){
		return SimpleServiceLocator.logisticsManager.hasDestination(stack, true, _itemSender.getSourceUUID(), true);
	}
	
	@Override
	public List<String> getClientInformation() {
		List<String> list = new ArrayList<String>();
		list.add("Extraction: " + _sneakyOrientation.name());
		return list;
	}

	@Override
	public void registerPosition(int xCoord, int yCoord, int zCoord, int slot) {}
}
