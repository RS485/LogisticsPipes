package logisticspipes.pipefxhandlers.providers;

import logisticspipes.pipefxhandlers.EntitySparkleFX;
import logisticspipes.pipefxhandlers.GenericSparkleFactory;
import logisticspipes.pipefxhandlers.ParticleProvider;

import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.particle.EntityFX;

public class EntityLightGreenSparkleFXProvider implements ParticleProvider {

	float red = 0.8125F;
	float green = 0.9411764705882353F;
	float blue = 0.3098039215686275F;

	@Override
	public EntityFX createGenericParticle(WorldClient world, double x, double y, double z, int amount) {

		EntitySparkleFX effect = GenericSparkleFactory.getSparkleInstance(world, x, y, z, red, green, blue, amount);

		return effect;

	}

}
