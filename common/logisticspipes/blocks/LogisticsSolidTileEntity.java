package logisticspipes.blocks;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import logisticspipes.interfaces.IRotationProvider;
import network.rs485.logisticspipes.block.LogisticsSolidBlockWithEntity;

public class LogisticsSolidTileEntity extends BlockEntity implements IRotationProvider {

	public LogisticsSolidTileEntity(BlockEntityType<? extends LogisticsSolidTileEntity> type) {
		super(type);
	}

	public void onBlockBreak() {}

	public boolean isActive() {
		return false;
	}

	@Override
	public Direction getFacing() {
		return getCachedState().get(LogisticsSolidBlockWithEntity.FACING);
	}

	@Override
	public void setFacing(Direction facing) {
		World world = getWorld();
		if (world == null) return;

		world.setBlockState(getPos(), getCachedState().with(LogisticsSolidBlockWithEntity.FACING, facing));
	}

	public void notifyOfBlockChange() {}

	public World getWorldForHUD() {
		return getWorld();
	}

}
