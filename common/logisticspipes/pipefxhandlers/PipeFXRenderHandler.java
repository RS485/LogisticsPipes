package logisticspipes.pipefxhandlers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;

import logisticspipes.proxy.MainProxy;

public class PipeFXRenderHandler {

	private static ParticleProvider[] particlemap = new ParticleProvider[Particles.values().length];

	public static void spawnGenericParticle(Particles particle, double x, double y, double z, int amount) {
		if (MainProxy.getClientMainWorld() == null) {
			return;
		}
		try {
			Minecraft mc = Minecraft.getMinecraft();
			int var14 = mc.gameSettings.particleSetting;
			double var15 = mc.getRenderViewEntity().posX - x;
			double var17 = mc.getRenderViewEntity().posY - y;
			double var19 = mc.getRenderViewEntity().posZ - z;
			Particle effect;

			double var22 = 16.0D;

			if (var15 * var15 + var17 * var17 + var19 * var19 > var22 * var22) {
				return;
			} else if (var14 > 1) {
				return;
			}

			ParticleProvider provider = PipeFXRenderHandler.particlemap[particle.ordinal()];
			if (provider == null) {
				return;
			}

			for (int i = 0; i < Math.sqrt(amount); i++) {
				effect = provider.createGenericParticle(mc.world, x, y, z, amount);
				if (effect != null) {
					mc.effectRenderer.addEffect(effect);
				}
			}

		} catch (NullPointerException ignored) {}
	}

	public static void registerParticleHandler(Particles particle, ParticleProvider provider) {
		if (PipeFXRenderHandler.particlemap[particle.ordinal()] == null) {
			PipeFXRenderHandler.particlemap[particle.ordinal()] = provider;
		}
	}
}
