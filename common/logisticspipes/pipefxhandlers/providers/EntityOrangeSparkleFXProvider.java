package logisticspipes.pipefxhandlers.providers;

import logisticspipes.pipefxhandlers.EntitySparkleFX;
import logisticspipes.pipefxhandlers.GenericSparkleFactory;
import logisticspipes.pipefxhandlers.ParticleProvider;

import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.particle.EntityFX;

public class EntityOrangeSparkleFXProvider implements ParticleProvider {

	float red = 0.97F;
	float green = 0.46F;
	float blue = 0.19F;

	@Override
	public EntityFX createGenericParticle(WorldClient world, double x, double y, double z, int amount) {
		EntitySparkleFX effect = GenericSparkleFactory.getSparkleInstance(world, x, y, z, red, green, blue, amount);

		return effect;

	}

}
