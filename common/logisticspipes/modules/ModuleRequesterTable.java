package logisticspipes.modules;

import com.google.common.collect.ImmutableList;

import network.rs485.logisticspipes.property.Property;

import org.jetbrains.annotations.NotNull;

import logisticspipes.utils.item.ItemIdentifierInventory;
import logisticspipes.utils.item.SimpleStackInventory;
import network.rs485.logisticspipes.property.IntegerProperty;
import network.rs485.logisticspipes.property.InventoryProperty;
import network.rs485.logisticspipes.property.SimpleInventoryProperty;

import java.util.List;

public class ModuleRequesterTable extends LogisticsModule {
	public final SimpleInventoryProperty diskInv = new SimpleInventoryProperty(new SimpleStackInventory(1, "Disk Slot", 1), "diskInv");
	public final SimpleInventoryProperty inv = new SimpleInventoryProperty(new SimpleStackInventory(27, "Crafting Resources", 64), "inv");
	public final InventoryProperty matrix = new InventoryProperty(new ItemIdentifierInventory(9, "Crafting Matrix", 1), "matrix");
	public final SimpleInventoryProperty toSortInv = new SimpleInventoryProperty(new SimpleStackInventory(1, "Sorting Slot", 64), "toSortInv");
	public final IntegerProperty rotation = new IntegerProperty(0, "blockRotation");

	@NotNull
	@Override
	public List<Property<?>> getProperties() {
		return ImmutableList.<Property<?>>builder()
			.add(diskInv)
			.add(inv)
			.add(matrix)
			.add(toSortInv)
			.add(rotation)
			.build();
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
	public boolean recievePassive() {
		return false;
	}
}
