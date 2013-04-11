package logisticspipes.pipefxhandlers.providers;

import logisticspipes.pipefxhandlers.EntitySparkleFX;
import logisticspipes.pipefxhandlers.GenericSparkleFactory;
import logisticspipes.pipefxhandlers.ParticleProvider;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.particle.EntityFX;

public class EntityVioletSparkleFXProvider implements ParticleProvider {
	
	float red = 0.51F;
	float green = 0.04F;
	float blue = 0.73F;

	@Override
	public EntityFX createGenericParticle(WorldClient world, double x, double y, double z) {
		EntitySparkleFX effect = GenericSparkleFactory.getSparkleInstance(world, x, y, z);
		
		effect.setRed(red);
		effect.setGreen(green);
		effect.setBlue(blue);
		
		return effect;

	}

}
