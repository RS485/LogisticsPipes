package logisticspipes.modules;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.nbt.NBTTagCompound;

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

public class ChassiModule extends LogisticsGuiModule {

	private final LogisticsModule[] modules;
	private final PipeLogisticsChassi parentChassis;

	public ChassiModule(int moduleCount, PipeLogisticsChassi parentChassis) {
		modules = new LogisticsModule[moduleCount];
		this.parentChassis = parentChassis;
		_service = parentChassis;
		registerPosition(ModulePositionType.IN_PIPE, 0);
	}

	public void installModule(int slot, LogisticsModule module) {
		modules[slot] = module;
	}

	public void removeModule(int slot) {
		modules[slot] = null;
	}

	public LogisticsModule getModule(int slot) {
		return modules[slot];
	}

	public boolean hasModule(int slot) {
		return (modules[slot] != null);
	}

	public LogisticsModule[] getModules() {
		return modules;
	}

	@Override
	public SinkReply sinksItem(ItemIdentifier item, int bestPriority, int bestCustomPriority, boolean allowDefault, boolean includeInTransit,
			boolean forcePassive) {
		SinkReply bestresult = null;
		for (LogisticsModule module : modules) {
			if (module != null) {
				if (!forcePassive || module.recievePassive()) {
					SinkReply result = module.sinksItem(item, bestPriority, bestCustomPriority, allowDefault, includeInTransit, forcePassive);
					if (result != null && result.maxNumberOfItems >= 0) {
						bestresult = result;
						bestPriority = result.fixedPriority.ordinal();
						bestCustomPriority = result.customPriority;
					}
				}
			}
		}

		if (bestresult == null) {
			return null;
		}
		//Always deny items when we can't put the item anywhere
		IInventoryUtil invUtil = parentChassis.getSneakyInventory(ModulePositionType.SLOT, ((ChassiTargetInformation) bestresult.addInfo).getModuleSlot());
		if (invUtil == null) {
			return null;
		}
		int roomForItem = invUtil.roomForItem(item);
		if (roomForItem < 1) {
			return null;
		}
		if (includeInTransit) {
			int onRoute = parentChassis.countOnRoute(item);
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
		if (slot < 0 || slot >= modules.length) {
			return null;
		}
		return modules[slot];
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		for (int i = 0; i < modules.length; i++) {
			if (modules[i] != null) {
				NBTTagCompound slot = nbttagcompound.getCompoundTag("slot" + i);
				if (slot != null) {
					modules[i].readFromNBT(slot);
				}
			}
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		for (int i = 0; i < modules.length; i++) {
			if (modules[i] != null) {
				NBTTagCompound slot = new NBTTagCompound();
				modules[i].writeToNBT(slot);
				nbttagcompound.setTag("slot" + i, slot);
			}
		}
	}

	@Override
	public void tick() {
		for (LogisticsModule module : modules) {
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
	public boolean interestedInAttachedInventory() {
		return false;
	}

	@Override
	public boolean interestedInUndamagedID() {
		return false;
	}

	@Override
	public boolean recievePassive() {
		for (LogisticsModule module : modules) {
			if (module != null && module.recievePassive()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public List<CCSinkResponder> queueCCSinkEvent(ItemIdentifierStack item) {
		List<CCSinkResponder> list = new ArrayList<>();
		for (LogisticsModule module : modules) {
			if (module != null) {
				list.addAll(module.queueCCSinkEvent(item));
			}
		}
		return list;
	}

	@Override
	public ModuleCoordinatesGuiProvider getPipeGuiProvider() {
		return NewGuiHandler.getGui(ChassiGuiProvider.class).setFlag(parentChassis.getUpgradeManager().hasUpgradeModuleUpgrade());
	}

	@Override
	public ModuleInHandGuiProvider getInHandGuiProvider() {
		return null;
	}
}
