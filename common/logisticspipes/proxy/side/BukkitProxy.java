package logisticspipes.proxy.side;

import buildcraft.transport.TileGenericPipe;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

public class BukkitProxy extends ServerProxy {

	@Override
	public int getDimensionForWorld(World world) {
		for(Integer dim: DimensionManager.getIDs()) {
			World worldByDim = DimensionManager.getWorld(dim);
			if(world == worldByDim) {
				return dim;
			}
		}
		return super.getDimensionForWorld(world);
	}

	@Override
	public String getSide() {
		return "Bukkit";
	}

	@Override
	public TileGenericPipe getPipeInDimensionAt(int dimension, int x, int y, int z, EntityPlayer player) {
		TileGenericPipe pipe = getPipe(DimensionManager.getWorld(dimension), x, y, z);
		if(pipe != null) return pipe;
		pipe = getPipe(player.worldObj, x, y, z);
		if(pipe != null) return pipe;
		for(Integer dim:DimensionManager.getIDs()) {
			World world = DimensionManager.getWorld(dim);
			if(world.getWorldInfo().getDimension() != dimension) continue;
			pipe = getPipe(player.worldObj, x, y, z);
			if(pipe != null) return pipe;
		}
		return null;
	}
}
