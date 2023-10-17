package logisticspipes.modules;

import net.minecraft.item.ItemStack;

import network.rs485.logisticspipes.property.ItemStackProperty;

import network.rs485.logisticspipes.property.Property;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class ModuleItemsRequestLogisticsMk2 extends LogisticsModule{
	public final ItemStackProperty disk = new ItemStackProperty(ItemStack.EMPTY, "Disk");

	@NotNull
	@Override
	public List<Property<?>> getProperties() {
		return Collections.singletonList(disk);
	}

	@NotNull
	@Override
	public String getLPName() {
		return "";
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
}
