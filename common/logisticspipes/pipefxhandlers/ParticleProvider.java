package logisticspipes.pipefxhandlers;

import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.particle.EntityFX;

public interface ParticleProvider {

	float red = 1;
	float green = 1;
	float blue = 1;

	public abstract EntityFX createGenericParticle(WorldClient world, double x, double y, double z, int amount);

}
