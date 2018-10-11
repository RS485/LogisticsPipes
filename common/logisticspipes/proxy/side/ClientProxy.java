package logisticspipes.proxy.side;

import java.util.ArrayList;
import java.util.List;

import logisticspipes.LogisticsPipes;
import logisticspipes.gui.GuiCraftingPipe;
import logisticspipes.gui.modules.ModuleBaseGui;
import logisticspipes.gui.popup.SelectItemOutOfList;
import logisticspipes.interfaces.ILogisticsItem;
import logisticspipes.items.ItemLogisticsPipe;
import logisticspipes.items.LogisticsSolidBlockItem;
import logisticspipes.modules.abstractmodules.LogisticsModule;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.gui.DummyContainerSlotClick;
import logisticspipes.pipefxhandlers.Particles;
import logisticspipes.pipefxhandlers.PipeFXRenderHandler;
import logisticspipes.pipefxhandlers.providers.EntityBlueSparkleFXProvider;
import logisticspipes.pipefxhandlers.providers.EntityGoldSparkleFXProvider;
import logisticspipes.pipefxhandlers.providers.EntityGreenSparkleFXProvider;
import logisticspipes.pipefxhandlers.providers.EntityLightGreenSparkleFXProvider;
import logisticspipes.pipefxhandlers.providers.EntityLightRedSparkleFXProvider;
import logisticspipes.pipefxhandlers.providers.EntityOrangeSparkleFXProvider;
import logisticspipes.pipefxhandlers.providers.EntityRedSparkleFXProvider;
import logisticspipes.pipefxhandlers.providers.EntityVioletSparkleFXProvider;
import logisticspipes.pipefxhandlers.providers.EntityWhiteSparkleFXProvider;
import logisticspipes.pipes.basic.CoreUnroutedPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.proxy.interfaces.IProxy;
import logisticspipes.renderer.FluidContainerRenderer;
import logisticspipes.renderer.LogisticsRenderPipe;
import logisticspipes.renderer.newpipe.GLRenderListHandler;
import logisticspipes.renderer.newpipe.LogisticsBlockModel;
import logisticspipes.renderer.newpipe.LogisticsNewPipeModel;
import logisticspipes.renderer.newpipe.LogisticsNewRenderPipe;
import logisticspipes.textures.Textures;
import logisticspipes.utils.FluidIdentifier;
import logisticspipes.utils.gui.LogisticsBaseGuiScreen;
import logisticspipes.utils.gui.SubGuiScreen;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.item.ItemIdentifierStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.DimensionManager;

import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ClientProxy implements IProxy {


	@Override
	public String getSide() {
		return "Client";
	}

	@Override
	public World getWorld() {
		return FMLClientHandler.instance().getClient().world;
	}

	@Override
	public void registerTileEntities() {
		LogisticsRenderPipe lrp = new LogisticsRenderPipe();
		ClientRegistry.bindTileEntitySpecialRenderer(LogisticsTileGenericPipe.class, lrp);

		SimpleServiceLocator.setRenderListHandler(new GLRenderListHandler());
	}

	@Override
	public EntityPlayer getClientPlayer() {
		return FMLClientHandler.instance().getClient().player;
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
			return ((WorldServer) world).provider.getDimension();
		}
		if (world instanceof WorldClient) {
			return ((WorldClient) world).provider.getDimension();
		}
		return 0;
	}

	@Override
	public LogisticsTileGenericPipe getPipeInDimensionAt(int dimension, int x, int y, int z, EntityPlayer player) {
		return ClientProxy.getPipe(DimensionManager.getWorld(dimension), x, y, z);
	}

	// BuildCraft method
	/**
	 * Retrieves pipe at specified coordinates if any.
	 * 
	 * @param world
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	private static LogisticsTileGenericPipe getPipe(World world, int x, int y, int z) {
		if (world == null || world.isAirBlock(new BlockPos(x, y, z))) {
			return null;
		}

		final TileEntity tile = world.getTileEntity(new BlockPos(x, y, z));
		if (!(tile instanceof LogisticsTileGenericPipe)) {
			return null;
		}

		return (LogisticsTileGenericPipe) tile;
	}

	// BuildCraft method end

	@Override
	public void addLogisticsPipesOverride(TextureMap par1IIconRegister, int index, String override1, String override2, boolean flag) {
		if (par1IIconRegister != null) {
			if ("NewPipeTexture".equals(override2) && !override1.contains("status_overlay")) {
				Textures.LPnewPipeIconProvider.setIcon(index, par1IIconRegister.registerSprite(new ResourceLocation("logisticspipes", override1.replace("pipes/", "blocks/pipes/new_texture/"))));
			} else if (flag) {
				Textures.LPpipeIconProvider.setIcon(index, par1IIconRegister.registerSprite(new ResourceLocation("logisticspipes", "blocks/" + override1)));
			} else {
				Textures.LPpipeIconProvider.setIcon(index, par1IIconRegister.registerSprite(new ResourceLocation("logisticspipes", "blocks/" + override1.replace("pipes/", "pipes/overlay_gen/") + "/" + override2.replace("pipes/status_overlay/", ""))));
			}
		}
	}

	@Override
	public void sendBroadCast(String message) {
		if (Minecraft.getMinecraft().player != null) {
			Minecraft.getMinecraft().player.sendMessage(new TextComponentString("[LP] Client: " + message));
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
			return ((NetHandlerPlayServer) handler).player;
		} else {
			return Minecraft.getMinecraft().player;
		}
	}

	@Override
	public void setIconProviderFromPipe(ItemLogisticsPipe item, CoreUnroutedPipe dummyPipe) {
		item.setPipesIcons(dummyPipe.getIconProvider());
	}

	@Override
	public LogisticsModule getModuleFromGui() {
		if (FMLClientHandler.instance().getClient().currentScreen instanceof ModuleBaseGui) {
			return ((ModuleBaseGui) FMLClientHandler.instance().getClient().currentScreen).getModule();
		}
		if (FMLClientHandler.instance().getClient().currentScreen instanceof GuiCraftingPipe) {
			return ((GuiCraftingPipe) FMLClientHandler.instance().getClient().currentScreen).get_pipe();
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
			final List<ItemIdentifierStack> list = new ArrayList<>();
			for (FluidIdentifier fluid : FluidIdentifier.all()) {
				if (fluid == null) {
					continue;
				}
				list.add(fluid.getItemIdentifier().makeStack(1));
			}
			SelectItemOutOfList subGui = new SelectItemOutOfList(list, slot -> MainProxy.sendPacketToServer(PacketHandler.getPacket(DummyContainerSlotClick.class).setSlotId(slotId).setStack(list.get(slot).makeNormalStack()).setButton(0)));
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
	public void registerModels(ILogisticsItem logisticsItem) {
		logisticsItem.registerModels();
	}

	@Override
	public void registerTextures() {
		LogisticsPipes.textures.registerBlockIcons(Minecraft.getMinecraft().getTextureMapBlocks());
		LogisticsNewRenderPipe.registerTextures(Minecraft.getMinecraft().getTextureMapBlocks());
		LogisticsPipes.LogisticsSolidBlock.registerBlockIcons(Minecraft.getMinecraft().getTextureMapBlocks());
		LogisticsNewPipeModel.registerTextures(Minecraft.getMinecraft().getTextureMapBlocks());
	}

	@Override
	public void initModelLoader() {
		ModelLoaderRegistry.registerLoader(new LogisticsNewPipeModel.LogisticsNewPipeModelLoader());
		ModelLoaderRegistry.registerLoader(new LogisticsBlockModel.LogisticsBlockModelLoader());
		ModelLoaderRegistry.registerLoader(new FluidContainerRenderer.FluidContainerRendererModelLoader());
	}

	@Override
	public LogisticsSolidBlockItem registerSolidBlockModel(LogisticsSolidBlockItem logisticsSolidBlockItem) {
		return logisticsSolidBlockItem.registerModels();
	}
}
