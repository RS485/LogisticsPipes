package logisticspipes.pipefxhandlers;

import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.proxy.object3d.interfaces.I3DOperation;
import logisticspipes.proxy.object3d.interfaces.IModel3D;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import org.lwjgl.opengl.GL11;

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
	public void renderParticle(Tessellator tess, float p_70539_2_, float p_70539_3_, float p_70539_4_, float p_70539_5_, float p_70539_6_, float p_70539_7_) {
		tess.draw();
		GL11.glPushMatrix();
		double x = posX - Particle.interpPosX;
		double y = posY - Particle.interpPosY;
		double z = posZ - Particle.interpPosZ;
		GL11.glTranslated(x, y, z);

		SimpleServiceLocator.cclProxy.getRenderState().reset();
		SimpleServiceLocator.cclProxy.getRenderState().setUseNormals(true);
		SimpleServiceLocator.cclProxy.getRenderState().setAlphaOverride(0xff);

		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		SimpleServiceLocator.cclProxy.getRenderState().startDrawing(mode, format);
		model.render(operations);
		SimpleServiceLocator.cclProxy.getRenderState().draw();

		GL11.glPopMatrix();
		tess.startDrawingQuads();
	}

}
