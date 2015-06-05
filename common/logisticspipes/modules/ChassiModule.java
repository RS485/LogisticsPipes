package logisticspipes.modules;

import java.util.ArrayList;
import java.util.List;

import logisticspipes.interfaces.IInventoryUtil;
import logisticspipes.interfaces.IPipeServiceProvider;
import logisticspipes.interfaces.IWorldProvider;
import logisticspipes.modules.abstractmodules.LogisticsGuiModule;
import logisticspipes.modules.abstractmodules.LogisticsModule;
import logisticspipes.network.NewGuiHandler;
import logisticspipes.network.abstractguis.ModuleCoordinatesGuiProvider;
import logisticspipes.network.abstractguis.ModuleInHandGuiProvider;
import logisticspipes.network.guis.pipe.ChassiGuiProvider;
import logisticspipes.pipes.PipeLogisticsChassi;
import logisticspipes.pipes.PipeLogisticsChassi.ChassiTargetInformation;
import logisticspipes.proxy.computers.objects.CCSinkResponder;
import logisticspipes.utils.SinkReply;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.item.ItemIdentifierStack;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;

public class ChassiModule extends LogisticsGuiModule {

	private final LogisticsModule[] _modules;
	private final PipeLogisticsChassi _parentPipe;

	public ChassiModule(int moduleCount, PipeLogisticsChassi parentPipe) {
		_modules = new LogisticsModule[moduleCount];
		_parentPipe = parentPipe;
		_service = parentPipe;
		registerPosition(ModulePositionType.IN_PIPE, 0);
	}

	public void installModule(int slot, LogisticsModule module) {
		_modules[slot] = module;
	}

	public void removeModule(int slot) {
		_modules[slot] = null;
	}

	public LogisticsModule getModule(int slot) {
		return _modules[slot];
	}

	public boolean hasModule(int slot) {
		return (_modules[slot] != null);
	}

	@Override
	public SinkReply sinksItem(ItemIdentifier item, int bestPriority, int bestCustomPriority, boolean allowDefault, boolean includeInTransit) {
		SinkReply bestresult = null;
		for (LogisticsModule module : _modules) {
			if (module != null) {
				SinkReply result = module.sinksItem(item, bestPriority, bestCustomPriority, allowDefault, includeInTransit);
				if (result != null && result.maxNumberOfItems >= 0) {
					bestresult = result;
					bestPriority = result.fixedPriority.ordinal();
					bestCustomPriority = result.customPriority;
				}
			}
		}

		if (bestresult == null) {
			return null;
		}
		//Always deny items when we can't put the item anywhere
		IInventoryUtil invUtil = _parentPipe.getSneakyInventory(false, ModulePositionType.SLOT, ((ChassiTargetInformation) bestresult.addInfo).getModuleSlot());
		if (invUtil == null) {
			return null;
		}
		int roomForItem = invUtil.roomForItem(item);
		if (roomForItem < 1) {
			return null;
		}
		if (includeInTransit) {
			int onRoute = _parentPipe.countOnRoute(item);
			roomForItem = invUtil.roomForItem(item, onRoute + item.getMaxStackSize());
			roomForItem -= onRoute;
			if (roomForItem < 1) {
				return null;
			}
		}

		if (bestresult.maxNumberOfItems == 0) {
			return new SinkReply(bestresult, roomForItem);
		}
		return new SinkReply(bestresult, Math.min(bestresult.maxNumberOfItems, roomForItem));
	}

	@Override
	public LogisticsModule getSubModule(int slot) {
		if (slot < 0 || slot >= _modules.length) {
			return null;
		}
		return _modules[slot];
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		for (int i = 0; i < _modules.length; i++) {
			if (_modules[i] != null) {
				NBTTagCompound slot = nbttagcompound.getCompoundTag("slot" + i);
				if (slot != null) {
					_modules[i].readFromNBT(slot);
				}
			}
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		for (int i = 0; i < _modules.length; i++) {
			if (_modules[i] != null) {
				NBTTagCompound slot = new NBTTagCompound();
				_modules[i].writeToNBT(slot);
				nbttagcompound.setTag("slot" + i, slot);
			}
		}
	}

	@Override
	public void tick() {
		for (LogisticsModule module : _modules) {
			if (module == null) {
				continue;
			}
			module.tick();
		}
	}

	@Override
	public void registerHandler(IWorldProvider world, IPipeServiceProvider service) {
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

	@Override
	public boolean recievePassive() {
		for (LogisticsModule module : _modules) {
			if (module != null && module.recievePassive()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public List<CCSinkResponder> queueCCSinkEvent(ItemIdentifierStack item) {
		List<CCSinkResponder> list = new ArrayList<CCSinkResponder>();
		for (LogisticsModule module : _modules) {
			if (module != null) {
				list.addAll(module.queueCCSinkEvent(item));
			}
		}
		return list;
	}

	@Override
	public IIcon getIconTexture(IIconRegister register) {
		//Not Needed
		return null;
	}

	@Override
	protected ModuleCoordinatesGuiProvider getPipeGuiProvider() {
		return NewGuiHandler.getGui(ChassiGuiProvider.class).setFlag(_parentPipe.getUpgradeManager().hasUpgradeModuleUpgrade());
	}

	@Override
	protected ModuleInHandGuiProvider getInHandGuiProvider() {
		return null;
	}
}
