package logisticspipes.proxy.interfaces;

import logisticspipes.asm.IgnoreDisabledProxy;
import logisticspipes.pipes.basic.CoreUnroutedPipe;
import logisticspipes.pipes.basic.LogisticsBlockGenericPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.buildcraft.subproxies.IBCCoreState;
import logisticspipes.proxy.buildcraft.subproxies.IBCPipePart;
import logisticspipes.proxy.buildcraft.subproxies.IBCRenderState;
import logisticspipes.proxy.buildcraft.subproxies.IBCTilePart;
import logisticspipes.proxy.buildcraft.subproxies.IConnectionOverrideResult;
import logisticspipes.renderer.state.PipeRenderState;
import logisticspipes.transport.LPTravelingItem;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public interface IBCProxy {
	void resetItemRotation();
	boolean insertIntoBuildcraftPipe(TileEntity tile, LPTravelingItem item);
	boolean isIPipeTile(TileEntity tile);
	void registerPipeInformationProvider();
	void initProxy();
	boolean checkForPipeConnection(TileEntity with, ForgeDirection side, LogisticsTileGenericPipe pipe);
	IConnectionOverrideResult checkConnectionOverride(TileEntity with, ForgeDirection side, LogisticsTileGenericPipe pipe);
	IBCPipePart getBCPipePart(LogisticsTileGenericPipe tile);
	boolean handleBCClickOnPipe(ItemStack currentItem, CoreUnroutedPipe pipe, World world, int x, int y, int z, EntityPlayer player, int side, LogisticsBlockGenericPipe logisticsBlockGenericPipe);
	ItemStack getPipePlugItemStack();
	ItemStack getRobotStationItemStack();
	boolean stripEquipment(World world, int x, int y, int z, EntityPlayer player, CoreUnroutedPipe pipe, LogisticsBlockGenericPipe block);
	IBCTilePart getBCTilePart(LogisticsTileGenericPipe tile);
	void notifyOfChange(LogisticsTileGenericPipe pipe, TileEntity tile, ForgeDirection o);
	@SideOnly(Side.CLIENT) void renderGatesWires(LogisticsTileGenericPipe pipe, double x, double y, double z);
	@SideOnly(Side.CLIENT) void pipeFacadeRenderer(RenderBlocks renderblocks, LogisticsBlockGenericPipe block, PipeRenderState state, int x, int y, int z);
	@SideOnly(Side.CLIENT) void pipePlugRenderer(RenderBlocks renderblocks, Block block, PipeRenderState state, int x, int y, int z);
	ItemStack getDropFacade(CoreUnroutedPipe pipe, ForgeDirection dir);
	/** Only used by the BC proxy internaly */
	boolean canPipeConnect(TileEntity pipe, TileEntity tile, ForgeDirection direction);
	@SideOnly(Side.CLIENT) void pipeRobotStationRenderer(RenderBlocks renderblocks, LogisticsBlockGenericPipe block, PipeRenderState state, int x, int y, int z);
	boolean isActive();
	@IgnoreDisabledProxy
	boolean isInstalled();
	Object getLPPipeType();
	void registerTrigger();
	ICraftingParts getRecipeParts();
	void addCraftingRecipes(ICraftingParts parts);
	Object overridePipeConnection(LogisticsTileGenericPipe pipe, Object type, ForgeDirection dir);
	IBCCoreState getBCCoreState();
	IBCRenderState getBCRenderState();
	void checkUpdateNeighbour(TileEntity tile);
	void logWarning(String format);
	Class<? extends ICraftingRecipeProvider> getAssemblyTableProviderClass();
	boolean isTileGenericPipe(TileEntity tile);
}
