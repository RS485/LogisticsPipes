package logisticspipes.modules;

import com.google.common.collect.ImmutableList;

import logisticspipes.utils.item.ItemIdentifierInventory;

import net.minecraft.nbt.NBTTagCompound;

import network.rs485.logisticspipes.property.BooleanProperty;
import network.rs485.logisticspipes.property.InventoryProperty;

import network.rs485.logisticspipes.property.Property;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ModuleFirewall extends LogisticsModule {
	public final InventoryProperty inv = new InventoryProperty(new ItemIdentifierInventory(6 * 6, "Filter Inv", 1), "");
	public final BooleanProperty blockProvider = new BooleanProperty(false, "blockProvider");
	public final BooleanProperty blockCrafter = new BooleanProperty(false, "blockCrafter");
	public final BooleanProperty blockSorting = new BooleanProperty(false, "blockSorting");
	public final BooleanProperty blockPower = new BooleanProperty(true, "blockPower");
	public final BooleanProperty isBlocking = new BooleanProperty(true, "isBlocking");

	@NotNull
	@Override
	public List<Property<?>> getProperties() {
		return ImmutableList.<Property<?>>builder()
			.add(inv)
			.add(blockProvider)
			.add(blockCrafter)
			.add(blockSorting)
			.add(blockPower)
			.add(isBlocking)
			.build();
	}

	@NotNull
	@Override
	public String getLPName() {
		throw new RuntimeException("Cannot get LP name for " + this);
	}

	@Override
	public void tick() {}

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

	@Override
	public void readFromNBT(@NotNull NBTTagCompound tag) {
		super.readFromNBT(tag);

		// FIXME: remove after 1.12
		if (tag.hasKey("blockCrafer"))
			blockCrafter.setValue(tag.getBoolean("blockCrafer"));
	}
}
