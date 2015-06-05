package logisticspipes.proxy.interfaces;

import logisticspipes.asm.IgnoreDisabledProxy;
import logisticspipes.pipes.basic.CoreUnroutedPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.buildcraft.subproxies.IBCClickResult;
import logisticspipes.proxy.buildcraft.subproxies.IBCRenderTESR;
import logisticspipes.proxy.buildcraft.subproxies.IBCTilePart;
import logisticspipes.proxy.buildcraft.subproxies.IConnectionOverrideResult;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import net.minecraftforge.common.util.ForgeDirection;

public interface IBCProxy {

	void resetItemRotation();

	boolean isIPipeTile(TileEntity tile);

	void registerPipeInformationProvider();

	void initProxy();

	boolean checkForPipeConnection(TileEntity with, ForgeDirection side, LogisticsTileGenericPipe pipe);

	IConnectionOverrideResult checkConnectionOverride(TileEntity with, ForgeDirection side, LogisticsTileGenericPipe pipe);

	/** Only used by the BC proxy internaly */
	boolean canPipeConnect(TileEntity pipe, TileEntity tile, ForgeDirection direction);

	boolean isActive();

	@IgnoreDisabledProxy
	boolean isInstalled();

	Object getLPPipeType();

	void registerTrigger();

	ICraftingParts getRecipeParts();

	void addCraftingRecipes(ICraftingParts parts);

	Class<? extends ICraftingRecipeProvider> getAssemblyTableProviderClass();

	void notifyOfChange(LogisticsTileGenericPipe logisticsTileGenericPipe, TileEntity tile, ForgeDirection o);

	IBCTilePart getBCTilePart(LogisticsTileGenericPipe logisticsTileGenericPipe);

	IBCClickResult handleBCClickOnPipe(World world, int x, int y, int z, EntityPlayer player, int side, float xOffset, float yOffset, float zOffset, CoreUnroutedPipe pipe);

	void callBCNeighborBlockChange(World world, int x, int y, int z, Block block);

	void callBCRemovePipe(World world, int x, int y, int z);

	void logWarning(String format);

	IBCRenderTESR getBCRenderTESR();

	boolean isTileGenericPipe(TileEntity tile);

	void cleanup();
}
