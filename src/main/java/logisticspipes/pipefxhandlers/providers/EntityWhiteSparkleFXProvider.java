package logisticspipes.pipefxhandlers.providers;

import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.particle.Particle;

import logisticspipes.pipefxhandlers.GenericSparkleFactory;
import logisticspipes.pipefxhandlers.ParticleProvider;

public class EntityWhiteSparkleFXProvider implements ParticleProvider {

	@Override
	public Particle createGenericParticle(WorldClient world, double x, double y, double z, int amount) {

		return GenericSparkleFactory.getSparkleInstance(world, x, y, z, ParticleProvider.red, ParticleProvider.green, ParticleProvider.blue, amount);
	}

}
