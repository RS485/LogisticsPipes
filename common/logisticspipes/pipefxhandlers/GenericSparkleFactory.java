package logisticspipes.pipefxhandlers;

import net.minecraft.world.World;

public class GenericSparkleFactory {

	public static EntitySparkleFX getSparkleInstance(World world, double x, double y, double z, float red, float green, float blue, int amount) {

		EntitySparkleFX effect;

		float boundry = 0.4F;
		int pipeWidth = 3;

		float width = boundry + world.rand.nextInt(pipeWidth) / 10.0F;
		float length = boundry + world.rand.nextInt(pipeWidth) / 10.0F;
		float height = world.rand.nextInt(7) / 10.0F + 0.2F;

		float scalemult = 1f + (float) Math.log10(amount);

		effect = new EntitySparkleFX(world, x + length, y + height, z + width, scalemult, red, green, blue, 6 + world.rand.nextInt(3));

		return effect;
	}

}
