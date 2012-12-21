package logisticspipes.pipefxhandlers;

import net.minecraft.world.World;

public class GenericSparkleFactory {
	
	public static EntitySparkleFX getSparkleInstance(World world, double x, double y, double z) {

		EntitySparkleFX effect = null;

	    float boundry = 0.4F;
	    int pipeWidth = 3;

		float width = boundry + world.rand.nextInt(pipeWidth) / 10.0F;
	    float length = boundry + world.rand.nextInt(pipeWidth) / 10.0F;
	    float height = world.rand.nextInt(7) / 10.0F + 0.2F;

	    effect = new EntitySparkleFX(world, x + length, y + height, z + width, 1F, 1, 10 + world.rand.nextInt(5));
	    
		
		effect.setGravity(0.07F);
		
		return effect;
	}

}
