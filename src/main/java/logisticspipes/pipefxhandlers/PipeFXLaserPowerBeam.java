package logisticspipes.pipefxhandlers;

import java.util.Random;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import net.minecraftforge.fml.client.FMLClientHandler;

import lombok.Setter;
import lombok.experimental.Accessors;
import org.lwjgl.opengl.GL11;

import network.rs485.logisticspipes.world.DoubleCoordinates;

@Accessors(chain = true)
public class PipeFXLaserPowerBeam extends Particle {

	private static final ResourceLocation beam = new ResourceLocation("logisticspipes", "textures/particles/laserBeam.png");
	private static final ResourceLocation field_110737_b = new ResourceLocation("textures/particle/particles.png");
	private static final int ROTATIONSPEED = 5;
	private static final Random RAND = new Random();

	@Setter
	private boolean reverse = false;
	private float length;
	private float yaw;
	private float pitch;
	private float random;
	private TileEntity tile;

	public PipeFXLaserPowerBeam(World par1World, DoubleCoordinates pos, float length, EnumFacing dir, int color, TileEntity tile) {
		super(par1World, pos.getXCoord() + 0.5D, pos.getYCoord() + 0.5D, pos.getZCoord() + 0.5D, 0.0D, 0.0D, 0.0D);
		setSize(0.02F, 0.02F);
		this.tile = tile;
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
		this.length = length;
		random = PipeFXLaserPowerBeam.RAND.nextFloat() * PipeFXLaserPowerBeam.RAND.nextInt(10);
		dir = dir.getOpposite();
		yaw = ((float) (Math.atan2(dir.getDirectionVec().getX(), dir.getDirectionVec().getZ()) * 180.0D / Math.PI));
		pitch = ((float) (Math.atan2(dir.getDirectionVec().getY(), MathHelper.sqrt(dir.getDirectionVec().getX() * dir.getDirectionVec().getX() + dir.getDirectionVec().getZ() * dir.getDirectionVec().getZ())) * 180.0D / Math.PI));
		particleMaxAge = 0;
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
		float slide = world.getTotalWorldTime() + random;
		float rot = world.provider.getWorldTime() % (360 / PipeFXLaserPowerBeam.ROTATIONSPEED) * PipeFXLaserPowerBeam.ROTATIONSPEED + PipeFXLaserPowerBeam.ROTATIONSPEED * partialTicks;

		GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, 10497.0F);
		GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, 10497.0F);

		GL11.glDisable(GL11.GL_CULL_FACE);

		float partSlide = slide + partialTicks;
		if (reverse) {
			partSlide *= -1.0F;
		}
		float globalTextureSlide = -partSlide * 0.2F - MathHelper.floor(-partSlide * 0.1F);

		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
		GL11.glDepthMask(false);

		double x = posX - Particle.interpPosX;
		double y = posY - Particle.interpPosY;
		double z = posZ - Particle.interpPosZ;
		GL11.glTranslated(x, y, z);

		GL11.glRotatef(90.0F, 1.0F, 0.0F, 0.0F);
		GL11.glRotatef(180.0F + yaw, 0.0F, 0.0F, -1.0F);
		GL11.glRotatef(pitch, 1.0F, 0.0F, 0.0F);

		GL11.glRotatef(rot, 0.0F, 1.0F, 0.0F);
		if (length != 0) {
			Minecraft.getMinecraft().renderEngine.bindTexture(PipeFXLaserPowerBeam.beam);
			for (int t = 0; t < 3; t++) {
				double texturePos = -1.0F + globalTextureSlide + t / 3.0F;
				GL11.glRotatef(60.0F, 0.0F, 1.0F, 0.0F);
				BufferBuilder buffer = Tessellator.getInstance().getBuffer();
				buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
				buffer.pos(-0.07D, length, 0.0D).tex(1.0D, length + texturePos).color(particleRed, particleGreen, particleBlue, 0.5F).endVertex();
				buffer.pos(-0.07D, 0.0D, 0.0D).tex(1.0D, texturePos).color(particleRed, particleGreen, particleBlue, 0.5F).endVertex();
				buffer.pos(0.07D, 0.0D, 0.0D).tex(0.0D, texturePos).color(particleRed, particleGreen, particleBlue, 0.5F).endVertex();
				buffer.pos(0.07D, length, 0.0D).tex(0.0D, length + texturePos).color(particleRed, particleGreen, particleBlue, 0.5F).endVertex();
				Tessellator.getInstance().draw();
			}
		}
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GL11.glDepthMask(true);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glPopMatrix();
		Minecraft.getMinecraft().renderEngine.bindTexture(PipeFXLaserPowerBeam.field_110737_b);
		BufferBuilder buffer = Tessellator.getInstance().getBuffer();
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
	}
}
