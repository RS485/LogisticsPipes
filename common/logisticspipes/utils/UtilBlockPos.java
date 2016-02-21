package logisticspipes.utils;

import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

/**
 * Created by stefan on 19-2-2016.
 */
public class UtilBlockPos {
    private BlockPos pos;


    public BlockPos getblockposoffset(EnumFacing side){
        if (side == EnumFacing.UP)
            return pos.up();
        else if (side == EnumFacing.DOWN)
            return pos.down();
        else if (side == EnumFacing.WEST)
            return pos.west();
        else if (side == EnumFacing.EAST)
            return pos.east();
        else if (side == EnumFacing.NORTH)
            return pos.north();
        else if (side == EnumFacing.SOUTH)
            return pos.south();
        else return null;
    }
    public BlockPos getBlockposfromXYZ(int x,int y,int z){
       return new BlockPos(x,y,z);

    }
    }
