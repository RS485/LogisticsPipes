package logisticspipes.proxy.side;

import logisticspipes.LogisticsPipes;
import logisticspipes.blocks.LogisticsSecurityTileEntity;
import logisticspipes.blocks.LogisticsSignTileEntity;
import logisticspipes.blocks.LogisticsSolderingTileEntity;
import logisticspipes.blocks.crafting.LogisticsCraftingTableTileEntity;
import logisticspipes.blocks.powertile.LogisticsPowerJunctionTileEntity;
import logisticspipes.config.Configs;
import logisticspipes.pipefxhandlers.Particles;
import logisticspipes.pipefxhandlers.PipeFXRenderHandler;
import logisticspipes.pipefxhandlers.providers.EntityBlueSparkleFXProvider;
import logisticspipes.pipefxhandlers.providers.EntityGoldSparkleFXProvider;
import logisticspipes.pipefxhandlers.providers.EntityGreenSparkleFXProvider;
import logisticspipes.pipefxhandlers.providers.EntityOrangeSparkleFXProvider;
import logisticspipes.pipefxhandlers.providers.EntityRedSparkleFXProvider;
import logisticspipes.pipefxhandlers.providers.EntityVioletSparkleFXProvider;
import logisticspipes.pipefxhandlers.providers.EntityWhiteSparkleFXProvider;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.proxy.interfaces.IProxy;
import logisticspipes.renderer.LogisticsPipeWorldRenderer;
import logisticspipes.renderer.LogisticsRenderPipe;
import logisticspipes.textures.Textures;
import logisticspipes.utils.ItemIdentifier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import buildcraft.transport.TileGenericPipe;
import buildcraft.transport.render.RenderPipe;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.common.registry.GameRegistry;
//import logisticspipes.textures.LogisticsPipesTextureStatic;


public class ClientProxy implements IProxy {
	
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
		GameRegistry.registerTileEntity(LogisticsSignTileEntity.class, "net.minecraft.src.buildcraft.logisticspipes.blocks.LogisticsTileEntiy");
		GameRegistry.registerTileEntity(LogisticsSignTileEntity.class, "logisticspipes.blocks.LogisticsSignTileEntity");
		GameRegistry.registerTileEntity(LogisticsSolderingTileEntity.class, "logisticspipes.blocks.LogisticsSolderingTileEntity");
		GameRegistry.registerTileEntity(LogisticsPowerJunctionTileEntity.class, "logisticspipes.blocks.powertile.LogisticsPowerJuntionTileEntity");
		GameRegistry.registerTileEntity(LogisticsSecurityTileEntity.class, "logisticspipes.blocks.LogisticsSecurityTileEntity");
		GameRegistry.registerTileEntity(LogisticsCraftingTableTileEntity.class, "logisticspipes.blocks.crafting.LogisticsCraftingTableTileEntity");
		if(!Configs.LOGISTICS_TILE_GENERIC_PIPE_REPLACEMENT_DISABLED) {
			GameRegistry.registerTileEntity(LogisticsTileGenericPipe.class, LogisticsPipes.logisticsTileGenericPipeMapping);
		}
		LogisticsRenderPipe lrp = new LogisticsRenderPipe();
		ClientRegistry.bindTileEntitySpecialRenderer(LogisticsTileGenericPipe.class, lrp);
		SimpleServiceLocator.buildCraftProxy.resetItemRotation(lrp);
		Object brp = TileEntityRenderer.instance.specialRendererMap.get(TileGenericPipe.class);
		if(brp instanceof RenderPipe) {
			SimpleServiceLocator.buildCraftProxy.resetItemRotation((RenderPipe) brp);
		}
		RenderingRegistry.registerBlockHandler(new LogisticsPipeWorldRenderer());
	}

	@Override
	public EntityPlayer getClientPlayer() {
		return FMLClientHandler.instance().getClient().thePlayer;
	}

	@Override
	public boolean isMainThreadRunning() {
		return FMLClientHandler.instance().getClient().running;
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
	}
	
	@Override
	public String getName(ItemIdentifier item) {
		String name = "???";
		try {
			name = Item.itemsList[item.itemID].getItemDisplayName(item.unsafeMakeNormalStack(1));
			if(name == null) {
				throw new Exception();
			}
		} catch(Exception e) {
			try {
				name = Item.itemsList[item.itemID].getUnlocalizedName(item.unsafeMakeNormalStack(1));
				if(name == null) {
					throw new Exception();
				}
			} catch(Exception e1) {
				try {
					name = Item.itemsList[item.itemID].getUnlocalizedName();
					if(name == null) {
						throw new Exception();
					}
				} catch(Exception e2) {
					name = "???"; 
				}
			}
		}
		return name;
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
	public void sendNameUpdateRequest(Player player) {
		//Not Client Side
	}

	@Override
	public int getDimensionForWorld(World world) {
		if(world instanceof WorldServer) {
			return ((WorldServer)world).provider.dimensionId;
		}
		if(world instanceof WorldClient) {
			return ((WorldClient)world).provider.dimensionId;
		}
		return world.getWorldInfo().getVanillaDimension();
	}

	@Override
	public TileGenericPipe getPipeInDimensionAt(int dimension, int x, int y, int z, EntityPlayer player) {
		return getPipe(DimensionManager.getWorld(dimension), x, y, z);
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
	private static TileGenericPipe getPipe(World world, int x, int y, int z) {
		if(world == null) {
			return null;
		}
		if (!world.blockExists(x, y, z)) {
			return null;
		}

		final TileEntity tile = world.getBlockTileEntity(x, y, z);
		if (!(tile instanceof TileGenericPipe)) {
			return null;
		}

		return (TileGenericPipe) tile;
	}
	// BuildCraft method end

	@Override
	public void addLogisticsPipesOverride(IconRegister par1IconRegister, int index, String override1, String override2, boolean flag) {
		if(par1IconRegister != null) {
			if(flag) {
				Textures.LPpipeIconProvider.setIcon(index, par1IconRegister.registerIcon("logisticspipes:"+override1));
			} else {
				Textures.LPpipeIconProvider.setIcon(index, par1IconRegister.registerIcon("logisticspipes:"+override1.replace("pipes/", "pipes/overlay_gen/")+"/"+override2.replace("pipes/status_overlay/","")));
			}
		}
	}
}
