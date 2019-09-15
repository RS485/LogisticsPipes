package logisticspipes.utils;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import logisticspipes.pipes.basic.LogisticsTileGenericPipe;

public final class TileBuffer {

	private Block block = null;
	private BlockEntity tile;

	private final SafeTimeTracker tracker = new SafeTimeTracker(20, 5);
	private final World world;
	private final int x, y, z;
	private final boolean loadUnloaded;

	public TileBuffer(World world, int x, int y, int z, boolean loadUnloaded) {
		this.world = world;
		this.x = x;
		this.y = y;
		this.z = z;
		this.loadUnloaded = loadUnloaded;

		refresh();
	}

	public void refresh() {
		if (tile instanceof LogisticsTileGenericPipe && ((LogisticsTileGenericPipe) tile).pipe != null && ((LogisticsTileGenericPipe) tile).pipe.preventRemove()) {
			if (world.getBlockState(new BlockPos(x, y, z)) == null) {
				return;
			}
		}
		tile = null;
		block = null;

		if (!loadUnloaded) {
			return;
		}

		BlockState blockState = world.getBlockState(new BlockPos(x, y, z));
		block = blockState != null ? blockState.getBlock() : null;

		if (block != null && block.hasTileEntity(world.getBlockState(new BlockPos(x, y, z)))) {
			tile = world.getBlockEntity(new BlockPos(x, y, z));
		}
	}

	public void set(Block block, BlockEntity tile) {
		this.block = block;
		this.tile = tile;
		tracker.markTime(world);
	}

	public Block getBlock() {
		if (tile != null && !tile.isInvalid()) {
			return block;
		}

		if (tracker.markTimeIfDelay(world)) {
			refresh();

			if (tile != null && !tile.isInvalid()) {
				return block;
			}
		}

		return null;
	}

	public BlockEntity getTile() {
		if (tile != null && !tile.isInvalid()) {
			return tile;
		}

		if (tracker.markTimeIfDelay(world)) {
			refresh();

			if (tile != null && !tile.isInvalid()) {
				return tile;
			}
		}

		return null;
	}

	public boolean exists() {
		if (tile != null && !tile.isInvalid()) {
			return true;
		}
		BlockState blockState = world.getBlockState(new BlockPos(x, y, z));
		return blockState != null && blockState.getBlock() != null;
	}

	public static TileBuffer[] makeBuffer(World world, BlockPos pos, boolean loadUnloaded) {
		TileBuffer[] buffer = new TileBuffer[6];

		for (int i = 0; i < 6; i++) {
			Direction d = Direction.getFront(i);
			buffer[i] = new TileBuffer(world, pos.getX() + d.getFrontOffsetX(), pos.getY() + d.getFrontOffsetY(), pos.getZ() + d.getFrontOffsetZ(), loadUnloaded);
		}

		return buffer;
	}
}
