package logisticspipes.utils;

import logisticspipes.pipes.basic.LogisticsTileGenericPipe;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import net.minecraftforge.common.util.ForgeDirection;

public final class TileBuffer {

	private Block block = null;
	private TileEntity tile;

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
			if (world.getBlock(x, y, z) == null) {
				return;
			}
		}
		tile = null;
		block = null;

		if (!loadUnloaded && !world.blockExists(x, y, z)) {
			return;
		}

		block = world.getBlock(x, y, z);

		if (block != null && block.hasTileEntity(world.getBlockMetadata(x, y, z))) {
			tile = world.getTileEntity(x, y, z);
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

		return world.blockExists(x, y, z);
	}

	public static TileBuffer[] makeBuffer(World world, int x, int y, int z, boolean loadUnloaded) {
		TileBuffer[] buffer = new TileBuffer[6];

		for (int i = 0; i < 6; i++) {
			ForgeDirection d = ForgeDirection.getOrientation(i);
			buffer[i] = new TileBuffer(world, x + d.offsetX, y + d.offsetY, z + d.offsetZ, loadUnloaded);
		}

		return buffer;
	}
}
