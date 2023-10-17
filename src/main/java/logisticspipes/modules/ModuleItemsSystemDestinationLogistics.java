package logisticspipes.modules;

import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import logisticspipes.utils.item.ItemIdentifierInventory;
import network.rs485.logisticspipes.property.InventoryProperty;
import network.rs485.logisticspipes.property.Property;

public class ModuleItemsSystemDestinationLogistics extends LogisticsModule {
	public final InventoryProperty inv = new InventoryProperty(new ItemIdentifierInventory(1, "Freq Slot", 1), "");

	@NotNull
	@Override
	public List<Property<?>> getProperties() {
		return Collections.singletonList(inv);
	}

	@NotNull
	@Override
	public String getLPName() {
		throw new RuntimeException("Cannot get LP name for " + this);
	}

	@Override
	public void tick() {

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
	public boolean receivePassive() {
		return false;
	}
}
