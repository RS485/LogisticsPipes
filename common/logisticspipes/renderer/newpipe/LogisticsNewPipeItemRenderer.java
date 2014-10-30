package logisticspipes.renderer.newpipe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import logisticspipes.LPConstants;
import logisticspipes.LogisticsPipes;
import logisticspipes.items.ItemLogisticsPipe;
import logisticspipes.pipes.basic.LogisticsBlockGenericPipe;
import logisticspipes.renderer.newpipe.LogisticsNewRenderPipe.Corner;
import logisticspipes.renderer.newpipe.LogisticsNewRenderPipe.Edge;
import logisticspipes.renderer.newpipe.LogisticsNewRenderPipe.Mount;
import logisticspipes.renderer.newpipe.LogisticsNewRenderPipe.Support;
import logisticspipes.renderer.newpipe.LogisticsNewRenderPipe.Turn_Corner;
import logisticspipes.textures.Textures;
import logisticspipes.utils.tuples.LPPosition;
import logisticspipes.utils.tuples.Pair;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.common.util.ForgeDirection;

import org.lwjgl.opengl.GL11;

import codechicken.lib.render.CCModel;
import codechicken.lib.render.uv.IconTransformation;

public class LogisticsNewPipeItemRenderer implements IItemRenderer {
	
	private final boolean renderAsBlock;

	private static final float PIPE_MIN_POS = 0.25F;
	private static final float PIPE_MAX_POS = 0.75F;
	
	public LogisticsNewPipeItemRenderer(boolean flag) {
		renderAsBlock = flag;
	}

	private void renderPipeItem(RenderBlocks render, ItemStack item, float translateX, float translateY, float translateZ) {
		GL11.glPushAttrib(GL11.GL_COLOR_BUFFER_BIT); //don't break other mods' guis when holding a pipe
		//force transparency
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glEnable(GL11.GL_BLEND);
		//GL11.glDisable(GL11.GL_LIGHTING);

		// GL11.glBindTexture(GL11.GL_TEXTURE_2D, 10);
		Tessellator tessellator = Tessellator.instance;

		GL11.glTranslatef(translateX, translateY, translateZ);
		Block block = LogisticsPipes.LogisticsPipeBlock;
		if(item.getItem() instanceof ItemLogisticsPipe) {
			ItemLogisticsPipe lItem = (ItemLogisticsPipe)item.getItem();
			int renderList = lItem.getNewPipeRenderList();
			
			if(renderList == -1) {
				lItem.setNewPipeRenderList(GL11.glGenLists(1));
				renderList = lItem.getNewPipeRenderList();
				GL11.glNewList(renderList, GL11.GL_COMPILE);
				tessellator.startDrawingQuads();
				generatePipeRenderList(lItem.getNewPipeIconIndex());
				tessellator.draw();
				GL11.glEndList();
			}
			GL11.glCallList(renderList);
		}
		GL11.glTranslatef(0.5F, 0.5F, 0.5F);
		block.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);

		GL11.glPopAttrib(); // nicely leave the rendering how it was
	}
	
	private void generatePipeRenderList(int texture) {
		List<Pair<CCModel, IconTransformation>> objectsToRender = new ArrayList<Pair<CCModel, IconTransformation>>();
		
		for(Corner corner: Corner.values()) {
			for(CCModel model:LogisticsNewRenderPipe.corners_M.get(corner)) {
				objectsToRender.add(new Pair<CCModel, IconTransformation>(model, LogisticsNewRenderPipe.basicTexture));
			}
		}
		
		for(Edge edge: Edge.values()) {
			objectsToRender.add(new Pair<CCModel, IconTransformation>(LogisticsNewRenderPipe.edges.get(edge), LogisticsNewRenderPipe.basicTexture));
		}
		
		//ArrayList<Pair<CCModel, IconTransformation>> objectsToRender2 = new ArrayList<Pair<CCModel, IconTransformation>>();
		for(ForgeDirection dir:ForgeDirection.VALID_DIRECTIONS) {
			for(CCModel model:LogisticsNewRenderPipe.texturePlate_Outer.get(dir)) {
				IconTransformation icon = Textures.LPnewPipeIconProvider.getIcon(texture);
				if(icon != null) {
					objectsToRender.add(new Pair<CCModel, IconTransformation>(model, icon));
				}
			}
		}
		
		for(Pair<CCModel, IconTransformation> part:objectsToRender) {
			part.getValue1().render(part.getValue2());
		}
		
	}
	
	private void renderBlockItem(RenderBlocks render, ItemStack item, float translateX, float translateY, float translateZ) {
		Tessellator tessellator = Tessellator.instance;

		Block block = LogisticsPipes.LogisticsPipeBlock;
		
		block.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
		block.setBlockBoundsForItemRender();
		render.setRenderBoundsFromBlock(block);

		GL11.glTranslatef(translateX, translateY, translateZ);
		tessellator.startDrawingQuads();
		tessellator.setNormal(0.0F, -1F, 0.0F);
		render.renderFaceYNeg(block, 0.0D, 0.0D, 0.0D, LogisticsBlockGenericPipe.getRequestTableTextureFromSide(0));
		tessellator.draw();
		tessellator.startDrawingQuads();
		tessellator.setNormal(0.0F, 1.0F, 0.0F);
		render.renderFaceYPos(block, 0.0D, 0.0D, 0.0D, LogisticsBlockGenericPipe.getRequestTableTextureFromSide(1));
		tessellator.draw();
		tessellator.startDrawingQuads();
		tessellator.setNormal(0.0F, 0.0F, -1F);
		render.renderFaceZNeg(block, 0.0D, 0.0D, 0.0D, LogisticsBlockGenericPipe.getRequestTableTextureFromSide(2));
		tessellator.draw();
		tessellator.startDrawingQuads();
		tessellator.setNormal(0.0F, 0.0F, 1.0F);
		render.renderFaceZPos(block, 0.0D, 0.0D, 0.0D, LogisticsBlockGenericPipe.getRequestTableTextureFromSide(3));
		tessellator.draw();
		tessellator.startDrawingQuads();
		tessellator.setNormal(-1F, 0.0F, 0.0F);
		render.renderFaceXNeg(block, 0.0D, 0.0D, 0.0D, LogisticsBlockGenericPipe.getRequestTableTextureFromSide(4));
		tessellator.draw();
		tessellator.startDrawingQuads();
		tessellator.setNormal(1.0F, 0.0F, 0.0F);
		render.renderFaceXPos(block, 0.0D, 0.0D, 0.0D, LogisticsBlockGenericPipe.getRequestTableTextureFromSide(5));
		tessellator.draw();
		GL11.glTranslatef(0.5F, 0.5F, 0.5F);
		block.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
	}
	
	private void renderItem(RenderBlocks render, ItemStack item, float translateX, float translateY, float translateZ) {
		if(renderAsBlock) {
			renderBlockItem(render, item, translateX, translateY, translateZ);
		} else {
			renderPipeItem(render, item, translateX, translateY, translateZ);
		}
	}
		
	/** IItemRenderer implementation **/

	@Override
	public boolean handleRenderType(ItemStack item, ItemRenderType type) {
		switch (type) {
		case ENTITY:
			return true;
		case EQUIPPED:
			return true;
		case EQUIPPED_FIRST_PERSON:
			return true;
		case INVENTORY:
			return true;
		default:
			return false;
		}
	}

	@Override
	public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) {
		return true;
	}

	@Override
	public void renderItem(ItemRenderType type, ItemStack item, Object... data) {
		switch (type) {
			case ENTITY:
				renderItem((RenderBlocks) data[0], item, -0.5f, -0.5f, -0.5f);
				break;
			case EQUIPPED:
				renderItem((RenderBlocks) data[0], item, -0.4f, 0.50f, 0.35f);
				break;
			case EQUIPPED_FIRST_PERSON:
				renderItem((RenderBlocks) data[0], item, -0.4f, 0.50f, 0.35f);
				break;
			case INVENTORY:
				renderItem((RenderBlocks) data[0], item, -0.5f, -0.5f, -0.5f);
				break;
			default:
		}
	}
}
