package logisticspipes.modules;

import java.util.List;

import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;

import logisticspipes.pipes.PipeFluidSupplierMk2;
import logisticspipes.utils.item.ItemIdentifierInventory;
import network.rs485.logisticspipes.property.*;

public class ModuleFluidSupplierMK2 extends ModuleFluidSupplier {

	public final InventoryProperty dummyInventory = new InventoryProperty(
		new ItemIdentifierInventory(1, "Fluid to keep stocked", 127, true), "");
	public final IntegerProperty amount = new IntegerProperty(0, "amount");
	public final EnumProperty<PipeFluidSupplierMk2.MinMode> _bucketMinimum = new EnumProperty<>(PipeFluidSupplierMk2.MinMode.ONEBUCKET,
		"_bucketMinimum", PipeFluidSupplierMk2.MinMode.values());

	@NotNull
	@Override
	public List<Property<?>> getProperties() {
		return ImmutableList.<Property<?>>builder()
			.add(dummyInventory)
			.add(amount)
			.add(_requestPartials)
			.add(_bucketMinimum)
			.build();
	}
}
