package logisticspipes.pipefxhandlers;

import java.util.Random;

import logisticspipes.utils.tuples.LPPosition;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.client.FMLClientHandler;

@Accessors(chain = true)
public class PipeFXLaserPowerBeam extends EntityFX {
	private static final ResourceLocation	beam			= new ResourceLocation("logisticspipes", "textures/particles/laserBeam.png");
	private static final ResourceLocation	field_110737_b	= new ResourceLocation("textures/particle/particles.png");
	private static final int				ROTATIONSPEED	= 5;
	private static final Random				RAND			= new Random();
	
	@Setter
	private boolean							reverse			= false;
	private float							length			= 0.0F;
	private float							yaw				= 0.0F;
	private float							pitch			= 0.0F;
	private float							random			= 0;
	private TileEntity tile;

	public PipeFXLaserPowerBeam(World par1World, LPPosition pos, float length, ForgeDirection dir, int color, TileEntity tile) {
		super(par1World, pos.getXD() + 0.5D, pos.getYD() + 0.5D, pos.getZD() + 0.5D, 0.0D, 0.0D, 0.0D);
		setSize(0.02F, 0.02F);
		this.tile = tile;
		this.particleRed = ((float)((color & 0xff0000) >> 16)) / 0xff;
		this.particleGreen = ((float)((color & 0x00ff00) >> 8)) / 0xff;
		this.particleBlue = ((float)((color & 0x0000ff) >> 0)) / 0xff;
		this.noClip = true;
		this.motionX = 0.0D;
		this.motionY = 0.0D;
		this.motionZ = 0.0D;
		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;
		this.length = length;
		this.random = RAND.nextFloat() * RAND.nextInt(10);
		dir = dir.getOpposite();
		this.yaw = ((float)(Math.atan2(dir.offsetX, dir.offsetZ) * 180.0D / Math.PI));
		this.pitch = ((float)(Math.atan2(dir.offsetY, MathHelper.sqrt_double(dir.offsetX * dir.offsetX + dir.offsetZ * dir.offsetZ)) * 180.0D / Math.PI));
		this.particleMaxAge = 0;
		EntityLivingBase renderentity = FMLClientHandler.instance().getClient().renderViewEntity;
		int visibleDistance = 50;
		if(!FMLClientHandler.instance().getClient().gameSettings.fancyGraphics) {
			visibleDistance = 25;
		}
		if(renderentity.getDistance(this.posX, this.posY, this.posZ) > visibleDistance) {
			this.setDead();
		}
	}
	
	public void onUpdate() {
		if(tile.isInvalid()) {
			this.setDead();
		}
	}
	
	public void renderParticle(Tessellator tessellator, float f, float f1, float f2, float f3, float f4, float f5) {
		tessellator.draw();
		GL11.glPushMatrix();
		float slide = (float)this.worldObj.getTotalWorldTime() + random;
		float rot = (float)(this.worldObj.provider.getWorldTime() % (360 / ROTATIONSPEED) * ROTATIONSPEED) + ROTATIONSPEED * f;
		
		GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, 10497.0F);
		GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, 10497.0F);
		
		GL11.glDisable(GL11.GL_CULL_FACE);
		
		float partSlide = slide + f;
		if(this.reverse) {
			partSlide *= -1.0F;
		}
		float globalTextureSlide = -partSlide * 0.2F - MathHelper.floor_float(-partSlide * 0.1F);
		
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
		GL11.glDepthMask(false);
		
		double x = this.posX - EntityFX.interpPosX;
		double y = this.posY - EntityFX.interpPosY;
		double z = this.posZ - EntityFX.interpPosZ;
		GL11.glTranslated(x, y, z);
		
		GL11.glRotatef(90.0F, 1.0F, 0.0F, 0.0F);
		GL11.glRotatef(180.0F + this.yaw, 0.0F, 0.0F, -1.0F);
		GL11.glRotatef(this.pitch, 1.0F, 0.0F, 0.0F);
		
		GL11.glRotatef(rot, 0.0F, 1.0F, 0.0F);
		if(length != 0) {
			Minecraft.getMinecraft().renderEngine.bindTexture(beam);
			for(int t = 0; t < 3; t++) {
				double texturePos = -1.0F + globalTextureSlide + t / 3.0F;
				GL11.glRotatef(60.0F, 0.0F, 1.0F, 0.0F);
				tessellator.startDrawingQuads();
				tessellator.setBrightness(200);
				tessellator.setColorRGBA_F(this.particleRed, this.particleGreen, this.particleBlue, 0.5F);
				tessellator.addVertexWithUV(-0.07D, this.length, 0.0D, 1.0D, this.length + texturePos);
				tessellator.addVertexWithUV(-0.07D, 0.0D, 0.0D, 1.0D, texturePos);
				tessellator.addVertexWithUV(0.07D, 0.0D, 0.0D, 0.0D, texturePos);
				tessellator.addVertexWithUV(0.07D, this.length, 0.0D, 0.0D, this.length + texturePos);
				tessellator.draw();
			}
		}
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GL11.glDepthMask(true);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glPopMatrix();
		Minecraft.getMinecraft().renderEngine.bindTexture(field_110737_b);
		tessellator.startDrawingQuads();
	}
}
