package logisticspipes.pipefxhandlers;

import java.util.Random;

import logisticspipes.utils.tuples.LPPosition;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import cpw.mods.fml.client.FMLClientHandler;

import lombok.experimental.Accessors;
import org.lwjgl.opengl.GL11;

@Accessors(chain = true)
public class PipeFXLaserPowerBall extends EntityFX {

	private static final ResourceLocation ball = new ResourceLocation("logisticspipes", "textures/particles/laserBall.png");
	private static final ResourceLocation field_110737_b = new ResourceLocation("textures/particle/particles.png");
	private static final int ROTATIONSPEED = 5;
	private static final Random RAND = new Random();

	private float random = 0;
	private TileEntity tile;

	public PipeFXLaserPowerBall(World par1World, LPPosition pos, int color, TileEntity tile) {
		super(par1World, pos.getXD() + 0.5D, pos.getYD() + 0.5D, pos.getZD() + 0.5D, 0.0D, 0.0D, 0.0D);
		this.tile = tile;
		setSize(0.02F, 0.02F);
		particleRed = ((float) ((color & 0xff0000) >> 16)) / 0xff;
		particleGreen = ((float) ((color & 0x00ff00) >> 8)) / 0xff;
		particleBlue = ((float) ((color & 0x0000ff) >> 0)) / 0xff;
		noClip = true;
		motionX = 0.0D;
		motionY = 0.0D;
		motionZ = 0.0D;
		prevPosX = posX;
		prevPosY = posY;
		prevPosZ = posZ;
		particleMaxAge = 0;
		random = PipeFXLaserPowerBall.RAND.nextFloat() * PipeFXLaserPowerBall.RAND.nextInt(360 / PipeFXLaserPowerBall.ROTATIONSPEED);
		EntityLivingBase renderentity = FMLClientHandler.instance().getClient().renderViewEntity;
		int visibleDistance = 50;
		if (!FMLClientHandler.instance().getClient().gameSettings.fancyGraphics) {
			visibleDistance = 25;
		}
		if (renderentity.getDistance(posX, posY, posZ) > visibleDistance) {
			setDead();
		}
	}

	@Override
	public void onUpdate() {
		if (tile.isInvalid()) {
			setDead();
		}
	}

	@Override
	public void renderParticle(Tessellator tessellator, float f, float f1, float f2, float f3, float f4, float f5) {
		tessellator.draw();
		GL11.glPushMatrix();
		float rot = (worldObj.provider.getWorldTime() + random) % (360 / PipeFXLaserPowerBall.ROTATIONSPEED) * PipeFXLaserPowerBall.ROTATIONSPEED + PipeFXLaserPowerBall.ROTATIONSPEED * f;

		GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, 10497.0F);
		GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, 10497.0F);
		GL11.glDisable(GL11.GL_CULL_FACE);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
		GL11.glDepthMask(false);

		double x = posX - EntityFX.interpPosX;
		double y = posY - EntityFX.interpPosY;
		double z = posZ - EntityFX.interpPosZ;
		GL11.glTranslated(x, y, z);

		GL11.glRotatef(rot, 0.0F, 1.0F, 0.0F);
		GL11.glRotatef(rot, 0.0F, 0.0F, 1.0F);
		int repeat = 3;
		float rotate = 60.0F;
		Minecraft.getMinecraft().renderEngine.bindTexture(PipeFXLaserPowerBall.ball);
		for (int t = 0; t < repeat; t++) {
			GL11.glRotatef(rotate, 0.0F, 0.0F, 1.0F);
			for (int u = 0; u < repeat; u++) {
				GL11.glRotatef(rotate, 1.0F, 0.0F, 0.0F);
				for (int v = 0; v < repeat; v++) {
					GL11.glRotatef(rotate, 0.0F, 1.0F, 0.0F);
					tessellator.startDrawingQuads();
					tessellator.setBrightness(50);
					tessellator.setColorRGBA_F(particleRed, particleGreen, particleBlue, 0.8F);
					double zoom = 0.25D;
					tessellator.addVertexWithUV(zoom, zoom, 0.0D, 1.0D, 1.0D);
					tessellator.addVertexWithUV(zoom, -zoom, 0.0D, 1.0D, 0.0D);
					tessellator.addVertexWithUV(-zoom, -zoom, 0.0D, 0.0D, 0.0D);
					tessellator.addVertexWithUV(-zoom, zoom, 0.0D, 0.0D, 1.0D);
					tessellator.draw();
				}
			}
		}
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GL11.glDepthMask(true);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glPopMatrix();
		Minecraft.getMinecraft().renderEngine.bindTexture(PipeFXLaserPowerBall.field_110737_b);
		tessellator.startDrawingQuads();
	}
}
