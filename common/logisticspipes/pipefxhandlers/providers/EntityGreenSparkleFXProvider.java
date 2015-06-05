package logisticspipes.pipefxhandlers.providers;

import logisticspipes.pipefxhandlers.EntitySparkleFX;
import logisticspipes.pipefxhandlers.GenericSparkleFactory;
import logisticspipes.pipefxhandlers.ParticleProvider;

import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.particle.EntityFX;

public class EntityGreenSparkleFXProvider implements ParticleProvider {

	float red = 0F;
	float green = 1F;
	float blue = 0F;

	@Override
	public EntityFX createGenericParticle(WorldClient world, double x, double y, double z, int amount) {
		EntitySparkleFX effect = GenericSparkleFactory.getSparkleInstance(world, x, y, z, red, green, blue, amount);

		return effect;

	}

}
