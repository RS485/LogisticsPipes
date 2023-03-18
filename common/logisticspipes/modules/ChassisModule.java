package logisticspipes.modules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import logisticspipes.logisticspipes.ItemModuleInformationManager;
import logisticspipes.LPItems;
import logisticspipes.items.ItemModule;
import logisticspipes.interfaces.IInventoryUtil;
import logisticspipes.interfaces.ISlotUpgradeManager;
import logisticspipes.network.NewGuiHandler;
import logisticspipes.network.abstractguis.ModuleCoordinatesGuiProvider;
import logisticspipes.network.abstractguis.ModuleInHandGuiProvider;
import logisticspipes.network.guis.pipe.ChassisGuiProvider;
import logisticspipes.pipes.PipeLogisticsChassis;
import logisticspipes.pipes.PipeLogisticsChassis.ChassiTargetInformation;
import logisticspipes.proxy.computers.objects.CCSinkResponder;
import logisticspipes.pipes.upgrades.ModuleUpgradeManager;
import logisticspipes.utils.SinkReply;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.item.ItemIdentifierInventory;
import logisticspipes.utils.item.ItemIdentifierStack;
import network.rs485.logisticspipes.connection.SingleAdjacent;
import network.rs485.logisticspipes.module.Gui;
import network.rs485.logisticspipes.module.PipeServiceProviderUtilKt;
import network.rs485.logisticspipes.property.*;

public class ChassisModule extends LogisticsModule implements Gui {

	private final PipeLogisticsChassis parentChassis;
	private final SlottedModuleListProperty modules;
	public final InventoryProperty _moduleInventory;
	public final ModuleUMListProperty slotUpgradeManagers;
	public final AdjacentProperty<SingleAdjacent> pointedAdjacent;

	public ChassisModule(int moduleCount, PipeLogisticsChassis parentChassis) {
		modules = new SlottedModuleListProperty(moduleCount, "modules");
		_moduleInventory = new InventoryProperty(
			new ItemIdentifierInventory(moduleCount, "Chassi pipe", 1), "chassi");
		slotUpgradeManagers = new ModuleUMListProperty("");
		pointedAdjacent = new AdjacentProperty<>(null, parentChassis, "Orientation");
		this.parentChassis = parentChassis;
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

	public SingleAdjacent getAdjacent() {
		return (SingleAdjacent) this.pointedAdjacent.getValue();
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
		for (int i = 0; i < parentChassis.getChassisSize(); i++) {
			// TODO: remove after 1.12.2 update, backwards compatibility
			final ItemIdentifierStack idStack = this._moduleInventory.getIDStackInSlot(i);
			if (idStack != null && !this.hasModule(i)) {
				final Item stackItem = idStack.getItem().item;
				if (stackItem instanceof ItemModule) {
					final ItemModule moduleItem = (ItemModule) stackItem;
					LogisticsModule module = moduleItem.getModule(null, parentChassis, parentChassis);
					if (module != null) {
						this.installModule(i, module);
					}
				}
			}
			// remove end

			if (i >= this.slotUpgradeManagers.size()) {
				this.addModuleUpgradeManager();
			}
			this.slotUpgradeManagers.get(i).readFromNBT(tag, Integer.toString(i));
		}
		modules.stream()
			.filter(slottedModule -> !slottedModule.isEmpty())
			.forEach(slottedModule -> {
				LogisticsModule logisticsModule = Objects.requireNonNull(slottedModule.getModule());
				// FIXME: rely on getModuleForItem instead
				logisticsModule.registerHandler(parentChassis, parentChassis);
				slottedModule.registerPosition();
				if (tag.hasKey("slot" + slottedModule.getSlot()))
					logisticsModule.readFromNBT(tag.getCompoundTag("slot" + slottedModule.getSlot()));
			});
	}

	@Override
	public void writeToNBT(@Nonnull NBTTagCompound tag) {
		super.writeToNBT(tag);
		updateModuleInventory();
	}

	public void addModuleUpgradeManager() {
		this.slotUpgradeManagers.add(new ModuleUpgradeManager(parentChassis, parentChassis.getOriginalUpgradeManager()));
	}

	// FIXME: remove after 1.12
	public void updateModuleInventory() {
		modules.forEach(slottedModule -> {
			if (slottedModule.isEmpty()) {
				this._moduleInventory.clearInventorySlotContents(slottedModule.getSlot());
				return;
			}
			final LogisticsModule module = Objects.requireNonNull(slottedModule.getModule());
			final ItemIdentifierStack idStack = this._moduleInventory.getIDStackInSlot(slottedModule.getSlot());
			ItemStack moduleStack;
			if (idStack != null) {
				moduleStack = idStack.getItem().makeNormalStack(1);
			} else {
				ResourceLocation resourceLocation = LPItems.modules.get(module.getLPName());
				Item item = Item.REGISTRY.getObject(resourceLocation);
				if (item == null) return;
				moduleStack = new ItemStack(item);
			}
			ItemModuleInformationManager.saveInformation(moduleStack, module);
			this._moduleInventory.setInventorySlotContents(slottedModule.getSlot(), moduleStack);
		});
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
