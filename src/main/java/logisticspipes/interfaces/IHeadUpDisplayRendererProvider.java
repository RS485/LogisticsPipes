package logisticspipes.interfaces;

import net.minecraft.world.World;

public interface IHeadUpDisplayRendererProvider {

	IHeadUpDisplayRenderer getRenderer();

	int getX();

	int getY();

	int getZ();

	World getWorldForHUD();

	void startWatching();

	void stopWatching();
}
