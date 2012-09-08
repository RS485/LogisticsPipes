package logisticspipes.proxy;

import logisticspipes.blocks.LogisticsSignTileEntity;
import logisticspipes.blocks.LogisticsSolderingTileEntity;
import net.minecraft.src.ModLoader;
import net.minecraft.src.World;
import net.minecraftforge.common.DimensionManager;

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
		ModLoader.registerTileEntity(LogisticsSignTileEntity.class, "net.minecraft.src.buildcraft.logisticspipes.blocks.LogisticsTileEntiy");
		ModLoader.registerTileEntity(LogisticsSignTileEntity.class, "logisticspipes.blocks.LogisticsSignTileEntity");
		ModLoader.registerTileEntity(LogisticsSolderingTileEntity.class, "logisticspipes.blocks.LogisticsSolderingTileEntity");
	}

	@Override
	public World getWorld(int _dimension) {
		return DimensionManager.getWorld(_dimension);
	}
}
