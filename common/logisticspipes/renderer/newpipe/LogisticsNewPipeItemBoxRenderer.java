package logisticspipes.renderer.newpipe;

import java.util.HashMap;
import java.util.Map;

import logisticspipes.LPConstants;
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
	private static final ResourceLocation	BLOCKS	= new ResourceLocation("textures/atlas/blocks.png");
	private static final Map<FluidIdentifier, int[]> renderLists = new HashMap<FluidIdentifier, int[]>();
	
	public void doRenderItem(ItemStack itemstack, float light, double x, double y, double z) {
		if(renderList == -1) {
			renderList = GLAllocation.generateDisplayLists(1);
			GL11.glNewList(renderList, GL11.GL_COMPILE);
			Tessellator tess = Tessellator.instance;
			tess.startDrawingQuads();
			LogisticsNewRenderPipe.innerTransportBox.render(LogisticsNewRenderPipe.innerBoxTexture);
			tess.draw();
			GL11.glEndList();
		}
		y -= 0.5;
		z -= 0.5;
		x -= 0.5;
		GL11.glTranslated(x, y, z);
		Minecraft.getMinecraft().getTextureManager().bindTexture(BLOCKS);
		GL11.glCallList(renderList);
		if(itemstack != null && itemstack.getItem() instanceof LogisticsFluidContainer) {
			FluidStack f = SimpleServiceLocator.logisticsFluidManager.getFluidFromContainer(ItemIdentifierStack.getFromStack(itemstack));
			if(f != null) {
				FluidContainerRenderer.skipNext = true;
				int list = getRenderListFor(f);
				GL11.glCallList(list);
			}
		}
		GL11.glTranslated(-x, -y, -z);
	}
	
	private int getRenderListFor(FluidStack fluid) {
		FluidIdentifier ident = FluidIdentifier.get(fluid);
		int[] array = renderLists.get(fluid);
		if(array == null) {
			array = new int[RENDER_SIZE];
			renderLists.put(ident, array);
		}
		int pos = Math.min((int) (((Math.min(fluid.amount, 5000) * 1.0F) * RENDER_SIZE) / 5000), RENDER_SIZE - 1);
		if(array[pos] != 0) return array[pos];
		RenderInfo block = new RenderInfo();
	
		block.baseBlock = fluid.getFluid().getBlock();
		block.texture = fluid.getFluid().getStillIcon();
		
		float ratio = pos * 1.0F / (RENDER_SIZE - 1);
		
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
