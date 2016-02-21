package logisticspipes.proxy.interfaces;

import logisticspipes.blocks.LogisticsSolidBlock;
import logisticspipes.items.LogisticsItem;
import logisticspipes.modules.abstractmodules.LogisticsModule;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.utils.item.ItemIdentifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetHandler;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public interface IProxy {

	public String getSide();

	public World getWorld();

	public void registerTileEntities();

	public EntityPlayer getClientPlayer();

//	public void addLogisticsPipesOverride(IIconRegister par1IIconRegister, int index, String override1, String override2, boolean flag);

	public void registerParticles();

	public String getName(ItemIdentifier item);

	public void updateNames(ItemIdentifier item, String name);

	public void tick();

	public void sendNameUpdateRequest(EntityPlayer player);

	public int getDimensionForWorld(World world);

	public LogisticsTileGenericPipe getPipeInDimensionAt(int dimension, BlockPos pos, EntityPlayer player);

	public void sendBroadCast(String message);

	public void tickServer();

	public void tickClient();

	public EntityPlayer getEntityPlayerFromNetHandler(INetHandler handler);

//	public void setIconProviderFromPipe(ItemLogisticsPipe item, CoreUnroutedPipe dummyPipe);

	public LogisticsModule getModuleFromGui();

//	public IItemRenderer getPipeItemRenderer();

	public boolean checkSinglePlayerOwner(String commandSenderName);

	public void openFluidSelectGui(int slotId);

	public abstract void registerItemRenders();;

	public abstract void registerBlockRenderers();

	public abstract void registerPipeRenderers();

	public abstract void registerBlockForMeshing(LogisticsSolidBlock block, int metadata, String name);

	public abstract void registerItemForMeshing(LogisticsItem item, int metadata, String name);

	public abstract void registerPipeForMeshing(LogisticsTileGenericPipe block, int metadata, String name);
}
