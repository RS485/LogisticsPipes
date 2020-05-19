package logisticspipes.utils;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public class OrientationsUtil {

	public static EnumFacing getOrientationOfTilewithTile(TileEntity pipeTile, TileEntity tileTile) {
		final BlockPos pipe = pipeTile.getPos();
		final BlockPos other = tileTile.getPos();
		if (pipe.getZ() == other.getZ()) {
			if (pipe.getY() == other.getY()) {
				if (pipe.getX() < other.getX()) {
					return EnumFacing.EAST;
				} else if (pipe.getX() > other.getX()) {
					return EnumFacing.WEST;
				}
			}
		}
		if (pipe.getX() == other.getX()) {
			if (pipe.getZ() == other.getZ()) {
				if (pipe.getY() < other.getY()) {
					return EnumFacing.UP;
				} else if (pipe.getY() > other.getY()) {
					return EnumFacing.DOWN;
				}
			}
		}
		if (pipe.getX() == other.getX()) {
			if (pipe.getY() == other.getY()) {
				if (pipe.getZ() < other.getZ()) {
					return EnumFacing.SOUTH;
				} else if (pipe.getZ() > other.getZ()) {
					return EnumFacing.NORTH;
				}
			}
		}
		return null;
	}

}
