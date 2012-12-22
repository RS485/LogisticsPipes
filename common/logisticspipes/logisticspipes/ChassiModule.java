package logisticspipes.logisticspipes;

import logisticspipes.interfaces.IChassiePowerProvider;
import logisticspipes.interfaces.ILogisticsModule;
import logisticspipes.interfaces.ISendRoutedItem;
import logisticspipes.interfaces.IWorldProvider;
import logisticspipes.network.GuiIDs;
import logisticspipes.pipes.PipeLogisticsChassi;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.utils.InventoryUtil;
import logisticspipes.utils.ItemIdentifier;
import logisticspipes.utils.SinkReply;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class ChassiModule implements ILogisticsModule{
	
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
	public SinkReply sinksItem(ItemStack item) {
		SinkReply result = null;
		for (ILogisticsModule module : _modules){
			if (module != null){
				result = module.sinksItem(item);
				if (result != null){
					break;
				}
			}
		}

		if (result == null) return null;
		//Always deny items when we can't put the item anywhere
		IInventory inv = _parentPipe.getInventory();
		if (inv == null) return null;
		InventoryUtil invUtil = SimpleServiceLocator.inventoryUtilFactory.getInventoryUtil(inv);
		int roomForItem = invUtil.roomForItem(ItemIdentifier.get(item)); 
		
		if (roomForItem < 1) return null;

		result.maxNumberOfItems = roomForItem;
				
		return result;
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
	public void readFromNBT(NBTTagCompound nbttagcompound, String prefix) {
		for (int i = 0; i < _modules.length; i++){
			if (_modules[i] != null){
				NBTTagCompound slot = nbttagcompound.getCompoundTag("slot" + i);
				if (slot != null){
					_modules[i].readFromNBT(slot, "");
				}
			}
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound, String prefix) {
		for (int i = 0; i < _modules.length; i++){
			if (_modules[i] != null){
				NBTTagCompound slot = new NBTTagCompound();
				_modules[i].writeToNBT(slot, "");
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
	public void registerHandler(IInventoryProvider invProvider, ISendRoutedItem itemSender, IWorldProvider world, IChassiePowerProvider powerprovider) {
		//Not used in Chassie Module
	}

	@Override
	public void registerPosition(int xCoord, int yCoord, int zCoord, int slot) {
		//Not used in Chassie Module
	}
}
