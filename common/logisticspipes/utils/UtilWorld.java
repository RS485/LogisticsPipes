package logisticspipes.utils;

import net.minecraft.block.Block;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ReportedException;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

/**
 * Created by stefan on 19-2-2016.
 */
public class UtilWorld {

    public static boolean chunkLoaded(World world, BlockPos pos){
        return world.getChunkProvider().chunkExists(pos.getX() >> 4, pos.getZ() >> 4) && world.getChunkFromBlockCoords(pos).isLoaded();
    }


    public static boolean blockExists(BlockPos pos, World world)
    {
        return (pos.getY() >= 0 && pos.getY() < 256) && world.getChunkProvider().chunkExists(pos.getX() >> 4, pos.getZ() >> 4);
    }
    public Block getBlock(BlockPos pos , World world){
        if (pos.getX() >= -30000000 && pos.getZ() >= -30000000 && pos.getX() < 30000000 && pos.getZ() < 30000000 && pos.getY() >= 0 && pos.getY() < 256)
        {
            Chunk chunk = null;

            try
            {
                chunk = world.getChunkFromChunkCoords(pos.getX() >> 4, pos.getZ() >> 4);
                return chunk.getBlock(pos.getX() & 15, pos.getY(), pos.getZ() & 15);
            }
            catch (Throwable throwable)
            {
                CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Exception getting block type in world");
                CrashReportCategory crashreportcategory = crashreport.makeCategory("Requested block coordinates");
                crashreportcategory.addCrashSection("Found chunk", Boolean.valueOf(chunk == null));
                crashreportcategory.addCrashSection("Location", CrashReportCategory.getCoordinateInfo(pos.getX(), pos.getY(), pos.getZ()));
                throw new ReportedException(crashreport);
            }
        }
        else
        {
            return Blocks.air;
        }
    }
}
