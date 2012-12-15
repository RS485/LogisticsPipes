package logisticspipes.pipefxhandlers.providers;

import logisticspipes.pipefxhandlers.EntitySparkleFX;
import logisticspipes.pipefxhandlers.GenericSparkleFactory;
import logisticspipes.pipefxhandlers.ParticleProvider;
import net.minecraft.src.EntityFX;
import net.minecraft.src.WorldClient;

public class EntityRedSparkleFXProvider implements ParticleProvider {

	float red = 1F;
	float green = 0.03F;
	float blue = 0.02F;
	
	@Override
	public EntityFX createGenericParticle(WorldClient world, double x, double y, double z) {
	
		EntitySparkleFX effect = GenericSparkleFactory.getSparkleInstance(world, x, y, z);
		
		effect.setRed(red);
		effect.setGreen(green);
		effect.setBlue(blue);
		
		return effect;

		
	}

	
	
}
