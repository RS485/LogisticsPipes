package logisticspipes.proxy.side;

import logisticspipes.LogisticsPipes;
import logisticspipes.blocks.LogisticsSignTileEntity;
import logisticspipes.blocks.LogisticsSolderingTileEntity;
import logisticspipes.proxy.interfaces.IProxy;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.World;
import net.minecraftforge.common.DimensionManager;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.server.FMLServerHandler;

public class ServerProxy implements IProxy {
	@Override
	public String getSide() {
		return "Server";
	}

	@Override
	public World getWorld() {
		return null;
	}

	@Override
	public void registerTileEntitis() {
		GameRegistry.registerTileEntity(LogisticsSignTileEntity.class, "net.minecraft.src.buildcraft.logisticspipes.blocks.LogisticsTileEntiy");
		GameRegistry.registerTileEntity(LogisticsSignTileEntity.class, "logisticspipes.blocks.LogisticsSignTileEntity");
		GameRegistry.registerTileEntity(LogisticsSolderingTileEntity.class, "logisticspipes.blocks.LogisticsSolderingTileEntity");
		GameRegistry.registerTileEntity(LogisticsPipes.powerTileEntity, "logisticspipes.blocks.powertile.LogisticsPowerJuntionTileEntity");
		GameRegistry.registerTileEntity(LogisticsPipes.logisticsTileGenericPipe, LogisticsPipes.logisticsTileGenericPipeMapping);
	}

	@Override
	public World getWorld(int _dimension) {
		return DimensionManager.getWorld(_dimension);
	}

	@Override
	public EntityPlayer getClientPlayer() {
		return null;
	}

	@Override
	public boolean isMainThreadRunning() {
		return FMLServerHandler.instance().getServer().isServerRunning();
	}

	@Override
	public void addLogisticsPipesOverride(int index, String override1, String override2) {
		//Only Client Side
	}

	@Override
	public void registerParticles() {
		//Only Client Side
	}
}
