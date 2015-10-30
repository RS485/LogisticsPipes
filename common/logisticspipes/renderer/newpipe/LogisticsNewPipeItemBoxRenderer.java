package logisticspipes.renderer.newpipe;

import java.util.HashMap;
import java.util.Map;

import logisticspipes.items.LogisticsFluidContainer;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.renderer.CustomBlockRenderer;
import logisticspipes.renderer.CustomBlockRenderer.RenderInfo;
import logisticspipes.renderer.FluidContainerRenderer;
import logisticspipes.utils.FluidIdentifier;
import logisticspipes.utils.item.ItemIdentifierStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.fluids.FluidStack;

import org.lwjgl.opengl.GL11;

public class LogisticsNewPipeItemBoxRenderer {

	private static final int RENDER_SIZE = 40;

	private int renderList = -1;
	private static final ResourceLocation BLOCKS = new ResourceLocation("textures/atlas/blocks.png");
	private static final Map<FluidIdentifier, int[]> renderLists = new HashMap<FluidIdentifier, int[]>();

	public void doRenderItem(ItemStack itemstack, float light, double x, double y, double z, double boxScale) {
		if(LogisticsNewRenderPipe.innerTransportBox == null) return;
		GL11.glPushMatrix();

		if (renderList == -1) {
			renderList = GLAllocation.generateDisplayLists(1);
			GL11.glNewList(renderList, GL11.GL_COMPILE);
			Tessellator tess = Tessellator.instance;
			tess.startDrawingQuads();
			LogisticsNewRenderPipe.innerTransportBox.render(LogisticsNewRenderPipe.innerBoxTexture);
			tess.draw();
			GL11.glEndList();
		}

		GL11.glTranslated(x, y, z);
		Minecraft.getMinecraft().getTextureManager().bindTexture(LogisticsNewPipeItemBoxRenderer.BLOCKS);
		GL11.glScaled(boxScale, boxScale, boxScale);
		GL11.glTranslated(-0.5, -0.5, -0.5);
		GL11.glCallList(renderList);
		GL11.glTranslated(0.5, 0.5, 0.5);
		GL11.glScaled(1 / boxScale, 1 / boxScale, 1 / boxScale);
		GL11.glTranslated(-0.5, -0.5, -0.5);

		if (itemstack != null && itemstack.getItem() instanceof LogisticsFluidContainer) {
			FluidStack f = SimpleServiceLocator.logisticsFluidManager.getFluidFromContainer(ItemIdentifierStack.getFromStack(itemstack));
			if (f != null) {
				FluidContainerRenderer.skipNext = true;
				int list = getRenderListFor(f);
				GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
				GL11.glEnable(GL11.GL_CULL_FACE);
				GL11.glDisable(GL11.GL_LIGHTING);
				GL11.glEnable(GL11.GL_BLEND);
				GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

				GL11.glCallList(list);
				GL11.glPopAttrib();
			}
		}

		GL11.glPopMatrix();
	}

	private int getRenderListFor(FluidStack fluid) {
		FluidIdentifier ident = FluidIdentifier.get(fluid);
		int[] array = LogisticsNewPipeItemBoxRenderer.renderLists.get(fluid);
		if (array == null) {
			array = new int[LogisticsNewPipeItemBoxRenderer.RENDER_SIZE];
			LogisticsNewPipeItemBoxRenderer.renderLists.put(ident, array);
		}
		int pos = Math.min((int) (((Math.min(fluid.amount, 5000) * 1.0F) * LogisticsNewPipeItemBoxRenderer.RENDER_SIZE) / 5000), LogisticsNewPipeItemBoxRenderer.RENDER_SIZE - 1);
		if (array[pos] != 0) {
			return array[pos];
		}
		RenderInfo block = new RenderInfo();

		block.baseBlock = fluid.getFluid().getBlock();
		block.texture = fluid.getFluid().getStillIcon();

		float ratio = pos * 1.0F / (LogisticsNewPipeItemBoxRenderer.RENDER_SIZE - 1);

		// CENTER HORIZONTAL

		array[pos] = GLAllocation.generateDisplayLists(1);
		GL11.glNewList(array[pos], 4864 /* GL_COMPILE */);

		block.minX = 0.32;
		block.maxX = 0.68;

		block.minY = 0.32;
		block.maxY = 0.32 + (0.68 - 0.32) * ratio;

		block.minZ = 0.32;
		block.maxZ = 0.68;

		CustomBlockRenderer.INSTANCE.renderBlock(block, Minecraft.getMinecraft().theWorld, 0, 0, 0, false, true);

		GL11.glEndList();
		return array[pos];
	}
}
