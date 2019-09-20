package logisticspipes.proxy.specialinventoryhandler;

import javax.annotation.Nullable;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public interface SpecialWrappedInventoryFactory<T extends SpecialInventoryHandler> {

	@Nullable
	T getUtil(World world, BlockPos pos, Direction dir, boolean hideOnePerStack, boolean hideOne, int cropStart, int cropEnd);

}
