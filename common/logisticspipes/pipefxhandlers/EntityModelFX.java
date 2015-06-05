package logisticspipes.pipefxhandlers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import codechicken.lib.render.CCModel;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.CCRenderState.IVertexOperation;
import org.lwjgl.opengl.GL11;

public class EntityModelFX extends EntityFX {

	private final CCModel model;
	private final IVertexOperation[] operations;
	private final ResourceLocation texture;

	public EntityModelFX(World world, double x, double y, double z, CCModel model, IVertexOperation[] operations, ResourceLocation texture) {
		super(world, x, y, z, 0, -5, 0);
		this.model = model;
		this.operations = operations;
		this.texture = texture;
	}

	@Override
	public void renderParticle(Tessellator tess, float p_70539_2_, float p_70539_3_, float p_70539_4_, float p_70539_5_, float p_70539_6_, float p_70539_7_) {
		tess.draw();
		GL11.glPushMatrix();
		double x = posX - EntityFX.interpPosX;
		double y = posY - EntityFX.interpPosY;
		double z = posZ - EntityFX.interpPosZ;
		GL11.glTranslated(x, y, z);

		CCRenderState.reset();
		CCRenderState.useNormals = true;
		CCRenderState.alphaOverride = 0xff;

		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		CCRenderState.startDrawing();
		model.render(operations);
		CCRenderState.draw();

		GL11.glPopMatrix();
		tess.startDrawingQuads();
	}

}
