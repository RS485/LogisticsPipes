package logisticspipes.proxy.interfaces;

import logisticspipes.asm.IgnoreDisabledProxy;
import logisticspipes.pipes.basic.CoreUnroutedPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.buildcraft.subproxies.IBCClickResult;
import logisticspipes.proxy.buildcraft.subproxies.IBCRenderTESR;
import logisticspipes.proxy.buildcraft.subproxies.IBCTilePart;
import logisticspipes.proxy.buildcraft.subproxies.IConnectionOverrideResult;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import net.minecraft.util.EnumFacing;

public interface IBCProxy {

	void resetItemRotation();

	boolean isIPipeTile(TileEntity tile);

	void registerPipeInformationProvider();

	void initProxy();

	boolean checkForPipeConnection(TileEntity with, EnumFacing side, LogisticsTileGenericPipe pipe);

	IConnectionOverrideResult checkConnectionOverride(TileEntity with, EnumFacing side, LogisticsTileGenericPipe pipe);

	/** Only used by the BC proxy internaly */
	boolean canPipeConnect(TileEntity pipe, TileEntity tile, EnumFacing direction);

	boolean isActive();

	@IgnoreDisabledProxy
	boolean isInstalled();

	Object getLPPipeType();

	void registerTrigger();

	ICraftingParts getRecipeParts();

	void addCraftingRecipes(ICraftingParts parts);

	Class<? extends ICraftingRecipeProvider> getAssemblyTableProviderClass();

	void notifyOfChange(LogisticsTileGenericPipe logisticsTileGenericPipe, TileEntity tile, EnumFacing o);

	IBCTilePart getBCTilePart(LogisticsTileGenericPipe logisticsTileGenericPipe);

	IBCClickResult handleBCClickOnPipe(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumFacing side, float xOffset, float yOffset, float zOffset, CoreUnroutedPipe pipe);

	void callBCNeighborBlockChange(IBlockAccess world, BlockPos pos, BlockPos neighbor);

	void callBCRemovePipe(World world, int x, int y, int z);

	void logWarning(String format);

	IBCRenderTESR getBCRenderTESR();

	boolean isTileGenericPipe(TileEntity tile);

	void cleanup();
}
