package logisticspipes.pipefxhandlers;

import java.util.Random;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import net.minecraftforge.fml.client.FMLClientHandler;

import lombok.experimental.Accessors;
import org.lwjgl.opengl.GL11;

import network.rs485.logisticspipes.world.DoubleCoordinates;

@Accessors(chain = true)
public class PipeFXLaserPowerBall extends Particle {

	private static final ResourceLocation ball = new ResourceLocation("logisticspipes", "textures/particles/laserBall.png");
	private static final ResourceLocation field_110737_b = new ResourceLocation("textures/particle/particles.png");
	private static final int ROTATIONSPEED = 5;
	private static final Random RAND = new Random();

	private float random;
	private TileEntity tile;

	public PipeFXLaserPowerBall(World par1World, DoubleCoordinates pos, int color, TileEntity tile) {
		super(par1World, pos.getXCoord() + 0.5D, pos.getYCoord() + 0.5D, pos.getZCoord() + 0.5D, 0.0D, 0.0D, 0.0D);
		this.tile = tile;
		setSize(0.02F, 0.02F);
		particleRed = ((float) ((color & 0xff0000) >> 16)) / 0xff;
		particleGreen = ((float) ((color & 0x00ff00) >> 8)) / 0xff;
		particleBlue = ((float) ((color & 0x0000ff) >> 0)) / 0xff;
		canCollide = false;
		motionX = 0.0D;
		motionY = 0.0D;
		motionZ = 0.0D;
		prevPosX = posX;
		prevPosY = posY;
		prevPosZ = posZ;
		particleMaxAge = 0;
		random = PipeFXLaserPowerBall.RAND.nextFloat() * PipeFXLaserPowerBall.RAND.nextInt(360 / PipeFXLaserPowerBall.ROTATIONSPEED);
		Entity renderentity = FMLClientHandler.instance().getClient().getRenderViewEntity();
		int visibleDistance = 50;
		if (!FMLClientHandler.instance().getClient().gameSettings.fancyGraphics) {
			visibleDistance = 25;
		}
		if (renderentity.getDistance(posX, posY, posZ) > visibleDistance) {
			setExpired();
		}
	}

	@Override
	public void onUpdate() {
		if (tile.isInvalid()) {
			setExpired();
		}
	}

	@Override
	public void renderParticle(BufferBuilder worldRendererIn, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
		Tessellator.getInstance().draw();
		GL11.glPushMatrix();
		float rot = (world.provider.getWorldTime() + random) % (360 / PipeFXLaserPowerBall.ROTATIONSPEED) * PipeFXLaserPowerBall.ROTATIONSPEED + PipeFXLaserPowerBall.ROTATIONSPEED * partialTicks;

		GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, 10497.0F);
		GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, 10497.0F);
		GL11.glDisable(GL11.GL_CULL_FACE);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
		GL11.glDepthMask(false);

		double x = posX - Particle.interpPosX;
		double y = posY - Particle.interpPosY;
		double z = posZ - Particle.interpPosZ;
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
					BufferBuilder buffer = Tessellator.getInstance().getBuffer();
					buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
					double zoom = 0.25D;
					buffer.pos(zoom, zoom, 0.0D).tex(1.0D, 1.0D).color(particleRed, particleGreen, particleBlue, 0.8F).endVertex();
					buffer.pos(zoom, -zoom, 0.0D).tex(1.0D, 0.0D).color(particleRed, particleGreen, particleBlue, 0.8F).endVertex();
					buffer.pos(-zoom, -zoom, 0.0D).tex(0.0D, 0.0D).color(particleRed, particleGreen, particleBlue, 0.8F).endVertex();
					buffer.pos(-zoom, zoom, 0.0D).tex(0.0D, 1.0D).color(particleRed, particleGreen, particleBlue, 0.8F).endVertex();
					Tessellator.getInstance().draw();
				}
			}
		}
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GL11.glDepthMask(true);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glPopMatrix();
		Minecraft.getMinecraft().renderEngine.bindTexture(PipeFXLaserPowerBall.field_110737_b);
		BufferBuilder buffer = Tessellator.getInstance().getBuffer();
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
	}
}
