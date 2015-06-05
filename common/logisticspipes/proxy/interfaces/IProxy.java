package logisticspipes.proxy.interfaces;

import logisticspipes.items.ItemLogisticsPipe;
import logisticspipes.modules.abstractmodules.LogisticsModule;
import logisticspipes.pipes.basic.CoreUnroutedPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.utils.item.ItemIdentifier;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetHandler;
import net.minecraft.world.World;

import net.minecraftforge.client.IItemRenderer;

public interface IProxy {

	public String getSide();

	public World getWorld();

	public void registerTileEntities();

	public EntityPlayer getClientPlayer();

	public void addLogisticsPipesOverride(IIconRegister par1IIconRegister, int index, String override1, String override2, boolean flag);

	public void registerParticles();

	public String getName(ItemIdentifier item);

	public void updateNames(ItemIdentifier item, String name);

	public void tick();

	public void sendNameUpdateRequest(EntityPlayer player);

	public int getDimensionForWorld(World world);

	public LogisticsTileGenericPipe getPipeInDimensionAt(int dimension, int x, int y, int z, EntityPlayer player);

	public void sendBroadCast(String message);

	public void tickServer();

	public void tickClient();

	public EntityPlayer getEntityPlayerFromNetHandler(INetHandler handler);

	public void setIconProviderFromPipe(ItemLogisticsPipe item, CoreUnroutedPipe dummyPipe);

	public LogisticsModule getModuleFromGui();

	public IItemRenderer getPipeItemRenderer();

	public boolean checkSinglePlayerOwner(String commandSenderName);

	public void openFluidSelectGui(int slotId);
}
