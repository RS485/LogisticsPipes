package logisticspipes.proxy.side;

import logisticspipes.LogisticsPipes;
import logisticspipes.blocks.CraftingSignRenderer;
import logisticspipes.blocks.LogisticsSecurityTileEntity;
import logisticspipes.blocks.LogisticsSignTileEntity;
import logisticspipes.blocks.LogisticsSolderingTileEntity;
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
import logisticspipes.proxy.buildcraft.BuildCraftProxy;
import logisticspipes.proxy.interfaces.IProxy;
import logisticspipes.renderer.LogisticsRenderPipe;
import logisticspipes.textures.LogisticsPipesTextureStatic;
import logisticspipes.utils.ItemIdentifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.world.World;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.TextureFXManager;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.common.registry.GameRegistry;


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
	public void registerTileEntitis() {
		ClientRegistry.registerTileEntity(LogisticsSignTileEntity.class, "net.minecraft.src.buildcraft.logisticspipes.blocks.LogisticsTileEntiy", new CraftingSignRenderer());
		ClientRegistry.registerTileEntity(LogisticsSignTileEntity.class, "logisticspipes.blocks.LogisticsSignTileEntity", new CraftingSignRenderer());
		GameRegistry.registerTileEntity(LogisticsSolderingTileEntity.class, "logisticspipes.blocks.LogisticsSolderingTileEntity");
		GameRegistry.registerTileEntity(LogisticsPipes.powerTileEntity, "logisticspipes.blocks.powertile.LogisticsPowerJuntionTileEntity");
		GameRegistry.registerTileEntity(LogisticsSecurityTileEntity.class, "logisticspipes.blocks.LogisticsSecurityTileEntity");
		if(!Configs.LOGISTICS_TILE_GENERIC_PIPE_REPLACEMENT_DISABLED) {
			GameRegistry.registerTileEntity(BuildCraftProxy.logisticsTileGenericPipe, LogisticsPipes.logisticsTileGenericPipeMapping);
		}
		LogisticsRenderPipe lrp = new LogisticsRenderPipe();
		ClientRegistry.bindTileEntitySpecialRenderer(BuildCraftProxy.logisticsTileGenericPipe, lrp);
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
	public void addLogisticsPipesOverride(int index, String override1, String override2) {
		TextureFXManager.instance().addAnimation(new LogisticsPipesTextureStatic(index, override1, override2));
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
			name = Item.itemsList[item.itemID].getItemDisplayName(item.makeNormalStack(1));
			if(name == null) {
				throw new Exception();
			}
		} catch(Exception e) {
			try {
				name = Item.itemsList[item.itemID].getItemNameIS(item.makeNormalStack(1));
				if(name == null) {
					throw new Exception();
				}
			} catch(Exception e1) {
				try {
					name = Item.itemsList[item.itemID].getItemName();
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
}
