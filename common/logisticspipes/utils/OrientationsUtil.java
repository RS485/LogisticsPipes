package logisticspipes.utils;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.transport.PipeTransport;

public class OrientationsUtil {
	public static ForgeDirection getOrientationOfTilewithPipe(PipeTransport pipe,TileEntity tile) {
		if(pipe.getZ() == tile.zCoord) {
			if(pipe.getY() == tile.yCoord) {
				if(pipe.getX() < tile.xCoord) {
					return ForgeDirection.EAST;
				} else if(pipe.getX() > tile.xCoord) {
					return ForgeDirection.WEST;
				}
			}
		}
		if(pipe.getX() == tile.xCoord) {
			if(pipe.getZ() == tile.zCoord) {
				if(pipe.getY() < tile.yCoord) {
					return ForgeDirection.UP;
				} else if(pipe.getY() > tile.yCoord) {
					return ForgeDirection.DOWN;
				}
			}
		}
		if(pipe.getX() == tile.xCoord) {
			if(pipe.getY() == tile.yCoord) {
				if(pipe.getZ() < tile.zCoord) {
					return ForgeDirection.SOUTH;
				} else if(pipe.getZ() > tile.zCoord) {
					return ForgeDirection.NORTH;
				}
			}
		}
		return ForgeDirection.UNKNOWN;
	}
	
	public static ForgeDirection getOrientationOfTilewithTile(TileEntity pipe,TileEntity tile) {
		if(pipe.getZ() == tile.zCoord) {
			if(pipe.getY() == tile.yCoord) {
				if(pipe.getX() < tile.xCoord) {
					return ForgeDirection.EAST;
				} else if(pipe.getX() > tile.xCoord) {
					return ForgeDirection.WEST;
				}
			}
		}
		if(pipe.getX() == tile.xCoord) {
			if(pipe.getZ() == tile.zCoord) {
				if(pipe.getY() < tile.yCoord) {
					return ForgeDirection.UP;
				} else if(pipe.getY() > tile.yCoord) {
					return ForgeDirection.DOWN;
				}
			}
		}
		if(pipe.getX() == tile.xCoord) {
			if(pipe.getY() == tile.yCoord) {
				if(pipe.getZ() < tile.zCoord) {
					return ForgeDirection.SOUTH;
				} else if(pipe.getZ() > tile.zCoord) {
					return ForgeDirection.NORTH;
				}
			}
		}
		return ForgeDirection.UNKNOWN;
	}
}
