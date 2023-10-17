package logisticspipes.modules;

import java.util.*;
import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableList;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import logisticspipes.interfaces.IClientInformationProvider;
import logisticspipes.interfaces.IPipeServiceProvider;
import logisticspipes.network.NewGuiHandler;
import logisticspipes.network.abstractguis.ModuleCoordinatesGuiProvider;
import logisticspipes.network.abstractguis.ModuleInHandGuiProvider;
import logisticspipes.network.guis.module.inpipe.FluidSupplierSlot;
import logisticspipes.pipefxhandlers.Particles;
import logisticspipes.pipes.PipeLogisticsChassis.ChassiTargetInformation;
import logisticspipes.pipes.basic.fluid.FluidRoutedPipe;
import logisticspipes.utils.item.ItemIdentifierStack;
import logisticspipes.utils.SinkReply;
import logisticspipes.utils.SinkReply.FixedPriority;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.item.ItemIdentifierInventory;

import network.rs485.logisticspipes.module.Gui;
import network.rs485.logisticspipes.property.BooleanProperty;
import network.rs485.logisticspipes.property.InventoryProperty;
import network.rs485.logisticspipes.property.Property;

public class ModuleFluidSupplier extends LogisticsModule implements IClientInformationProvider, Gui {

	protected final FluidRoutedPipe fluidPipe = (FluidRoutedPipe) _service;

	public final HashMap<ItemIdentifier, Integer> _requestedItems = new HashMap<>();

	public final InventoryProperty filterInventory = new InventoryProperty(
			new ItemIdentifierInventory(9, "Requested liquids", 1), "");
	public final BooleanProperty _requestPartials = new BooleanProperty(false, "requestpartials");

	private SinkReply _sinkReply;

	@Nonnull
	@Override
	public String getLPName() {
		throw new RuntimeException("Cannot get LP name for " + this);
	}

	@Nonnull
	@Override
	public List<Property<?>> getProperties() {
		return ImmutableList.<Property<?>>builder()
			.add(filterInventory)
			.add(_requestPartials)
			.build();
	}

	@Nonnull
	public IInventory getFilterInventory() {
		return filterInventory;
	}

	@Override
	public void registerPosition(@Nonnull ModulePositionType slot, int positionInt) {
		super.registerPosition(slot, positionInt);
		_sinkReply = new SinkReply(FixedPriority.ItemSink,
				0,
				true,
				false,
				0,
				0,
				new ChassiTargetInformation(getPositionInt()));
	}

	@Override
	public SinkReply sinksItem(@Nonnull ItemStack stack, ItemIdentifier item, int bestPriority, int bestCustomPriority,
			boolean allowDefault, boolean includeInTransit, boolean forcePassive) {
		if (bestPriority > _sinkReply.fixedPriority.ordinal()
				|| (bestPriority == _sinkReply.fixedPriority.ordinal()
				&& bestCustomPriority >= _sinkReply.customPriority)) {
			return null;
		}
		final IPipeServiceProvider service = _service;
		if (service == null) return null;
		if (filterInventory.containsItem(item)) {
			service.spawnParticle(Particles.VioletParticle, 2);
			return _sinkReply;
		}
		return null;
	}

	@Override
	public void tick() {

	}

	public void decreaseRequested(ItemIdentifierStack item) {
		int remaining = item.getStackSize();
		//see if we can get an exact match
		Integer count = _requestedItems.get(item.getItem());
		if (count != null) {
			_requestedItems.put(item.getItem(), Math.max(0, count - remaining));
			remaining -= count;
		}
		if (remaining <= 0) {
			return;
		}
		//still remaining... was from fuzzyMatch on a crafter
		for (Map.Entry<ItemIdentifier, Integer> e : _requestedItems.entrySet()) {
			if (e.getKey().item == item.getItem().item && e.getKey().itemDamage == item.getItem().itemDamage) {
				int expected = e.getValue();
				e.setValue(Math.max(0, expected - remaining));
				remaining -= expected;
			}
			if (remaining <= 0) {
				return;
			}
		}
		//we have no idea what this is, log it.
		fluidPipe.debug.log("liquid supplier got unexpected item " + item);
	}

	@Override
	public @Nonnull
	List<String> getClientInformation() {
		List<String> list = new ArrayList<>();
		list.add("Supplied: ");
		list.add("<inventory>");
		list.add("<that>");
		return list;
	}

	@Override
	public boolean hasGenericInterests() {
		return true;
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
	public boolean receivePassive() {
		return true;
	}

	@Nonnull
	@Override
	public ModuleCoordinatesGuiProvider getPipeGuiProvider() {
		return NewGuiHandler.getGui(FluidSupplierSlot.class);
	}

	@Nonnull
	@Override
	public ModuleInHandGuiProvider getInHandGuiProvider() {
		throw new UnsupportedOperationException("Fluid Supplier GUI cannot be opened in hand");
	}

}
