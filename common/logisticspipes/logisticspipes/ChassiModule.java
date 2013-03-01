package logisticspipes.logisticspipes;

import java.util.List;

import logisticspipes.api.IRoutedPowerProvider;
import logisticspipes.interfaces.IInventoryUtil;
import logisticspipes.interfaces.ILogisticsGuiModule;
import logisticspipes.interfaces.ILogisticsModule;
import logisticspipes.interfaces.ISendRoutedItem;
import logisticspipes.interfaces.IWorldProvider;
import logisticspipes.network.GuiIDs;
import logisticspipes.pipes.PipeLogisticsChassi;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.utils.ItemIdentifier;
import logisticspipes.utils.SinkReply;
import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.NBTTagCompound;

public class ChassiModule implements ILogisticsGuiModule{
	
	private final ILogisticsModule[] _modules;
	private final PipeLogisticsChassi _parentPipe;
	
	public ChassiModule(int moduleCount,PipeLogisticsChassi parentPipe){
		_modules = new ILogisticsModule[moduleCount];
		_parentPipe = parentPipe;
	}
	
	public void installModule(int slot, ILogisticsModule module){
		_modules[slot] = module;
	}
	
	public void removeModule(int slot){
		_modules[slot] = null;
	}
	
	public ILogisticsModule getModule(int slot){
		return _modules[slot];
	}
	
	public boolean hasModule(int slot){
		return (_modules[slot] != null);
	}
	
	@Override
	public SinkReply sinksItem(ItemIdentifier item, int bestPriority, int bestCustomPriority) {
		SinkReply bestresult = null;
		for (ILogisticsModule module : _modules){
			if (module != null){
				SinkReply result = module.sinksItem(item, bestPriority, bestCustomPriority);
				if (result != null) {
					bestresult = result;
					bestPriority = result.fixedPriority.ordinal();
					bestCustomPriority = result.customPriority;
				}
			}
		}

		if (bestresult == null) return null;
		//Always deny items when we can't put the item anywhere
		IInventory inv = _parentPipe.getSneakyInventory();
		if (inv == null) return null;
		IInventoryUtil invUtil = SimpleServiceLocator.inventoryUtilFactory.getInventoryUtil(inv);
		int roomForItem = invUtil.roomForItem(item); 
		
		if (roomForItem < 1) return null;

		if(bestresult.maxNumberOfItems == 0) {
			return new SinkReply(bestresult, roomForItem);
		}
		return new SinkReply(bestresult, Math.min(bestresult.maxNumberOfItems, roomForItem));
	}


	@Override
	public int getGuiHandlerID() {
		return GuiIDs.GUI_ChassiModule_ID;
	}
	
	@Override
	public ILogisticsModule getSubModule(int slot) {
		if (slot < 0 || slot >= _modules.length) return null;
		return _modules[slot];
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		for (int i = 0; i < _modules.length; i++){
			if (_modules[i] != null){
				NBTTagCompound slot = nbttagcompound.getCompoundTag("slot" + i);
				if (slot != null){
					_modules[i].readFromNBT(slot);
				}
			}
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		for (int i = 0; i < _modules.length; i++){
			if (_modules[i] != null){
				NBTTagCompound slot = new NBTTagCompound();
				_modules[i].writeToNBT(slot);
				nbttagcompound.setTag("slot"+i, slot);
			}
		}
	}

	@Override
	public void tick() {
		for (ILogisticsModule module : _modules){
			if (module == null) continue;
			module.tick();
		}
	}

	@Override
	public void registerHandler(IInventoryProvider invProvider, ISendRoutedItem itemSender, IWorldProvider world, IRoutedPowerProvider powerprovider) {
		//Not used in Chassie Module
	}

	@Override
	public void registerPosition(int xCoord, int yCoord, int zCoord, int slot) {
		//Not used in Chassie Module
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
}
