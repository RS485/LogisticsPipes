package logisticspipes.utils;

import logisticspipes.interfaces.IWorldProvider;

import net.minecraft.world.World;

public class DummyWorldProvider implements IWorldProvider {

	private final World world;

	public DummyWorldProvider(World world) {
		this.world = world;
	}

	@Override
	public World getWorld() {
		return world;
	}
}
