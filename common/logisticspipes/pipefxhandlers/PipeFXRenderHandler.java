package logisticspipes.pipefxhandlers;

import java.util.HashMap;

import logisticspipes.proxy.MainProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.src.EntityFX;

public class PipeFXRenderHandler {
	
	private static HashMap<Integer, ParticleProvider> particlemap = new HashMap<Integer, ParticleProvider> ();
	
	public static void spawnGenericParticle(int particle, double x, double y, double z, int amount) {
		if (MainProxy.getClientMainWorld() == null) return;
		try {
		Minecraft mc = Minecraft.getMinecraft();
		int var14 = mc.gameSettings.particleSetting;
		double var15 = mc.renderViewEntity.posX - x;
		double var17 = mc.renderViewEntity.posY - y;
		double var19 = mc.renderViewEntity.posZ - z;
		EntityFX effect = null;
		Object effectObject = null;
		double var22 = 16.0D;
		
		if (var15 * var15 + var17 * var17 + var19 * var19 > var22 * var22) {
			return;
		} else if (var14 > 1) {
			return;
		}

		ParticleProvider provider = particlemap.get(particle);
		if (provider == null) return;
		
		
		for (int i = 0; i < amount; i++) {
			effect = provider.createGenericParticle(mc.theWorld, x, y, z);
			if (effect != null) {
				mc.effectRenderer.addEffect((EntityFX) effect, effectObject);
			}
		}
		
		} catch (NullPointerException e) {}
	}
	
	public static void registerParticleHandler(int particle, ParticleProvider provider) {
		if(!particlemap.containsKey(particle)) {
			particlemap.put(particle, provider);
		}
	}
}
