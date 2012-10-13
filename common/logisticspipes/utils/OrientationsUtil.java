package logisticspipes.utils;

import net.minecraft.src.TileEntity;
import buildcraft.api.core.Orientations;
import buildcraft.transport.PipeTransportItems;

public class OrientationsUtil {
	public static Orientations getOrientationOfTilewithPipe(PipeTransportItems pipe,TileEntity tile) {
		if(pipe.zCoord == tile.zCoord) {
			if(pipe.yCoord == tile.yCoord) {
				if(pipe.xCoord < tile.xCoord) {
					return Orientations.XPos;
				} else if(pipe.xCoord > tile.xCoord) {
					return Orientations.XNeg;
				}
			}
		} else if(pipe.xCoord == tile.xCoord) {
			if(pipe.zCoord == tile.zCoord) {
				if(pipe.yCoord < tile.yCoord) {
					return Orientations.YPos;
				} else if(pipe.yCoord > tile.yCoord) {
					return Orientations.YNeg;
				}
			}
		} else if(pipe.xCoord == tile.xCoord) {
			if(pipe.yCoord == tile.yCoord) {
				if(pipe.zCoord < tile.zCoord) {
					return Orientations.ZPos;
				} else if(pipe.zCoord > tile.zCoord) {
					return Orientations.ZNeg;
				}
			}
		}
		return Orientations.Unknown;
	}
}
