package logisticspipes.proxy.side;

import java.util.HashMap;

import logisticspipes.LogisticsPipes;
import logisticspipes.blocks.CraftingSignRenderer;
import logisticspipes.blocks.LogisticsSignTileEntity;
import logisticspipes.blocks.LogisticsSolderingTileEntity;
import logisticspipes.pipefxhandlers.EntityBlueSparkleFXProvider;
import logisticspipes.pipefxhandlers.EntityGoldSparkleFXProvider;
import logisticspipes.pipefxhandlers.EntityGreenSparkleFXProvider;
import logisticspipes.pipefxhandlers.EntityOrangeSparkleFXProvider;
import logisticspipes.pipefxhandlers.EntityRedSparkleFXProvider;
import logisticspipes.pipefxhandlers.EntityVioletSparkleFXProvider;
import logisticspipes.pipefxhandlers.EntityWhiteSparkleFXProvider;
import logisticspipes.pipefxhandlers.PipeFXRenderHandler;
import logisticspipes.proxy.interfaces.IProxy;
import logisticspipes.textures.LogisticsPipesTextureStatic;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.World;
import net.minecraftforge.common.DimensionManager;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.TextureFXManager;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Side;
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
		GameRegistry.registerTileEntity(LogisticsPipes.logisticsTileGenericPipe, LogisticsPipes.logisticsTileGenericPipeMapping);
	}

	@Override
	public World getWorld(int _dimension) {
		if(FMLCommonHandler.instance().getEffectiveSide().equals(Side.CLIENT)) {
			return getWorld();
		} else {
			return DimensionManager.getWorld(_dimension);
		}
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
	public void spawnGenericParticle(String particle, double x, double y, double z, int amount) {
		PipeFXRenderHandler.spawnGenericParticle(particle, x, y, z, amount);
	}

	@Override
	public void registerParticles() {
		HashMap particlemap = PipeFXRenderHandler.particlemap;
		particlemap.put("WhiteParticle", new EntityWhiteSparkleFXProvider());
		particlemap.put("RedParticle", new EntityRedSparkleFXProvider());
		particlemap.put("BlueParticle", new EntityBlueSparkleFXProvider());
		particlemap.put("GreenParticle", new EntityGreenSparkleFXProvider());
		particlemap.put("GoldParticle", new EntityGoldSparkleFXProvider());
		particlemap.put("VioletParticle", new EntityVioletSparkleFXProvider());
		particlemap.put("OrangeParticle", new EntityOrangeSparkleFXProvider());
	}
	

}
