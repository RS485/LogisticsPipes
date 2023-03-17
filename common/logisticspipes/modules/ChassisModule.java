package logisticspipes.modules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import logisticspipes.interfaces.IInventoryUtil;
import logisticspipes.interfaces.ISlotUpgradeManager;
import logisticspipes.network.NewGuiHandler;
import logisticspipes.network.abstractguis.ModuleCoordinatesGuiProvider;
import logisticspipes.network.abstractguis.ModuleInHandGuiProvider;
import logisticspipes.network.guis.pipe.ChassisGuiProvider;
import logisticspipes.pipes.PipeLogisticsChassis;
import logisticspipes.pipes.PipeLogisticsChassis.ChassiTargetInformation;
import logisticspipes.proxy.computers.objects.CCSinkResponder;
import logisticspipes.utils.SinkReply;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.item.ItemIdentifierStack;
import network.rs485.logisticspipes.module.Gui;
import network.rs485.logisticspipes.module.PipeServiceProviderUtilKt;
import network.rs485.logisticspipes.property.Property;
import network.rs485.logisticspipes.property.SlottedModule;
import network.rs485.logisticspipes.property.SlottedModuleListProperty;

public class ChassisModule extends LogisticsModule implements Gui {

	private final PipeLogisticsChassis parentChassis;
	private final SlottedModuleListProperty modules;

	public ChassisModule(int moduleCount, PipeLogisticsChassis parentChassis) {
		modules = new SlottedModuleListProperty(moduleCount, "modules");
		this.parentChassis = parentChassis;
		registerPosition(ModulePositionType.IN_PIPE, 0);
	}

	@Nonnull
	@Override
	public String getLPName() {
		throw new RuntimeException("Cannot get LP name for " + this);
	}

	@Nonnull
	@Override
	public List<Property<?>> getProperties() {
		return Collections.singletonList(modules);
	}

	public void installModule(int slot, LogisticsModule module) {
		modules.set(slot, module);
	}

	public void removeModule(int slot) {
		modules.clear(slot);
	}

	@Nullable
	public LogisticsModule getModule(int slot) {
		return modules.get(slot).getModule();
	}

	public boolean hasModule(int slot) {
		return !modules.get(slot).isEmpty();
	}

	public Stream<LogisticsModule> getModules() {
		return modules.stream()
				.filter(slottedModule -> !slottedModule.isEmpty())
				.map(SlottedModule::getModule);
	}

	public Stream<SlottedModule> slottedModules() {
		return modules.stream();
	}

	@Override
	public SinkReply sinksItem(@Nonnull ItemStack stack, ItemIdentifier item, int bestPriority, int bestCustomPriority,
			boolean allowDefault, boolean includeInTransit, boolean forcePassive) {
		SinkReply bestresult = null;
		for (SlottedModule slottedModule : modules) {
			final LogisticsModule module = slottedModule.getModule();
			if (module != null) {
				if (!forcePassive || module.recievePassive()) {
					SinkReply result = module
							.sinksItem(stack, item, bestPriority, bestCustomPriority, allowDefault, includeInTransit,
									forcePassive);
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
		final ISlotUpgradeManager upgradeManager = parentChassis.getUpgradeManager(ModulePositionType.SLOT,
				((ChassiTargetInformation) bestresult.addInfo).getModuleSlot());
		IInventoryUtil invUtil = PipeServiceProviderUtilKt.availableSneakyInventories(parentChassis, upgradeManager)
				.stream().findFirst().orElse(null);
		if (invUtil == null) {
			return null;
		}
		int roomForItem;
		if (includeInTransit) {
			int onRoute = parentChassis.countOnRoute(item);
			final ItemStack copy = stack.copy();
			copy.setCount(onRoute + item.getMaxStackSize());
			roomForItem = invUtil.roomForItem(copy);
			roomForItem -= onRoute;
		} else {
			roomForItem = invUtil.roomForItem(stack);
		}
		if (roomForItem < 1) {
			return null;
		}

		if (bestresult.maxNumberOfItems == 0) {
			return new SinkReply(bestresult, roomForItem);
		}
		return new SinkReply(bestresult, Math.min(bestresult.maxNumberOfItems, roomForItem));
	}

	@Override
	public void readFromNBT(@Nonnull NBTTagCompound tag) {
		super.readFromNBT(tag);
		// FIXME: remove after 1.12
		modules.stream()
				.filter(slottedModule -> !slottedModule.isEmpty() && tag.hasKey("slot" + slottedModule.getSlot()))
				.forEach(slottedModule -> Objects.requireNonNull(slottedModule.getModule())
						.readFromNBT(tag.getCompoundTag("slot" + slottedModule.getSlot())));
	}

	@Override
	public void tick() {
		for (SlottedModule slottedModule : modules) {
			final LogisticsModule module = slottedModule.getModule();
			if (module == null) {
				continue;
			}
			module.tick();
		}
	}

	@Override
	public void finishInit() {
		super.finishInit();
		getModules().forEach(LogisticsModule::finishInit);
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
		for (SlottedModule slottedModule : modules) {
			final LogisticsModule module = slottedModule.getModule();
			if (module != null && module.recievePassive()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public List<CCSinkResponder> queueCCSinkEvent(ItemIdentifierStack item) {
		List<CCSinkResponder> list = new ArrayList<>();
		for (SlottedModule slottedModule : modules) {
			final LogisticsModule module = slottedModule.getModule();
			if (module != null) {
				list.addAll(module.queueCCSinkEvent(item));
			}
		}
		return list;
	}

	@Nonnull
	@Override
	public ModuleCoordinatesGuiProvider getPipeGuiProvider() {
		return NewGuiHandler.getGui(ChassisGuiProvider.class)
				.setFlag(parentChassis.getUpgradeManager().hasUpgradeModuleUpgrade());
	}

	@Nonnull
	@Override
	public ModuleInHandGuiProvider getInHandGuiProvider() {
		throw new UnsupportedOperationException("Chassis GUI can never be opened in hand");
	}

}
