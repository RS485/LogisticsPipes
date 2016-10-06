package logisticspipes.utils;

import net.minecraft.tileentity.TileEntity;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import network.rs485.logisticspipes.world.IntegerCoordinates;

public class OrientationsUtil {

	public static EnumFacing getOrientationOfTilewithTile(TileEntity pipeTile, TileEntity tileTile) {
		IntegerCoordinates pipe = new IntegerCoordinates(pipeTile.getPos());
		IntegerCoordinates tile = new IntegerCoordinates(tileTile.getPos());
		if (pipe.getZCoord() == tile.getZCoord()) {
			if (pipe.getYCoord() == tile.getYCoord()) {
				if (pipe.getXCoord() < tile.getXCoord()) {
					return EnumFacing.EAST;
				} else if (pipe.getXCoord() > tile.getXCoord()) {
					return EnumFacing.WEST;
				}
			}
		}
		if (pipe.getXCoord() == tile.getXCoord()) {
			if (pipe.getZCoord() == tile.getZCoord()) {
				if (pipe.getYCoord() < tile.getYCoord()) {
					return EnumFacing.UP;
				} else if (pipe.getYCoord() > tile.getYCoord()) {
					return EnumFacing.DOWN;
				}
			}
		}
		if (pipe.getXCoord() == tile.getXCoord()) {
			if (pipe.getYCoord() == tile.getYCoord()) {
				if (pipe.getZCoord() < tile.getZCoord()) {
					return EnumFacing.SOUTH;
				} else if (pipe.getZCoord() > tile.getZCoord()) {
					return EnumFacing.NORTH;
				}
			}
		}
		return null;
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
		return tile.getWorld().getTileEntity(new BlockPos(x, y, z));
	}

	public static boolean isSide(EnumFacing ori) {
		return ori == EnumFacing.EAST || ori == EnumFacing.WEST || ori == EnumFacing.SOUTH || ori == EnumFacing.NORTH;
	}
}
