package logisticspipes.asm.mcmp;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class MCMPHooks {

	public interface IMCMPGetTEHook {

		TileEntity apply(IBlockAccess world, BlockPos pos);
	}

	public static IMCMPGetTEHook mcmpGetTEHook = IBlockAccess::getTileEntity;

	public static TileEntity getTileEntityForBlockClass(IBlockAccess world, BlockPos pos) {
		return mcmpGetTEHook.apply(world, pos);
	}
}
