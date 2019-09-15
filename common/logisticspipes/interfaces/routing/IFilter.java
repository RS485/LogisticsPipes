package logisticspipes.interfaces.routing;

import net.minecraft.util.math.BlockPos;

import network.rs485.logisticspipes.routing.request.Resource;
import network.rs485.logisticspipes.util.ItemVariant;

public interface IFilter {

	boolean isBlocked();

	boolean isFilteredItem(ItemVariant item);

	boolean isFilteredItem(Resource resultItem);

	boolean blockProvider();

	boolean blockCrafting();

	boolean blockRouting();

	boolean blockPower();

	BlockPos getPos();

}
