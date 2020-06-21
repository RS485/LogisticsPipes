package logisticspipes.proxy.interfaces;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetHandler;
import net.minecraft.world.World;

import logisticspipes.items.ItemLogisticsPipe;
import logisticspipes.modules.LogisticsModule;
import logisticspipes.pipes.basic.CoreUnroutedPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.utils.item.ItemIdentifier;

public interface IProxy {

	String getSide();

	World getWorld();

	void registerTileEntities();

	EntityPlayer getClientPlayer();

	void addLogisticsPipesOverride(Object par1IIconRegister, int index, String override1, String override2, boolean flag);

	void registerParticles();

	String getName(ItemIdentifier item);

	void updateNames(ItemIdentifier item, String name);

	void tick();

	void sendNameUpdateRequest(EntityPlayer player);

	LogisticsTileGenericPipe getPipeInDimensionAt(int dimension, int x, int y, int z, EntityPlayer player);

	void sendBroadCast(String message);

	void tickServer();

	void tickClient();

	EntityPlayer getEntityPlayerFromNetHandler(INetHandler handler);

	void setIconProviderFromPipe(ItemLogisticsPipe item, CoreUnroutedPipe dummyPipe);

	LogisticsModule getModuleFromGui();

	boolean checkSinglePlayerOwner(String commandSenderName);

	void openFluidSelectGui(int slotId);

	default void registerModels() {}

	void registerTextures();

	void initModelLoader();

	int getRenderIndex();
}
