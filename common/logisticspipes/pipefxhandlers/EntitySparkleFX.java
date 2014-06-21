package logisticspipes.pipefxhandlers;

import java.util.Random;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import org.lwjgl.opengl.GL11;

public class EntitySparkleFX extends EntityFX
{
    public int multiplier;
    public boolean shrink;
    public int particle;
    public int blendmode;

    public EntitySparkleFX(World world, double x, double y, double z, float scalemult, float red, float green, float blue, int var12)
    {
        super(world, x, y, z, 0.0D, 0.0D, 0.0D);
        this.shrink = false;
        this.particle = 0;
        this.blendmode = 1;

        this.particleRed = red;
        this.particleGreen = green;
        this.particleBlue = blue;
        this.particleGravity = 0.07F;
        this.motionX = this.motionY = this.motionZ = 0.0D;
        this.particleScale *= scalemult;
        this.particleMaxAge = 3 * var12 - 1;
        this.multiplier = var12;
        this.noClip = true;
    }

	private static final ResourceLocation TEXTURE = new ResourceLocation("logisticspipes", "textures/particles/particles.png");
	private static final ResourceLocation field_110737_b = new ResourceLocation("textures/particle/particles.png");

    @Override
	public void renderParticle(Tessellator var1, float var2, float var3, float var4, float var5, float var6, float var7)
    {
        var1.draw();
        GL11.glPushMatrix();
        GL11.glDepthMask(false);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, this.blendmode);
        Minecraft.getMinecraft().renderEngine.bindTexture(TEXTURE);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.75F);
        int var8 = this.particle + this.particleAge / this.multiplier;
        float var9 = var8 % 8 / 8.0F;
        float var10 = var9 + 0.124875F;
        float var11 = var8 / 8 / 8.0F;
        float var12 = var11 + 0.124875F;
        float var13 = 0.1F * this.particleScale * ((float)(this.particleMaxAge - this.particleAge + 1) / (float)this.particleMaxAge);
        float var14 = (float)(this.prevPosX + (this.posX - this.prevPosX) * var2 - interpPosX);
        float var15 = (float)(this.prevPosY + (this.posY - this.prevPosY) * var2 - interpPosY);
        float var16 = (float)(this.prevPosZ + (this.posZ - this.prevPosZ) * var2 - interpPosZ);
        float var17 = 1.0F;
        var1.startDrawingQuads();
        var1.setBrightness(240);
        var1.setColorRGBA_F(this.particleRed * var17, this.particleGreen * var17, this.particleBlue * var17, 1.0F);
        var1.addVertexWithUV(var14 - var3 * var13 - var6 * var13, var15 - var4 * var13, var16 - var5 * var13 - var7 * var13, var10, var12);
        var1.addVertexWithUV(var14 - var3 * var13 + var6 * var13, var15 + var4 * var13, var16 - var5 * var13 + var7 * var13, var10, var11);
        var1.addVertexWithUV(var14 + var3 * var13 + var6 * var13, var15 + var4 * var13, var16 + var5 * var13 + var7 * var13, var9, var11);
        var1.addVertexWithUV(var14 + var3 * var13 - var6 * var13, var15 - var4 * var13, var16 + var5 * var13 - var7 * var13, var9, var12);
        var1.draw();
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glDepthMask(true);
        GL11.glPopMatrix();
        Minecraft.getMinecraft().renderEngine.bindTexture(field_110737_b);
        var1.startDrawingQuads();
    }

    /**
     * Called to update the entity's position/logic.
     */
    @Override
	public void onUpdate()
    {
        try
        {
            EntityClientPlayerMP var1 = Minecraft.getMinecraft().thePlayer;

            if (var1.getDistance(this.posX, this.posY, this.posZ) > 50)
            {
                this.setDead();
            }

            this.prevPosX = this.posX;
            this.prevPosY = this.posY;
            this.prevPosZ = this.posZ;

            if (this.particleAge++ >= this.particleMaxAge)
            {
                this.setDead();
            }

            this.motionX -=  0.05D * this.particleGravity - 0.1D * this.particleGravity * new Random().nextDouble();
            this.motionY -=  0.05D * this.particleGravity - 0.1D * this.particleGravity * new Random().nextDouble();
            this.motionZ -=  0.05D * this.particleGravity - 0.1D * this.particleGravity * new Random().nextDouble();
            
            this.moveEntity(this.motionX, this.motionY, this.motionZ);
            this.motionX *= 0.9800000190734863D;
            this.motionY *= 0.9800000190734863D;
            this.motionZ *= 0.9800000190734863D;

            if (this.onGround)
            {
                this.motionX *= 0.699999988079071D;
                this.motionZ *= 0.699999988079071D;
            }
        }
        catch (Exception var3)
        {
            ;
        }
    }
}
