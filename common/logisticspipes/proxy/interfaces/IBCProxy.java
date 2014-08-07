package logisticspipes.proxy.interfaces;

import logisticspipes.pipes.basic.CoreUnroutedPipe;
import logisticspipes.pipes.basic.LogisticsBlockGenericPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.buildcraft.pipeparts.IBCPipePart;
import logisticspipes.proxy.buildcraft.pipeparts.IBCTilePart;
import logisticspipes.renderer.state.PipeRenderState;
import logisticspipes.transport.LPTravelingItem;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
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
	IBCPipePart getBCPipePart(LogisticsTileGenericPipe tile);
	boolean handleBCClickOnPipe(ItemStack currentItem, CoreUnroutedPipe pipe, World world, int x, int y, int z, EntityPlayer player, int side, LogisticsBlockGenericPipe logisticsBlockGenericPipe);
	ItemStack getPipePlugItemStack();
	ItemStack getRobotStationItemStack();
	boolean stripEquipment(World world, int x, int y, int z, EntityPlayer player, CoreUnroutedPipe pipe, LogisticsBlockGenericPipe block);
	IBCTilePart getBCTilePart(LogisticsTileGenericPipe tile);
	void notifyOfChange(LogisticsTileGenericPipe pipe, TileEntity tile, ForgeDirection o);
	void renderGatesWires(LogisticsTileGenericPipe pipe, double x, double y, double z);
	void pipeFacadeRenderer(RenderBlocks renderblocks, LogisticsBlockGenericPipe block, PipeRenderState state, int x, int y, int z);
	void pipePlugRenderer(RenderBlocks renderblocks, Block block, PipeRenderState state, int x, int y, int z);
	ItemStack getDropFacade(CoreUnroutedPipe pipe, ForgeDirection dir);
	/** Only used by the BC proxy internaly */
	boolean canPipeConnect(TileEntity pipe, TileEntity tile, ForgeDirection direction);
	void pipeRobotStationRenderer(RenderBlocks renderblocks, LogisticsBlockGenericPipe block, PipeRenderState state, int x, int y, int z);
}
