package logisticspipes.utils;

import net.minecraft.util.EnumFacing;

public class UtilEnumFacing {
    public static final EnumFacing[] VALID_DIRECTIONS;
    public static final EnumFacing UNKNOWN = null;

    public static EnumFacing getOrientation(int id) {
        return id >= 0 && id < VALID_DIRECTIONS.length?VALID_DIRECTIONS[id]:UNKNOWN;
    }

    public int getIdfromEnum(EnumFacing facing){
        if(facing == EnumFacing.DOWN )return 0;
        else if(facing == EnumFacing.UP )return 1;
        else if(facing == EnumFacing.NORTH )return 2;
        else if(facing == EnumFacing.SOUTH )return 3;
        else if(facing == EnumFacing.WEST )return 4;
        else if(facing == EnumFacing.EAST )return 5;
        else return -1;
    }
    public EnumFacing FromString(String stringfacing){
        if (stringfacing.equals("up")) {
            return EnumFacing.UP;}
        else if (stringfacing.equals("down")){
            return EnumFacing.DOWN;}
        else if (stringfacing.equals("north")){
            return EnumFacing.NORTH;}
        else if (stringfacing.equals("east")){
            return EnumFacing.EAST;}
        else if (stringfacing.equals("south")){
            return EnumFacing.SOUTH;}
        else if (stringfacing.equals("west")){
            return EnumFacing.WEST;}
        else return null;
    }
    static{
        VALID_DIRECTIONS = new EnumFacing[]{EnumFacing.DOWN, EnumFacing.UP, EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.WEST, EnumFacing.EAST};
    }
}

