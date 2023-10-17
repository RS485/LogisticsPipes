package logisticspipes.pipefxhandlers.providers;

import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.particle.Particle;

import logisticspipes.pipefxhandlers.GenericSparkleFactory;
import logisticspipes.pipefxhandlers.ParticleProvider;

public class EntityLightGreenSparkleFXProvider implements ParticleProvider {

	float red = 0.8125F;
	float green = 0.9411764705882353F;
	float blue = 0.3098039215686275F;

	@Override
	public Particle createGenericParticle(WorldClient world, double x, double y, double z, int amount) {

		return GenericSparkleFactory.getSparkleInstance(world, x, y, z, red, green, blue, amount);

	}

}
