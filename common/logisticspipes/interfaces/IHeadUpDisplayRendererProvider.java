package logisticspipes.interfaces;

import net.minecraft.world.World;

public interface IHeadUpDisplayRendererProvider {

	public IHeadUpDisplayRenderer getRenderer();

	public int getX();

	public int getY();

	public int getZ();

	public World getWorldForHUD();

	public void startWatching();

	public void stopWatching();
}
