package logisticspipes.modules;

import java.util.Collection;
import java.util.Objects;

import logisticspipes.modules.abstractmodules.LogisticsModule;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.routing.pathfinder.IPipeInformationProvider.ConnectionPipeType;
import logisticspipes.utils.SinkReply;
import logisticspipes.utils.SinkReply.FixedPriority;
import logisticspipes.utils.item.ItemIdentifier;
import network.rs485.logisticspipes.connection.NeighborBlockEntity;
import network.rs485.logisticspipes.init.ModuleTypes;
import network.rs485.logisticspipes.util.ItemVariant;
import network.rs485.logisticspipes.world.WorldCoordinatesWrapper;

// IHUDModuleHandler,
public class ModuleSatellite extends LogisticsModule {

	private final CoreRoutedPipe pipe;

	public ModuleSatellite(CoreRoutedPipe pipeItemsSatelliteLogistics) {
		super(ModuleTypes.INSTANCE.getSatellite());
		pipe = pipeItemsSatelliteLogistics;
	}

	private SinkReply _sinkReply = new SinkReply(FixedPriority.ItemSink, 0, true, false, 1, 0, null);

	@Override
	public SinkReply sinksItem(ItemIdentifier item, int bestPriority, int bestCustomPriority, boolean allowDefault, boolean includeInTransit,
			boolean forcePassive) {
		if (bestPriority > _sinkReply.fixedPriority.ordinal() || (bestPriority == _sinkReply.fixedPriority.ordinal() && bestCustomPriority >= _sinkReply.customPriority)) {
			return null;
		}
		return new SinkReply(_sinkReply, spaceFor(item, includeInTransit));
	}

	private int spaceFor(ItemVariant item, boolean includeInTransit) {
		WorldCoordinatesWrapper worldCoordinates = new WorldCoordinatesWrapper(pipe.container);

		int count = worldCoordinates.connectedTileEntities(ConnectionPipeType.ITEM)
				.map(adjacent -> adjacent.sneakyInsertion().from(getUpgradeManager()))
				.map(NeighborBlockEntity::getInventoryUtil)
				.filter(Objects::nonNull)
				.map(util -> util.roomForItem(item, 9999))
				.reduce(Integer::sum).orElse(0);

		if (includeInTransit) {
			count -= pipe.countOnRoute(item);
		}
		return count;
	}


	@Override
	public boolean hasGenericInterests() {
		return false;
	}

	@Override
	public Collection<ItemIdentifier> getSpecificInterests() {
		return pipe.getSpecificInterests();
	}

	@Override
	public boolean interestedInAttachedInventory() {
		return false;
		// when we are default we are interested in everything anyway, otherwise we're only interested in our filter.
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
