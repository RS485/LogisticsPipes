package logisticspipes.logisticspipes;

import java.util.List;

import logisticspipes.api.IRoutedPowerProvider;
import logisticspipes.interfaces.IInventoryUtil;
import logisticspipes.interfaces.ISendRoutedItem;
import logisticspipes.interfaces.IWorldProvider;
import logisticspipes.modules.LogisticsGuiModule;
import logisticspipes.modules.LogisticsModule;
import logisticspipes.network.GuiIDs;
import logisticspipes.pipes.PipeLogisticsChassi;
import logisticspipes.utils.ItemIdentifier;
import logisticspipes.utils.SinkReply;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Icon;

public class ChassiModule extends LogisticsGuiModule{
	
	private final LogisticsModule[] _modules;
	private final PipeLogisticsChassi _parentPipe;
	
	public ChassiModule(int moduleCount,PipeLogisticsChassi parentPipe){
		_modules = new LogisticsModule[moduleCount];
		_parentPipe = parentPipe;
	}
	
	public void installModule(int slot, LogisticsModule module){
		_modules[slot] = module;
	}
	
	public void removeModule(int slot){
		_modules[slot] = null;
	}
	
	public LogisticsModule getModule(int slot){
		return _modules[slot];
	}
	
	public boolean hasModule(int slot){
		return (_modules[slot] != null);
	}
	
	@Override
	public SinkReply sinksItem(ItemIdentifier item, int bestPriority, int bestCustomPriority, boolean allowDefault, boolean includeInTransit) {
		SinkReply bestresult = null;
		for (LogisticsModule module : _modules){
			if (module != null){
				SinkReply result = module.sinksItem(item, bestPriority, bestCustomPriority, allowDefault, includeInTransit);
				if (result != null) {
					bestresult = result;
					bestPriority = result.fixedPriority.ordinal();
					bestCustomPriority = result.customPriority;
				}
			}
		}

		if (bestresult == null) return null;
		//Always deny items when we can't put the item anywhere
		IInventoryUtil invUtil = _parentPipe.getSneakyInventory(false);
		if (invUtil == null) return null;
		int roomForItem = invUtil.roomForItem(item); 
		if (roomForItem < 1) return null;
		if(includeInTransit) {
			roomForItem-=_parentPipe.countOnRoute(item);
			if (roomForItem < 1) return null;
		}

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
	public LogisticsModule getSubModule(int slot) {
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
		for (LogisticsModule module : _modules){
			if (module == null) continue;
			module.tick();
		}
	}

	@Override
	public void registerHandler(IInventoryProvider invProvider, ISendRoutedItem itemSender, IWorldProvider world, IRoutedPowerProvider powerprovider) {
		//Not used in Chassie Module
	}


	@Override 
	public void registerSlot(int slot) {
	}
	
	@Override 
	public final int getX() {
		return this._parentPipe.getX();
	}
	@Override 
	public final int getY() {
		return this._parentPipe.getX();
	}
	
	@Override 
	public final int getZ() {
		return this._parentPipe.getX();
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

	@Override
	public boolean recievePassive() {
		for (LogisticsModule module : _modules){
			if(module != null && module.recievePassive())
				return true;
		}
		return false;
	}

	@Override
	public Icon getIconTexture(IconRegister register) {
		//Not Needed
		return null;
	}
}
