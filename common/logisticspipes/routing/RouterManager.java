/**
 * Copyright (c) Krapht, 2011
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.routing;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.dimension.Dimension;

@ParametersAreNonnullByDefault
public interface RouterManager {

	Router getOrCreateRouter(UUID routerUuid, World world, BlockPos pos, boolean forceCreateDuplicateAtCoordinate);

	Router getRouter(UUID id);

	boolean isRouter(UUID id);

	void removeRouter(UUID id);

	@Nonnull
	List<Router> getRouters();

	void serverStopClean();

	void dimensionUnloaded(int dim);

	void printAllRouters();
}
