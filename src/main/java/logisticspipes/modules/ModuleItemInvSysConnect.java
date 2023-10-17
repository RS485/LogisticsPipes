package logisticspipes.modules;

import java.util.List;

import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;

import network.rs485.logisticspipes.property.IntegerProperty;
import network.rs485.logisticspipes.property.Property;
import network.rs485.logisticspipes.property.UUIDProperty;

public class ModuleItemInvSysConnect extends LogisticsModule {
	public final IntegerProperty resistance = new IntegerProperty(0, "resistance");
	public final UUIDProperty connectedChannel = new UUIDProperty(null, "connectedChannel");

	@NotNull
	@Override
	public List<Property<?>> getProperties() {
		return ImmutableList.<Property<?>>builder()
			.add(resistance)
			.add(connectedChannel)
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
	public boolean receivePassive() {
		return false;
	}
}
