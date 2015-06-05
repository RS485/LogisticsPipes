package logisticspipes.routing.debug;

import logisticspipes.interfaces.IHeadUpDisplayRenderer;
import logisticspipes.interfaces.IHeadUpDisplayRendererProvider;
import logisticspipes.utils.tuples.LPPosition;

import net.minecraft.world.World;

public class HUDRoutingTableDebugProvider implements IHeadUpDisplayRendererProvider {

	private final IHeadUpDisplayRenderer hud;
	private final LPPosition pos;

	HUDRoutingTableDebugProvider(IHeadUpDisplayRenderer hud, LPPosition pos) {
		this.hud = hud;
		this.pos = pos;
	}

	@Override
	public IHeadUpDisplayRenderer getRenderer() {
		return hud;
	}

	@Override
	public int getX() {
		return pos.getX();
	}

	@Override
	public int getY() {
		return pos.getY();
	}

	@Override
	public int getZ() {
		return pos.getZ();
	}

	@Override
	public World getWorld() {
		return null;
	}

	@Override
	public void startWatching() {}

	@Override
	public void stopWatching() {}
}
