package logisticspipes.utils;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.transport.PipeTransport;

public class OrientationsUtil {
	public static ForgeDirection getOrientationOfTilewithPipe(PipeTransport pipe,TileEntity tile) {
		if(pipe.container.zCoord == tile.zCoord) {
			if(pipe.container.yCoord == tile.yCoord) {
				if(pipe.container.xCoord < tile.xCoord) {
					return ForgeDirection.EAST;
				} else if(pipe.container.xCoord > tile.xCoord) {
					return ForgeDirection.WEST;
				}
			}
		}
		if(pipe.container.xCoord == tile.xCoord) {
			if(pipe.container.zCoord == tile.zCoord) {
				if(pipe.container.yCoord < tile.yCoord) {
					return ForgeDirection.UP;
				} else if(pipe.container.yCoord > tile.yCoord) {
					return ForgeDirection.DOWN;
				}
			}
		}
		if(pipe.container.xCoord == tile.xCoord) {
			if(pipe.container.yCoord == tile.yCoord) {
				if(pipe.container.zCoord < tile.zCoord) {
					return ForgeDirection.SOUTH;
				} else if(pipe.container.zCoord > tile.zCoord) {
					return ForgeDirection.NORTH;
				}
			}
		}
		return ForgeDirection.UNKNOWN;
	}
	
	public static ForgeDirection getOrientationOfTilewithTile(TileEntity pipe,TileEntity tile) {
		if(pipe.zCoord == tile.zCoord) {
			if(pipe.yCoord == tile.yCoord) {
				if(pipe.xCoord < tile.xCoord) {
					return ForgeDirection.EAST;
				} else if(pipe.xCoord > tile.xCoord) {
					return ForgeDirection.WEST;
				}
			}
		}
		if(pipe.xCoord == tile.xCoord) {
			if(pipe.zCoord == tile.zCoord) {
				if(pipe.yCoord < tile.yCoord) {
					return ForgeDirection.UP;
				} else if(pipe.yCoord > tile.yCoord) {
					return ForgeDirection.DOWN;
				}
			}
		}
		if(pipe.xCoord == tile.xCoord) {
			if(pipe.yCoord == tile.yCoord) {
				if(pipe.zCoord < tile.zCoord) {
					return ForgeDirection.SOUTH;
				} else if(pipe.zCoord > tile.zCoord) {
					return ForgeDirection.NORTH;
				}
			}
		}
		return ForgeDirection.UNKNOWN;
	}
}
