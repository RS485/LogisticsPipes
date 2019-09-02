package logisticspipes.utils;

import net.minecraft.world.World;

import logisticspipes.interfaces.IWorldProvider;

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
