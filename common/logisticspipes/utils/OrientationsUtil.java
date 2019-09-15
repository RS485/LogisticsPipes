package logisticspipes.utils;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class OrientationsUtil {

	public static Direction getOrientationOfTileWithTile(BlockEntity pipeTile, BlockEntity tileTile) {
		BlockPos pipe = pipeTile.getPos();
		BlockPos tile = tileTile.getPos();
		if (pipe.getZ() == tile.getZ()) {
			if (pipe.getY() == tile.getY()) {
				if (pipe.getX() < tile.getX()) {
					return Direction.EAST;
				} else if (pipe.getX() > tile.getX()) {
					return Direction.WEST;
				}
			}
		}
		if (pipe.getX() == tile.getX()) {
			if (pipe.getZ() == tile.getZ()) {
				if (pipe.getY() < tile.getY()) {
					return Direction.UP;
				} else if (pipe.getY() > tile.getY()) {
					return Direction.DOWN;
				}
			}
		}
		if (pipe.getX() == tile.getX()) {
			if (pipe.getY() == tile.getY()) {
				if (pipe.getZ() < tile.getZ()) {
					return Direction.SOUTH;
				} else if (pipe.getZ() > tile.getZ()) {
					return Direction.NORTH;
				}
			}
		}
		return null;
	}

	public static BlockEntity getTileNextToThis(BlockEntity entity, Direction dir) {
		return entity.getWorld().getBlockEntity(entity.getPos().offset(dir));
	}

	public static boolean isSide(Direction ori) {
		return ori.getAxis() != Direction.Axis.Y;
	}

}
