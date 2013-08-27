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
    public boolean tinkle;
    public int blendmode;

    public EntitySparkleFX(World var1, double var2, double var4, double var6, float var8, float var9, float var10, float var11, int var12)
    {
        super(var1, var2, var4, var6, 0.0D, 0.0D, 0.0D);
        this.shrink = false;
        this.particle = 0;
        this.tinkle = false;
        this.blendmode = 1;

        if (var9 == 0.0F)
        {
            var9 = 1.0F;
        }

        this.particleRed = var9;
        this.particleGreen = var10;
        this.particleBlue = var11;
        this.particleGravity = 0.0F;
        this.motionX = this.motionY = this.motionZ = 0.0D;
        this.particleScale *= var8;
        this.particleMaxAge = 2 * var12 - 1;
        this.multiplier = var12;
        this.noClip = true;
    }

    public EntitySparkleFX(World var1, double var2, double var4, double var6, float var8, int var9, int var10)
    {
        this(var1, var2, var4, var6, var8, 0.0F, 0.0F, 0.0F, var10);

        switch (var9)
        {
            case 0:
                this.particleRed = 0.75F + var1.rand.nextFloat() * 0.25F;
                this.particleGreen = 0.25F + var1.rand.nextFloat() * 0.25F;
                this.particleBlue = 0.75F + var1.rand.nextFloat() * 0.25F;
                break;

            case 1:
                this.particleRed = 0.5F + var1.rand.nextFloat() * 0.3F;
                this.particleGreen = 0.5F + var1.rand.nextFloat() * 0.3F;
                this.particleBlue = 0.2F;
                break;

            case 2:
                this.particleRed = 0.2F;
                this.particleGreen = 0.2F;
                this.particleBlue = 0.7F + var1.rand.nextFloat() * 0.3F;
                break;

            case 3:
                this.particleRed = 0.2F;
                this.particleGreen = 0.7F + var1.rand.nextFloat() * 0.3F;
                this.particleBlue = 0.2F;
                break;

            case 4:
                this.particleRed = 0.7F + var1.rand.nextFloat() * 0.3F;
                this.particleGreen = 0.2F;
                this.particleBlue = 0.2F;
                break;

            case 5:
                this.blendmode = 771;
                this.particleRed = var1.rand.nextFloat() * 0.1F;
                this.particleGreen = var1.rand.nextFloat() * 0.1F;
                this.particleBlue = var1.rand.nextFloat() * 0.1F;
                break;

            case 6:
                this.particleRed = 0.8F + var1.rand.nextFloat() * 0.2F;
                this.particleGreen = 0.8F + var1.rand.nextFloat() * 0.2F;
                this.particleBlue = 0.8F + var1.rand.nextFloat() * 0.2F;
                break;

            case 7:
                this.particleRed = 0.2F;
                this.particleGreen = 0.5F + var1.rand.nextFloat() * 0.3F;
                this.particleBlue = 0.6F + var1.rand.nextFloat() * 0.3F;
        }
    }

    public EntitySparkleFX(World var1, double var2, double var4, double var6, double var8, double var10, double var12, float var14, int var15, int var16)
    {
        this(var1, var2, var4, var6, var14, var15, var16);
        double var17 = var8 - this.posX;
        double var19 = var10 - this.posY;
        double var21 = var12 - this.posZ;
        this.motionX = var17 / this.particleMaxAge;
        this.motionY = var19 / this.particleMaxAge;
        this.motionZ = var21 / this.particleMaxAge;
    }
	
	private static final ResourceLocation TEXTURE = new ResourceLocation("logisticspipes", "textures/particles/particles.png");
	

    @Override
	public void renderParticle(Tessellator var1, float var2, float var3, float var4, float var5, float var6, float var7)
    {
        var1.draw();
        GL11.glPushMatrix();
        GL11.glDepthMask(false);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, this.blendmode);
        Minecraft.getMinecraft().renderEngine.func_110577_a(TEXTURE);
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
        Minecraft.getMinecraft().renderEngine.func_110577_a(TEXTURE);//TODO: check -- was "/particles.png");, now LP-particles
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
            byte var2 = 50;

            if (!Minecraft.getMinecraft().gameSettings.fancyGraphics)
            {
                var2 = 25;
            }

            if (var1.getDistance(this.posX, this.posY, this.posZ) > var2)
            {
                this.setDead();
            }

            this.prevPosX = this.posX;
            this.prevPosY = this.posY;
            this.prevPosZ = this.posZ;

            if (this.particleAge == 0 && this.tinkle && this.worldObj.rand.nextInt(10) == 0)
            {
                this.worldObj.playSoundAtEntity(this, "random.orb", 0.02F, 0.7F * ((this.worldObj.rand.nextFloat() - this.worldObj.rand.nextFloat()) * 0.6F + 2.0F));
            }

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

    public void setGravity(float var1)
    {
        this.particleGravity = var1;
    }

    public void setParticle(int var1)
    {
        this.particle = var1;
    }
    
    public void setRed(float red) {
    	this.particleRed = red;
    }
    
    public void setGreen(float green) {
    	this.particleGreen = green;
    }
    
    public void setBlue(float blue) {
    	this.particleBlue = blue;
    }
    
}
