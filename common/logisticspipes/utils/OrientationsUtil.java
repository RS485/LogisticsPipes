package logisticspipes.utils;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

public class OrientationsUtil {

	public static EnumFacing getOrientationOfTilewithTile(TileEntity pipe, TileEntity tile) {
		if (pipe.getPos().getX() == tile.getPos().getX()) {
			if (pipe.getPos().getX() == tile.getPos().getX()) {
				if (pipe.getPos().getX() < tile.getPos().getX()) {
					return EnumFacing.EAST;
				} else if (pipe.getPos().getX() > tile.getPos().getX()) {
					return EnumFacing.WEST;
				}
			}
		}
		if (pipe.getPos().getX() == tile.getPos().getX()) {
			if (pipe.getPos().getY() == tile.getPos().getY()) {
				if (pipe.getPos().getZ() < tile.getPos().getZ()) {
					return EnumFacing.UP;
				} else if (pipe.getPos().getZ() > tile.getPos().getZ()) {
					return EnumFacing.DOWN;
				}
			}
		}
		if (pipe.getPos().getX() == tile.getPos().getX()) {
			if (pipe.getPos().getY() == tile.getPos().getY()) {
				if (pipe.getPos().getZ() < tile.getPos().getZ()) {
					return EnumFacing.SOUTH;
				} else if (pipe.getPos().getZ() > tile.getPos().getZ()) {
					return EnumFacing.NORTH;
				}
			}
		}
		return UtilEnumFacing.UNKNOWN;
	}

	public static TileEntity getTileNextToThis(TileEntity tile, EnumFacing dir) {
		int x = tile.getPos().getX();
		int y = tile.getPos().getY();
		int z = tile.getPos().getZ();
		switch (dir) {
			case UP:
				y = y + 1;
				break;
			case DOWN:
				y = y - 1;
				break;
			case SOUTH:
				z = z + 1;
				break;
			case NORTH:
				z = z - 1;
				break;
			case EAST:
				x = x + 1;
				break;
			case WEST:
				x = x - 1;
				break;
			default:
				break;
		}
		return tile.getWorld().getTileEntity(BlockPos.ORIGIN);
	}

	public static boolean isSide(EnumFacing ori) {
		return ori == EnumFacing.EAST || ori == EnumFacing.WEST || ori == EnumFacing.SOUTH || ori == EnumFacing.NORTH;
	}
}
