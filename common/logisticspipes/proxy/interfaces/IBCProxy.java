package logisticspipes.proxy.interfaces;

import logisticspipes.pipes.basic.CoreUnroutedPipe;
import logisticspipes.pipes.basic.LogisticsBlockGenericPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.buildcraft.pipeparts.IBCPipePart;
import logisticspipes.transport.LPTravelingItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public interface IBCProxy {
	void resetItemRotation();
	boolean insertIntoBuildcraftPipe(TileEntity tile, LPTravelingItem item);
	boolean isIPipeTile(TileEntity tile);
	void registerPipeInformationProvider();
	void initProxy();
	boolean checkForPipeConnection(TileEntity with, ForgeDirection side, LogisticsTileGenericPipe pipe);
	boolean checkConnectionOverride(TileEntity with, ForgeDirection side, LogisticsTileGenericPipe pipe);
	boolean isMachineManagingSolids(TileEntity tile);
	boolean isMachineManagingFluids(TileEntity tile);
	IBCPipePart getBCPipePart(CoreUnroutedPipe coreUnroutedPipe);
	boolean handleBCClickOnPipe(ItemStack currentItem, CoreUnroutedPipe pipe, World world, int x, int y, int z, EntityPlayer player, int side, LogisticsBlockGenericPipe logisticsBlockGenericPipe);
	ItemStack getPipePlugItemStack();
	ItemStack getRobotTrationItemStack();
	boolean stripEquipment(World world, int x, int y, int z, EntityPlayer player, CoreUnroutedPipe pipe, LogisticsBlockGenericPipe block);
}
