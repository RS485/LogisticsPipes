package logisticspipes.routing.pathfinder.changedetection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldEventListener;
import net.minecraft.world.World;

import logisticspipes.ticks.LPTickHandler.LPWorldInfo;

public class LPWorldAccess implements IWorldEventListener {

	private final World world;
	private final LPWorldInfo info;

	public LPWorldAccess(World world, LPWorldInfo info) {
		this.world = world;
		this.info = info;
	}

	@Override
	public void notifyBlockUpdate(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState oldState, @Nonnull IBlockState newState, int flags) {
		TEControl.handleBlockUpdate(world, info, pos);
	}

	@Override
	public void notifyLightSet(@Nonnull BlockPos pos) {}

	@Override
	public void markBlockRangeForRenderUpdate(int x1, int y1, int z1, int x2, int y2, int z2) {}

	@Override
	public void playSoundToAllNearExcept(@Nullable EntityPlayer player, @Nonnull SoundEvent soundIn, @Nonnull SoundCategory category, double x, double y, double z, float volume,
			float pitch) {}

	@Override
	public void playRecord(@Nonnull SoundEvent soundIn, @Nonnull BlockPos pos) {}

	@Override
	public void spawnParticle(int particleID, boolean ignoreRange, double xCoord, double yCoord, double zCoord, double xSpeed, double ySpeed, double zSpeed,
			@Nonnull int... parameters) {}

	@Override
	public void spawnParticle(int id, boolean ignoreRange, boolean p_190570_3_, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed,
			@Nonnull int... parameters) {

	}

	@Override
	public void onEntityAdded(@Nonnull Entity entityIn) {}

	@Override
	public void onEntityRemoved(@Nonnull Entity entityIn) {}

	@Override
	public void broadcastSound(int soundID, @Nonnull BlockPos pos, int data) {}

	@Override
	public void playEvent(@Nullable EntityPlayer player, int type, @Nonnull BlockPos blockPosIn, int data) {}

	@Override
	public void sendBlockBreakProgress(int breakerId, @Nonnull BlockPos pos, int progress) {}
}
