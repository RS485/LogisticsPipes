package logisticspipes.pipefxhandlers;

import net.minecraft.src.EntityFX;
import net.minecraft.src.WorldClient;

public class EntityGoldSparkleFXProvider implements ParticleProvider {

	float red = 0.93F;
	float green = 0.80F;
	float blue = 0.36F;

	@Override
	public EntityFX createGenericParticle(WorldClient world, double x, double y, double z) {
		EntitySparkleFX effect = GenericSparkleFactory.getSparkleInstance(world, x, y, z);
		
		effect.setRed(red);
		effect.setGreen(green);
		effect.setBlue(blue);
		
		return effect;

	}
}
