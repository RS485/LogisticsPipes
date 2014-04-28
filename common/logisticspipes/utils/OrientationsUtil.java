package logisticspipes.utils;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

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
	
	public static TileEntity getTileNextToThis(TileEntity tile, ForgeDirection dir) {
		int x = tile.xCoord;
		int y = tile.yCoord;
		int z = tile.zCoord;
		switch(dir) {
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
		return tile.worldObj.getBlockTileEntity(x, y, z);
	}

	public static boolean isSide(ForgeDirection ori) {
		return ori == ForgeDirection.EAST || ori == ForgeDirection.WEST || ori == ForgeDirection.SOUTH || ori == ForgeDirection.NORTH;
	}
}
