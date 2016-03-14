package logisticspipes.asm.enderio;

import com.enderio.core.common.util.BlockCoord;
import com.enderio.core.common.util.InventoryWrapper;
import crazypants.enderio.conduit.item.ItemConduit;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.pipes.basic.fluid.FluidRoutedPipe;
import logisticspipes.proxy.enderio.LogisticsInsertionFluidHandler;
import logisticspipes.proxy.enderio.LogisticsInsertionInventory;
import logisticspipes.utils.OrientationsUtil;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.IFluidHandler;

public class EnderIOHooks {

	private static boolean isEnabled = true;

	public static void disableHooks() {
		isEnabled = false;
	}

	public static IInventory handleGetExternalInventory(ItemConduit itemConduit, TileEntity tileEntity) {
		if(isEnabled && tileEntity instanceof LogisticsTileGenericPipe && ((LogisticsTileGenericPipe) tileEntity).isRoutingPipe() && ((LogisticsTileGenericPipe) tileEntity).isRouterInitialized()) {
			try {
				TileEntity conduitTile = itemConduit.getLocation().getTileEntity(tileEntity.getWorld());
				return new LogisticsInsertionInventory((LogisticsTileGenericPipe) tileEntity, OrientationsUtil
						.getOrientationOfTilewithTile(tileEntity, conduitTile));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public static ISidedInventory handleUpdateInventoryNetworkedInventory(ISidedInventory original, TileEntity tileEntity, EnumFacing dir) {
		if(isEnabled && original == null && tileEntity instanceof LogisticsTileGenericPipe && ((LogisticsTileGenericPipe) tileEntity).isRoutingPipe() && ((LogisticsTileGenericPipe) tileEntity).isRouterInitialized()) {
			try {
				return new InventoryWrapper(new LogisticsInsertionInventory((LogisticsTileGenericPipe) tileEntity, dir.getOpposite()));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return original;
	}

	public static boolean doesHandleGetExternalFluidHandler(IBlockAccess world, BlockCoord bc) {
		return handleGetExternalFluidHandler(world, bc) != null;
	}

	public static IFluidHandler handleGetExternalFluidHandler(IBlockAccess world, BlockCoord bc) {
		TileEntity tile = bc.getTileEntity(world);
		if(isEnabled && tile instanceof LogisticsTileGenericPipe && ((LogisticsTileGenericPipe) tile).isFluidPipe() && ((FluidRoutedPipe)((LogisticsTileGenericPipe) tile).pipe).canReceiveFluid()) {
			return new LogisticsInsertionFluidHandler((LogisticsTileGenericPipe)tile);
		}
		return null;
	}

	public static boolean doesHandleGetTankContainer(World world, BlockCoord bc) {
		return handleGetTankContainer(world, bc) != null;
	}

	public static IFluidHandler handleGetTankContainer(World world, BlockCoord bc) {
		TileEntity tile = bc.getTileEntity(world);
		if(isEnabled && tile instanceof LogisticsTileGenericPipe && ((LogisticsTileGenericPipe) tile).isFluidPipe() && ((FluidRoutedPipe)((LogisticsTileGenericPipe) tile).pipe).canReceiveFluid()) {
			return new LogisticsInsertionFluidHandler((LogisticsTileGenericPipe)tile);
		}
		return null;
	}
}
