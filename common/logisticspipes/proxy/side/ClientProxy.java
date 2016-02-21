package logisticspipes.proxy.side;

import logisticspipes.Client.Modelhelper;
import logisticspipes.LPConstants;
import logisticspipes.LogisticsPipes;
import logisticspipes.blocks.LogisticsSecurityTileEntity;
import logisticspipes.blocks.LogisticsSolderingTileEntity;
import logisticspipes.blocks.LogisticsSolidBlock;
import logisticspipes.blocks.crafting.LogisticsCraftingTableTileEntity;
import logisticspipes.blocks.powertile.LogisticsIC2PowerProviderTileEntity;
import logisticspipes.blocks.powertile.LogisticsPowerJunctionTileEntity;
import logisticspipes.blocks.powertile.LogisticsRFPowerProviderTileEntity;
import logisticspipes.blocks.stats.LogisticsStatisticsTileEntity;
import logisticspipes.gui.modules.ModuleBaseGui;
import logisticspipes.gui.popup.SelectItemOutOfList;
import logisticspipes.gui.popup.SelectItemOutOfList.IHandleItemChoise;
import logisticspipes.items.ItemLogisticsPipe;
import logisticspipes.items.LogisticsItem;
import logisticspipes.modules.abstractmodules.LogisticsModule;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.gui.DummyContainerSlotClick;
import logisticspipes.pipefxhandlers.Particles;
import logisticspipes.pipefxhandlers.PipeFXRenderHandler;
import logisticspipes.pipefxhandlers.providers.*;
import logisticspipes.pipes.basic.CoreUnroutedPipe;
import logisticspipes.pipes.basic.LogisticsBlockGenericPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.proxy.interfaces.IProxy;
import logisticspipes.reference.Reference;
import logisticspipes.renderer.LogisticsPipeWorldRenderer;
import logisticspipes.renderer.LogisticsRenderPipe;
import logisticspipes.renderer.LogisticsSolidBlockWorldRenderer;
import logisticspipes.renderer.newpipe.GLRenderListHandler;
import logisticspipes.utils.FluidIdentifier;
import logisticspipes.utils.UtilWorld;
import logisticspipes.utils.gui.LogisticsBaseGuiScreen;
import logisticspipes.utils.gui.SubGuiScreen;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.item.ItemIdentifierStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.registry.GameRegistry;

import java.util.ArrayList;
import java.util.List;

public class ClientProxy implements IProxy {

	private static ArrayList<BlockModelEntry> blocksToRegister = new ArrayList();
	private static ArrayList<ItemModelEntry> itemsToRegister = new ArrayList();
	private static ArrayList<PipeModelEntry> PipesToRegister = new ArrayList();

	@Override
	public String getSide() {
		return "Client";
	}

	@Override
	public World getWorld() {
		return FMLClientHandler.instance().getClient().theWorld;
	}

	@Override
	public void registerTileEntities() {
		GameRegistry.registerTileEntity(LogisticsSolderingTileEntity.class, "logisticspipes.blocks.LogisticsSolderingTileEntity");
		GameRegistry.registerTileEntity(LogisticsPowerJunctionTileEntity.class, "logisticspipes.blocks.powertile.LogisticsPowerJuntionTileEntity");
		GameRegistry.registerTileEntity(LogisticsRFPowerProviderTileEntity.class, "logisticspipes.blocks.powertile.LogisticsRFPowerProviderTileEntity");
		GameRegistry.registerTileEntity(LogisticsIC2PowerProviderTileEntity.class, "logisticspipes.blocks.powertile.LogisticsIC2PowerProviderTileEntity");
		GameRegistry.registerTileEntity(LogisticsSecurityTileEntity.class, "logisticspipes.blocks.LogisticsSecurityTileEntity");
		GameRegistry.registerTileEntity(LogisticsCraftingTableTileEntity.class, "logisticspipes.blocks.crafting.LogisticsCraftingTableTileEntity");
		GameRegistry.registerTileEntity(LogisticsTileGenericPipe.class, LogisticsPipes.logisticsTileGenericPipeMapping);
		GameRegistry.registerTileEntity(LogisticsStatisticsTileEntity.class, "logisticspipes.blocks.stats.LogisticsStatisticsTileEntity");

		LPConstants.pipeModel = RenderingRegistry.getNextAvailableRenderId();
		LPConstants.solidBlockModel = RenderingRegistry.getNextAvailableRenderId();

		LogisticsRenderPipe lrp = new LogisticsRenderPipe();
		ClientRegistry.bindTileEntitySpecialRenderer(LogisticsTileGenericPipe.class, lrp);

		RenderingRegistry.registerBlockHandler(new LogisticsPipeWorldRenderer());

		RenderingRegistry.registerBlockHandler(new LogisticsSolidBlockWorldRenderer());

		SimpleServiceLocator.buildCraftProxy.resetItemRotation();

		SimpleServiceLocator.setRenderListHandler(new GLRenderListHandler());
	}

	@Override
	public EntityPlayer getClientPlayer() {
		return FMLClientHandler.instance().getClient().thePlayer;
	}

	@Override
	public void registerParticles() {
		PipeFXRenderHandler.registerParticleHandler(Particles.WhiteParticle, new EntityWhiteSparkleFXProvider());
		PipeFXRenderHandler.registerParticleHandler(Particles.RedParticle, new EntityRedSparkleFXProvider());
		PipeFXRenderHandler.registerParticleHandler(Particles.BlueParticle, new EntityBlueSparkleFXProvider());
		PipeFXRenderHandler.registerParticleHandler(Particles.GreenParticle, new EntityGreenSparkleFXProvider());
		PipeFXRenderHandler.registerParticleHandler(Particles.GoldParticle, new EntityGoldSparkleFXProvider());
		PipeFXRenderHandler.registerParticleHandler(Particles.VioletParticle, new EntityVioletSparkleFXProvider());
		PipeFXRenderHandler.registerParticleHandler(Particles.OrangeParticle, new EntityOrangeSparkleFXProvider());
		PipeFXRenderHandler.registerParticleHandler(Particles.LightGreenParticle, new EntityLightGreenSparkleFXProvider());
		PipeFXRenderHandler.registerParticleHandler(Particles.LightRedParticle, new EntityLightRedSparkleFXProvider());
	}

	@Override
	public String getName(ItemIdentifier item) {
		return item.getFriendlyName();
	}

	@Override
	public void updateNames(ItemIdentifier item, String name) {
		//Not Client Side
	}

	@Override
	public void tick() {
		//Not Client Side
	}

	@Override
	public void sendNameUpdateRequest(EntityPlayer player) {
		//Not Client Side
	}

	@Override
	public int getDimensionForWorld(World world) {
		if (world instanceof WorldServer) {
			return ((WorldServer) world).provider.getDimensionId();
		}
		if (world instanceof WorldClient) {
			return ((WorldClient) world).provider.getDimensionId();
		}
		return world.getWorldInfo().getVanillaDimension();
	}

	@Override
	public LogisticsTileGenericPipe getPipeInDimensionAt(int dimension, BlockPos pos, EntityPlayer player) {
		return ClientProxy.getPipe(DimensionManager.getWorld(dimension),pos);
	}

	// BuildCraft method
	/**
	 * Retrieves pipe at specified coordinates if any.
	 * 
	 * @param world
	 * @param pos
	 * @return
	 */
	private static LogisticsTileGenericPipe getPipe(World world, BlockPos pos) {
		if (world == null || !UtilWorld.blockExists(pos ,world)) {
			return null;
		}

		final TileEntity tile = world.getTileEntity(pos);
		if (!(tile instanceof LogisticsTileGenericPipe)) {
			return null;
		}

		return (LogisticsTileGenericPipe) tile;
	}

	// BuildCraft method end

	@Override
	public void sendBroadCast(String message) {
		if (Minecraft.getMinecraft().thePlayer != null) {
			Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("[LP] Client: " + message));
		}
	}

	@Override
	public void tickServer() {}

	@Override
	public void tickClient() {
		MainProxy.addTick();
		SimpleServiceLocator.renderListHandler.tick();
	}

	@Override
	public EntityPlayer getEntityPlayerFromNetHandler(INetHandler handler) {
		if (handler instanceof NetHandlerPlayServer) {
			return ((NetHandlerPlayServer) handler).playerEntity;
		} else {
			return Minecraft.getMinecraft().thePlayer;
		}
	}

	@Override
	public LogisticsModule getModuleFromGui() {
		if (FMLClientHandler.instance().getClient().currentScreen instanceof ModuleBaseGui) {
			return ((ModuleBaseGui) FMLClientHandler.instance().getClient().currentScreen).getModule();
		}
		return null;
	}


	@Override
	public boolean checkSinglePlayerOwner(String commandSenderName) {
		return FMLCommonHandler.instance().getMinecraftServerInstance().isSinglePlayer() && FMLCommonHandler.instance().getMinecraftServerInstance() instanceof IntegratedServer && !((IntegratedServer) FMLCommonHandler.instance().getMinecraftServerInstance()).getPublic();
	}

	@Override
	public void openFluidSelectGui(final int slotId) {
		if (Minecraft.getMinecraft().currentScreen instanceof LogisticsBaseGuiScreen) {
			final List<ItemIdentifierStack> list = new ArrayList<ItemIdentifierStack>();
			for (FluidIdentifier fluid : FluidIdentifier.all()) {
				if (fluid == null) {
					continue;
				}
				list.add(fluid.getItemIdentifier().makeStack(1));
			}
			SelectItemOutOfList subGui = new SelectItemOutOfList(list, new IHandleItemChoise() {

				@Override
				public void handleItemChoise(int slot) {
					MainProxy.sendPacketToServer(PacketHandler.getPacket(DummyContainerSlotClick.class).setSlotId(slotId).setStack(list.get(slot).makeNormalStack()).setButton(0));
				}
			});
			LogisticsBaseGuiScreen gui = (LogisticsBaseGuiScreen) Minecraft.getMinecraft().currentScreen;
			if (!gui.hasSubGui()) {
				gui.setSubGui(subGui);
			} else {
				SubGuiScreen nextGui = gui.getSubGui();
				while (nextGui.hasSubGui()) {
					nextGui = nextGui.getSubGui();
				}
				nextGui.setSubGui(subGui);
			}
		} else {
			throw new UnsupportedOperationException(String.valueOf(Minecraft.getMinecraft().currentScreen));
		}
	}

	@Override
	public void registerItemRenders() {

	}

	@Override
	public void registerBlockRenderers() {

	}

	@Override
	public void registerPipeRenderers() {

	}

	@Override
	public void registerBlockForMeshing(LogisticsSolidBlock block, int metadata, String name) {
		{
			for (BlockModelEntry blockModelEntry : blocksToRegister)
			{
				Modelhelper.registerBlock(blockModelEntry.block, blockModelEntry.metadata, Reference.MOD_ID + ":" + blockModelEntry.name
				);
			}
		}

	}

	@Override
	public void registerItemForMeshing(LogisticsItem item, int metadata, String name) {
		{
			for (ItemModelEntry ItemModelEntry : itemsToRegister)
			{
				Modelhelper.registerItem(ItemModelEntry.item, ItemModelEntry.metadata, Reference.MOD_ID + ":" + ItemModelEntry.name
				);
			}
		}

	}

	@Override
	public void registerPipeForMeshing(LogisticsTileGenericPipe block, int metadata, String name) {
		{
			for (PipeModelEntry pipeModelEntry : PipesToRegister)
			{
				Modelhelper.registerBlock(pipeModelEntry.block, pipeModelEntry.metadata, Reference.MOD_ID + ":" + pipeModelEntry.name
				);
			}
		}

	}
	private static class BlockModelEntry
	{
		public LogisticsSolidBlock block;
		public int metadata;
		public String name;
		public BlockModelEntry(LogisticsSolidBlock block, int metadata, String name)
		{
			this.block = block;
			this.metadata = metadata;
			this.name = name;
		}
	}
	private static class ItemModelEntry
	{
		public LogisticsItem item;
		public int metadata;
		public String name;
		public ItemModelEntry(LogisticsItem item, int metadata, String name)
		{
			this.item = item;
			this.metadata = metadata;
			this.name = name;
		}
	}
	private static class PipeModelEntry
	{
		public LogisticsBlockGenericPipe block;
		public int metadata;
		public String name;
		public PipeModelEntry(LogisticsBlockGenericPipe block, int metadata, String name)
		{
			this.block = block;
			this.metadata = metadata;
			this.name = name;
		}
	}
}
