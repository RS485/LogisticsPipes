package logisticspipes.pipefxhandlers;

import java.util.Random;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import org.lwjgl.opengl.GL11;

public class EntitySparkleFX extends EntityFX {

	public int multiplier;
	public boolean shrink;
	public int particle;
	public int blendmode;

	public EntitySparkleFX(World world, double x, double y, double z, float scalemult, float red, float green, float blue, int var12) {
		super(world, x, y, z, 0.0D, 0.0D, 0.0D);
		shrink = false;
		particle = 0;
		blendmode = 1;

		particleRed = red;
		particleGreen = green;
		particleBlue = blue;
		particleGravity = 0.07F;
		motionX = motionY = motionZ = 0.0D;
		particleScale *= scalemult;
		particleMaxAge = 3 * var12 - 1;
		multiplier = var12;
		noClip = true;
	}

	private static final ResourceLocation TEXTURE = new ResourceLocation("logisticspipes", "textures/particles/particles.png");
	private static final ResourceLocation field_110737_b = new ResourceLocation("textures/particle/particles.png");

	@Override
	public void renderParticle(Tessellator var1, float var2, float var3, float var4, float var5, float var6, float var7) {
		var1.draw();
		GL11.glPushMatrix();
		GL11.glDepthMask(false);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, blendmode);
		Minecraft.getMinecraft().renderEngine.bindTexture(EntitySparkleFX.TEXTURE);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.75F);
		int var8 = particle + particleAge / multiplier;
		float var9 = var8 % 8 / 8.0F;
		float var10 = var9 + 0.124875F;
		float var11 = var8 / 8 / 8.0F;
		float var12 = var11 + 0.124875F;
		float var13 = 0.1F * particleScale * ((float) (particleMaxAge - particleAge + 1) / (float) particleMaxAge);
		float var14 = (float) (prevPosX + (posX - prevPosX) * var2 - EntityFX.interpPosX);
		float var15 = (float) (prevPosY + (posY - prevPosY) * var2 - EntityFX.interpPosY);
		float var16 = (float) (prevPosZ + (posZ - prevPosZ) * var2 - EntityFX.interpPosZ);
		float var17 = 1.0F;
		var1.startDrawingQuads();
		var1.setBrightness(240);
		var1.setColorRGBA_F(particleRed * var17, particleGreen * var17, particleBlue * var17, 1.0F);
		var1.addVertexWithUV(var14 - var3 * var13 - var6 * var13, var15 - var4 * var13, var16 - var5 * var13 - var7 * var13, var10, var12);
		var1.addVertexWithUV(var14 - var3 * var13 + var6 * var13, var15 + var4 * var13, var16 - var5 * var13 + var7 * var13, var10, var11);
		var1.addVertexWithUV(var14 + var3 * var13 + var6 * var13, var15 + var4 * var13, var16 + var5 * var13 + var7 * var13, var9, var11);
		var1.addVertexWithUV(var14 + var3 * var13 - var6 * var13, var15 - var4 * var13, var16 + var5 * var13 - var7 * var13, var9, var12);
		var1.draw();
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glDepthMask(true);
		GL11.glPopMatrix();
		Minecraft.getMinecraft().renderEngine.bindTexture(EntitySparkleFX.field_110737_b);
		var1.startDrawingQuads();
	}

	/**
	 * Called to update the entity's position/logic.
	 */
	@Override
	public void onUpdate() {
		try {
			EntityClientPlayerMP var1 = Minecraft.getMinecraft().thePlayer;

			if (var1.getDistance(posX, posY, posZ) > 50) {
				setDead();
			}

			prevPosX = posX;
			prevPosY = posY;
			prevPosZ = posZ;

			if (particleAge++ >= particleMaxAge) {
				setDead();
			}

			motionX -= 0.05D * particleGravity - 0.1D * particleGravity * new Random().nextDouble();
			motionY -= 0.05D * particleGravity - 0.1D * particleGravity * new Random().nextDouble();
			motionZ -= 0.05D * particleGravity - 0.1D * particleGravity * new Random().nextDouble();

			moveEntity(motionX, motionY, motionZ);
			motionX *= 0.9800000190734863D;
			motionY *= 0.9800000190734863D;
			motionZ *= 0.9800000190734863D;

			if (onGround) {
				motionX *= 0.699999988079071D;
				motionZ *= 0.699999988079071D;
			}
		} catch (Exception var3) {
			;
		}
	}
}
