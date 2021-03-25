package logisticspipes.interfaces;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import logisticspipes.api.IRoutedPowerProvider;
import logisticspipes.logisticspipes.IRoutedItem;
import logisticspipes.modules.LogisticsModule;
import logisticspipes.pipes.basic.debug.DebugLogController;
import logisticspipes.routing.order.LogisticsItemOrderManager;
import logisticspipes.utils.CacheHolder;
import logisticspipes.utils.item.ItemIdentifier;
import network.rs485.logisticspipes.connection.Adjacent;
import network.rs485.logisticspipes.connection.NeighborTileEntity;

//methods needed by modules that any CRP can offer
public interface IPipeServiceProvider extends IRoutedPowerProvider, ISpawnParticles, ISendRoutedItem {

	boolean isNthTick(int n);

	DebugLogController getDebug();

	CacheHolder getCacheHolder();

	@Nonnull
	BlockPos getPos();

	void markTileDirty();

	/**
	 * @return the available adjacent cache.
	 */
	@Nonnull
	Adjacent getAvailableAdjacent();

	/**
	 * Only makes sense to use this on pipes that use pointed orientation, like the chassis pipe.
	 */
	@Nullable
	EnumFacing getPointedOrientation();

	/**
	 * to interact and send items you need to know about orders, upgrades, and have the ability to send
	 */
	LogisticsItemOrderManager getItemOrderManager();

	void queueRoutedItem(IRoutedItem routedItem, EnumFacing from);

	@Nonnull
	ISlotUpgradeManager getUpgradeManager(LogisticsModule.ModulePositionType slot, int positionInt);

	int countOnRoute(ItemIdentifier item);
}
