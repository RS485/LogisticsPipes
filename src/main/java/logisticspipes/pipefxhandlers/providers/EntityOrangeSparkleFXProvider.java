package logisticspipes.pipefxhandlers.providers;

import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.particle.Particle;

import logisticspipes.pipefxhandlers.GenericSparkleFactory;
import logisticspipes.pipefxhandlers.ParticleProvider;

public class EntityOrangeSparkleFXProvider implements ParticleProvider {

	float red = 0.97F;
	float green = 0.46F;
	float blue = 0.19F;

	@Override
	public Particle createGenericParticle(WorldClient world, double x, double y, double z, int amount) {

		return GenericSparkleFactory.getSparkleInstance(world, x, y, z, red, green, blue, amount);

	}

}
