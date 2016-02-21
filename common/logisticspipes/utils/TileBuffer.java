package logisticspipes.utils;

import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;


public final class TileBuffer {

	private Block block = null;
	private TileEntity tile;
	public IBlockState iBlockState;
	public Chunk chunk;
	public UtilBlockPos ur;
	public BlockPos blockpos;


	private final SafeTimeTracker tracker = new SafeTimeTracker(20, 5);
	private final World world;
	private int x = blockpos.getX();
	private int y = blockpos.getY();
	private int z = blockpos.getZ();


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
			if (!UtilWorld.blockExists(blockpos, world)) {
				return;
			}
		}
		tile = null;
		block = null;

		if (!loadUnloaded && !UtilWorld.blockExists(blockpos, world)) {
			return;
		}

		block = chunk.getBlock(blockpos.getX(),blockpos.getY(),blockpos.getZ());

		if (block != null && block.hasTileEntity(iBlockState)) {
			tile = world.getTileEntity(blockpos);
		}
	}

	public void set(Block block, TileEntity tile) {
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

	public TileEntity getTile() {
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

		return UtilWorld.blockExists(blockpos,world);
	}

	public static TileBuffer[] makeBuffer(World world, BlockPos blockpos, boolean loadUnloaded) {
		TileBuffer[] buffer = new TileBuffer[6];

		for (int i = 0; i < 6; i++) {
			EnumFacing d = UtilEnumFacing.getOrientation(i);

			buffer[i] = new TileBuffer(world, blockpos.getX() + d.getFrontOffsetX(), blockpos.getY() + d.getFrontOffsetY(), blockpos.getZ() + d.getFrontOffsetZ(), loadUnloaded);
		}

		return buffer;
	}
}
