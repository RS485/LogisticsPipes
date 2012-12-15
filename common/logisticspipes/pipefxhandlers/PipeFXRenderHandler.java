package logisticspipes.pipefxhandlers;

import java.util.HashMap;

import buildcraft.core.DefaultProps;

import logisticspipes.LogisticsPipes;
import logisticspipes.network.NetworkConstants;
import logisticspipes.network.packets.PacketRenderFX;
import logisticspipes.proxy.MainProxy;

import net.minecraft.client.Minecraft;
import net.minecraft.src.Block;
import net.minecraft.src.EntityAuraFX;
import net.minecraft.src.EntityBreakingFX;
import net.minecraft.src.EntityBubbleFX;
import net.minecraft.src.EntityCloudFX;
import net.minecraft.src.EntityCritFX;
import net.minecraft.src.EntityDiggingFX;
import net.minecraft.src.EntityDropParticleFX;
import net.minecraft.src.EntityEnchantmentTableParticleFX;
import net.minecraft.src.EntityExplodeFX;
import net.minecraft.src.EntityFX;
import net.minecraft.src.EntityFlameFX;
import net.minecraft.src.EntityFootStepFX;
import net.minecraft.src.EntityHeartFX;
import net.minecraft.src.EntityHugeExplodeFX;
import net.minecraft.src.EntityLargeExplodeFX;
import net.minecraft.src.EntityLavaFX;
import net.minecraft.src.EntityNoteFX;
import net.minecraft.src.EntityPortalFX;
import net.minecraft.src.EntityReddustFX;
import net.minecraft.src.EntitySmokeFX;
import net.minecraft.src.EntitySnowShovelFX;
import net.minecraft.src.EntitySpellParticleFX;
import net.minecraft.src.EntitySplashFX;
import net.minecraft.src.EntitySuspendFX;
import net.minecraft.src.Item;
import net.minecraft.src.Material;
import net.minecraft.src.Packet;
import net.minecraft.src.WorldClient;

public class PipeFXRenderHandler {
	
	public static HashMap<String, ParticleProvider> particlemap = new HashMap<String, ParticleProvider> ();
	
	public static void spawnGenericParticle(String particle, double x, double y, double z, int amount) {
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
		//Send packets
		if (MainProxy.isServer()) {
			Packet packet = new PacketRenderFX(NetworkConstants.PARTICLE_FX_RENDER_DATA, (int) x, (int) y, (int) z, particle, amount).getPacket();
			MainProxy.sendPacketToAllAround(x, y, z, DefaultProps.NETWORK_UPDATE_RANGE, MainProxy.getDimensionForWorld(mc.theWorld), packet);
			
		}
		
		
		for (int i = 0; i < amount; i++) {
			effect = provider.createGenericParticle(mc.theWorld, x, y, z);
			if (effect != null) {
				mc.effectRenderer.addEffect((EntityFX) effect, effectObject);
			}
		}
		
		} catch (NullPointerException e) {}
	}
}
