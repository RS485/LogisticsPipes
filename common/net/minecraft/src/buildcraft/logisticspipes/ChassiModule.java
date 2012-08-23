package net.minecraft.src.buildcraft.logisticspipes;

import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.buildcraft.krapht.GuiIDs;
import net.minecraft.src.buildcraft.krapht.SimpleServiceLocator;
import net.minecraft.src.buildcraft.krapht.pipes.PipeLogisticsChassi;
import net.minecraft.src.buildcraft.logisticspipes.modules.ILogisticsModule;
import net.minecraft.src.buildcraft.logisticspipes.modules.ISendRoutedItem;
import net.minecraft.src.buildcraft.logisticspipes.modules.IWorldProvider;
import net.minecraft.src.buildcraft.logisticspipes.modules.SinkReply;
import net.minecraft.src.krapht.InventoryUtil;
import net.minecraft.src.krapht.ItemIdentifier;

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
		
		//Always deny items when we can't put the item anywhere
		IInventory inv = _parentPipe.getInventory();
		if (inv == null) return null;
		InventoryUtil invUtil = SimpleServiceLocator.inventoryUtilFactory.getInventoryUtil(inv);
		int roomForItem = invUtil.roomForItem(ItemIdentifier.get(item)); 
		
		if (roomForItem < 1) return null;
		
		for (ILogisticsModule module : _modules){
			if (module != null){
				SinkReply result = module.sinksItem(item);
				if (result != null){
					result.maxNumberOfItems = roomForItem;
					return result;
				}
			}
		}
		return null;
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
	public void registerHandler(IInventoryProvider invProvider, ISendRoutedItem itemSender, IWorldProvider world) {
		//Not used in Chassie Module
	}

}
