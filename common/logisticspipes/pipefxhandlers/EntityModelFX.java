package logisticspipes.pipefxhandlers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import org.lwjgl.opengl.GL11;

import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.proxy.object3d.interfaces.I3DOperation;
import logisticspipes.proxy.object3d.interfaces.IModel3D;

public class EntityModelFX extends Particle {

	private final IModel3D model;
	private final I3DOperation[] operations;
	private final ResourceLocation texture;

	public EntityModelFX(World world, double x, double y, double z, IModel3D model, I3DOperation[] i3dOperations, ResourceLocation texture) {
		super(world, x, y, z, 0, -5, 0);
		this.model = model;
		this.operations = i3dOperations;
		this.texture = texture;
	}

	@Override
	public void renderParticle(BufferBuilder worldRendererIn, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
		Tessellator.getInstance().draw();
		GL11.glPushMatrix();
		double x = posX - Particle.interpPosX;
		double y = posY - Particle.interpPosY;
		double z = posZ - Particle.interpPosZ;
		GL11.glTranslated(x, y, z);

		SimpleServiceLocator.cclProxy.getRenderState().reset();
		SimpleServiceLocator.cclProxy.getRenderState().setAlphaOverride(0xff);

		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		SimpleServiceLocator.cclProxy.getRenderState().startDrawing(GL11.GL_QUADS, DefaultVertexFormats.OLDMODEL_POSITION_TEX_NORMAL);
		model.render(operations);
		SimpleServiceLocator.cclProxy.getRenderState().draw();

		GL11.glPopMatrix();
		// Set texture back to particles
		Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation("textures/particle/particles.png"));
		BufferBuilder buffer = Tessellator.getInstance().getBuffer();
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
	}

}
