package logisticspipes.interfaces;

import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public interface IHeadUpDisplayRendererProvider {

	public IHeadUpDisplayRenderer getRenderer();

	public BlockPos getblockpos();

	public World getWorld();

	public void startWatching();

	public void stopWatching();
}
