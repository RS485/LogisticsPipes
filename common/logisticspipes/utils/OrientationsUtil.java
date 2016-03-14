package logisticspipes.utils;

import net.minecraft.tileentity.TileEntity;

import net.minecraft.util.EnumFacing;

public class OrientationsUtil {

	public static EnumFacing getOrientationOfTilewithTile(TileEntity pipe, TileEntity tile) {
		if (pipe.zCoord == tile.zCoord) {
			if (pipe.yCoord == tile.yCoord) {
				if (pipe.xCoord < tile.xCoord) {
					return EnumFacing.EAST;
				} else if (pipe.xCoord > tile.xCoord) {
					return EnumFacing.WEST;
				}
			}
		}
		if (pipe.xCoord == tile.xCoord) {
			if (pipe.zCoord == tile.zCoord) {
				if (pipe.yCoord < tile.yCoord) {
					return EnumFacing.UP;
				} else if (pipe.yCoord > tile.yCoord) {
					return EnumFacing.DOWN;
				}
			}
		}
		if (pipe.xCoord == tile.xCoord) {
			if (pipe.yCoord == tile.yCoord) {
				if (pipe.zCoord < tile.zCoord) {
					return EnumFacing.SOUTH;
				} else if (pipe.zCoord > tile.zCoord) {
					return EnumFacing.NORTH;
				}
			}
		}
		return null;
	}

	public static TileEntity getTileNextToThis(TileEntity tile, EnumFacing dir) {
		int x = tile.xCoord;
		int y = tile.yCoord;
		int z = tile.zCoord;
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
		return tile.getWorld().getTileEntity(x, y, z);
	}

	public static boolean isSide(EnumFacing ori) {
		return ori == EnumFacing.EAST || ori == EnumFacing.WEST || ori == EnumFacing.SOUTH || ori == EnumFacing.NORTH;
	}
}
