package logisticspipes.pipefxhandlers.providers;

import logisticspipes.pipefxhandlers.EntitySparkleFX;
import logisticspipes.pipefxhandlers.GenericSparkleFactory;
import logisticspipes.pipefxhandlers.ParticleProvider;

import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.particle.EntityFX;

public class EntityWhiteSparkleFXProvider implements ParticleProvider {

	@Override
	public EntityFX createGenericParticle(WorldClient world, double x, double y, double z, int amount) {
		EntitySparkleFX effect = GenericSparkleFactory.getSparkleInstance(world, x, y, z, ParticleProvider.red, ParticleProvider.green, ParticleProvider.blue, amount);

		return effect;
	}

}
