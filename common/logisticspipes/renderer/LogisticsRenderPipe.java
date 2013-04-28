package logisticspipes.renderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import logisticspipes.logic.BaseLogicCrafting;
import logisticspipes.pipes.PipeItemsCraftingLogistics;
import logisticspipes.transport.PipeLiquidTransportLogistics;
import net.minecraft.block.Block;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.model.ModelSign;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.World;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.IItemRenderer.ItemRenderType;
import net.minecraftforge.client.IItemRenderer.ItemRendererHelper;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.liquids.LiquidStack;

import org.lwjgl.opengl.GL11;

import buildcraft.BuildCraftCore;
import buildcraft.BuildCraftCore.RenderMode;
import buildcraft.core.render.RenderEntityBlock;
import buildcraft.core.render.RenderEntityBlock.BlockInterface;
import buildcraft.core.utils.Utils;
import buildcraft.transport.Pipe;
import buildcraft.transport.TileGenericPipe;
import buildcraft.transport.render.RenderPipe;

public class LogisticsRenderPipe extends RenderPipe {

	final static private int LIQUID_STAGES = 40;

	private final int[] angleY = { 0, 0, 270, 90, 0, 180 };
	private final int[] angleZ = { 90, 270, 0, 0, 0, 0 };
	
	private HashMap<Integer, HashMap<Integer, DisplayLiquidList>> displayLiquidLists = new HashMap<Integer, HashMap<Integer, DisplayLiquidList>>();
	
	private class DisplayLiquidList {

		public int[] sideHorizontal = new int[LIQUID_STAGES];
		public int[] sideVertical = new int[LIQUID_STAGES];
		public int[] centerHorizontal = new int[LIQUID_STAGES];
		public int[] centerVertical = new int[LIQUID_STAGES];
	}
	
	@Override
	public void renderTileEntityAt(TileEntity tileentity, double x, double y, double z, float f) {
		super.renderTileEntityAt(tileentity, x, y, z, f);
		if (BuildCraftCore.render == RenderMode.NoDynamic) return;
		TileGenericPipe pipe = ((TileGenericPipe) tileentity);
		if (pipe.pipe == null) return;
		if (pipe.pipe.transport instanceof PipeLiquidTransportLogistics) {
			renderLiquids(pipe.pipe, x, y, z);
		}
		if(pipe.pipe instanceof PipeItemsCraftingLogistics) {
			renderCraftingPipe((PipeItemsCraftingLogistics) pipe.pipe, x, y, z);
		}
	}
	
	private boolean needDistance(List<ForgeDirection> list) {
		List<ForgeDirection> copy = new ArrayList<ForgeDirection>(list);
		copy.remove(ForgeDirection.UP);
		copy.remove(ForgeDirection.DOWN);
		copy.remove(ForgeDirection.UNKNOWN);
		boolean result = copy.size() > 1;
		if(copy.size() == 2) {
			if(copy.contains(ForgeDirection.NORTH) && copy.contains(ForgeDirection.SOUTH)) {
				result = false;
			}
			if(copy.contains(ForgeDirection.EAST) && copy.contains(ForgeDirection.WEST)) {
				result = false;
			}
		}
		return result;
	}
	
	private void renderCraftingPipe(PipeItemsCraftingLogistics pipe, double x, double y, double z) {
		if(!pipe.getCraftingSigns().isEmpty()) {
			List<ForgeDirection> list = pipe.getCraftingSigns();
			for(ForgeDirection dir:list) {
				if(pipe.container.getRenderState().pipeConnectionMatrix.isConnected(dir)) {
					continue;
				}
				GL11.glPushMatrix();
				GL11.glTranslatef((float) x + 0.5F, (float) y + 0.5F, (float) z + 0.5F);
				switch(dir) {
				case UP:
			        GL11.glRotatef(90, 1.0F, 0.0F, 0.0F);
					break;
				case DOWN:
			        GL11.glRotatef(-90, 1.0F, 0.0F, 0.0F);
					break;
				case NORTH:
			        GL11.glRotatef(0, 0.0F, 1.0F, 0.0F);
					if(needDistance(list)) {
				        GL11.glTranslatef(0.0F, 0.0F, -0.15F);
					}
					break;
				case SOUTH:
			        GL11.glRotatef(-180, 0.0F, 1.0F, 0.0F);
					if(needDistance(list)) {
				        GL11.glTranslatef(0.0F, 0.0F, -0.15F);
					}
					break;
				case EAST:
			        GL11.glRotatef(-90, 0.0F, 1.0F, 0.0F);
					if(needDistance(list)) {
				        GL11.glTranslatef(0.0F, 0.0F, -0.15F);
					}
					break;
				case WEST:
			        GL11.glRotatef(90, 0.0F, 1.0F, 0.0F);
					if(needDistance(list)) {
				        GL11.glTranslatef(0.0F, 0.0F, -0.15F);
					}
					break;
				default:;
				}
				renderSign(pipe);
				GL11.glPopMatrix();
			}			
		}
	}
	
	/** The ModelSign instance used by the TileEntitySignRenderer */
    private ModelSign modelSign = new ModelSign();

	private RenderBlocks renderBlocks = new RenderBlocks();
	RenderManager rendermanager = RenderManager.instance;
	
	private void renderSign(PipeItemsCraftingLogistics pipe) {
		float var10 = 0.6666667F;
        float var12 = 0.016666668F * var10;
        
        GL11.glTranslatef(0.0F, -0.3125F, -0.31F);
        GL11.glRotatef(180, 0.0f, 1.0f, 0.0f);
        this.modelSign.signStick.showModel = false;

        this.bindTextureByName("/item/sign.png");
        GL11.glPushMatrix();
        GL11.glScalef(var10, -var10, -var10);
        this.modelSign.renderSign();
        GL11.glPopMatrix();
        GL11.glTranslatef(-0.32F, 0.5F * var10 + 0.08F, 0.07F * var10);
       
        renderSignLabel(pipe, var12);
        
	}
	
	private void renderSignLabel(PipeItemsCraftingLogistics pipe, float var12) {
        FontRenderer var17 = this.getFontRenderer();
        if(pipe != null) {
    		List<ItemStack> craftables = ((BaseLogicCrafting)pipe.logic).getCraftedItems();

    		String name = "";
    		if(craftables != null && craftables.size() > 0) {
    			ItemStack itemstack = craftables.get(0);
	        	
	        	if(itemstack == null || itemstack.getItem() == null) return; //Only happens on false configuration
		        
	        	Item item = itemstack.getItem();
	        	
		        IItemRenderer customRenderer = MinecraftForgeClient.getItemRenderer(itemstack, ItemRenderType.INVENTORY);
		        
		        this.bindTextureByName(itemstack.getItemSpriteNumber() == 0 ? "/terrain.png" : "/gui/items.png");
		        
				if(customRenderer != null) {
			        	if(customRenderer.shouldUseRenderHelper(ItemRenderType.INVENTORY, itemstack, ItemRendererHelper.INVENTORY_BLOCK)) {
			        		GL11.glScalef(0.20F, -0.20F, -0.01F);
			                
			    	        GL11.glRotatef(210.0F, 1.0F, 0.0F, 0.0F);
			    	        GL11.glRotatef(-45.0F, 0.0F, 1.0F, 0.0F);
			    	
			    	        GL11.glDisable(GL11.GL_LIGHTING);
			    	        GL11.glDisable(GL11.GL_LIGHT0);
			    	        GL11.glDisable(GL11.GL_LIGHT1);
			    	        GL11.glDisable(GL11.GL_COLOR_MATERIAL);
			    	        
			    	        GL11.glPushMatrix();
			    	        
			    	        customRenderer.renderItem(ItemRenderType.INVENTORY, itemstack, renderBlocks);

			    	        GL11.glPopMatrix();
			    	        
			    	        GL11.glEnable(GL11.GL_LIGHTING);
			    	        GL11.glEnable(GL11.GL_LIGHT0);
			    	        GL11.glEnable(GL11.GL_LIGHT1);
			    	        GL11.glEnable(GL11.GL_COLOR_MATERIAL);
			
			    	        GL11.glRotatef(45.0F, 0.0F, 1.0F, 0.0F);
			    	        GL11.glRotatef(-210.0F, 1.0F, 0.0F, 0.0F);
			                
			    	        GL11.glNormal3f(0.0F, 0.0F, -1.0F * var12);
			    	        GL11.glDepthMask(false);
			    	
			    	        GL11.glTranslatef(2.5F, 0.0F, 0.0F);
			    	
			    	        GL11.glScalef(5.0F, 5.0F, 1.0F);
			    	
			    	        GL11.glTranslatef(0.0F, +0.08F, 0.0F);
			    	        
			    		    GL11.glScalef(var12, var12, var12);
			        	} else {
			        		GL11.glDisable(GL11.GL_LIGHTING);
			    	        GL11.glDisable(GL11.GL_LIGHT0);
			    	        GL11.glDisable(GL11.GL_LIGHT1);
			    	        GL11.glDisable(GL11.GL_COLOR_MATERIAL);
			    	        
			    	        //renderBlocks.useInventoryTint = false;

					        GL11.glPushMatrix();
					        
			    	        customRenderer.renderItem(ItemRenderType.INVENTORY, itemstack, renderBlocks);

					        GL11.glPopMatrix();
					        
			    	        GL11.glEnable(GL11.GL_LIGHTING);
			    	        GL11.glEnable(GL11.GL_LIGHT0);
			    	        GL11.glEnable(GL11.GL_LIGHT1);
			    	        GL11.glEnable(GL11.GL_COLOR_MATERIAL);
			        	}
			   } else if(item instanceof ItemBlock && RenderBlocks.renderItemIn3d(Block.blocksList[item.itemID].getRenderType())) {
		            GL11.glScalef(0.20F, -0.20F, -0.01F);
		            
			        GL11.glRotatef(210.0F, 1.0F, 0.0F, 0.0F);
			        GL11.glRotatef(-45.0F, 0.0F, 1.0F, 0.0F);
			
			        renderBlocks.useInventoryTint = false;

			        GL11.glDisable(GL11.GL_LIGHTING);
			        GL11.glDisable(GL11.GL_LIGHT0);
			        GL11.glDisable(GL11.GL_LIGHT1);
			        GL11.glDisable(GL11.GL_COLOR_MATERIAL);

			        GL11.glPushMatrix();
			        
			        renderBlocks.renderBlockAsItem(Block.blocksList[item.itemID], itemstack.getItemDamage(), 1.0F);

			        GL11.glPopMatrix();
			        
			        GL11.glEnable(GL11.GL_LIGHTING);
			        GL11.glEnable(GL11.GL_LIGHT0);
			        GL11.glEnable(GL11.GL_LIGHT1);
			        GL11.glEnable(GL11.GL_COLOR_MATERIAL);
			        
			        GL11.glRotatef(45.0F, 0.0F, 1.0F, 0.0F);
	    	        GL11.glRotatef(-210.0F, 1.0F, 0.0F, 0.0F);
	                
			        GL11.glNormal3f(0.0F, 0.0F, -1.0F * var12);
			        GL11.glDepthMask(false);
			
			        GL11.glTranslatef(2.5F, 0.0F, 0.0F);
			
			        GL11.glScalef(5.0F, 5.0F, 1.0F);
			
			        GL11.glTranslatef(0.0F, +0.08F, 0.0F);
			        
				    GL11.glScalef(var12, var12, var12);
		        } else {
		        	GL11.glScalef(0.25F, -0.25F, -0.01F);
		            
			        GL11.glRotatef(180.0F, 1.0F, 0.0F, 0.0F);
			        GL11.glRotatef(0.0F, 0.0F, 1.0F, 0.0F);
			        
			        GL11.glDisable(GL11.GL_LIGHTING);
			        GL11.glDisable(GL11.GL_LIGHT0);
			        GL11.glDisable(GL11.GL_LIGHT1);
			        GL11.glDisable(GL11.GL_COLOR_MATERIAL);
			        
			        GL11.glTranslatef(0.1F, -0.2F, 0.0F);

			        GL11.glPushMatrix();
			        
			        if (item.requiresMultipleRenderPasses())
		            {
		                for (int var14 = 0; var14 < item.getRenderPasses(itemstack.getItemDamage()); ++var14)
		                {
		                    Icon var15 = item.getIconFromDamageForRenderPass(itemstack.getItemDamage(), var14);
		                    renderItem(var15);
		                }
		            }
		            else
		            {
		            	renderItem(item.getIconIndex(itemstack));
		            }

			        GL11.glPopMatrix();
			        
			        GL11.glTranslatef(-0.6F, 0.2F, 0.0F);
		
			        GL11.glEnable(GL11.GL_LIGHTING);
			        GL11.glEnable(GL11.GL_LIGHT0);
			        GL11.glEnable(GL11.GL_LIGHT1);
			        GL11.glEnable(GL11.GL_COLOR_MATERIAL);
			
			        GL11.glRotatef(-0.0F, 0.0F, 1.0F, 0.0F);
			        GL11.glRotatef(-180.0F, 1.0F, 0.0F, 0.0F);
		            
			        GL11.glNormal3f(0.0F, 0.0F, -1.0F * var12);
			        GL11.glDepthMask(false);
		
			        GL11.glTranslatef(2.5F, 0.0F, 0.0F);
			
			        GL11.glScalef(4.0F, 4.0F, 1.0F);
			
			        GL11.glTranslatef(0.0F, +0.08F, 0.0F);
			        
				    GL11.glScalef(var12, var12, var12);
		        }
		        try {
		        	name = item.getItemDisplayName(itemstack);
		        } catch(Exception e) {
		        	try {
		        		name = item.getUnlocalizedName();
		        	} catch(Exception e1) {}
		        }
		        
		        var17.drawString("ID: "+String.valueOf(item.itemID), -var17.getStringWidth("ID: "+String.valueOf(item.itemID)) / 2, 0 * 10 - 4 * 5, 0);
		        if(((BaseLogicCrafting)pipe.logic).satelliteId != 0) {
		        	var17.drawString("Sat ID: "+String.valueOf(((BaseLogicCrafting)pipe.logic).satelliteId), -var17.getStringWidth("Sat ID: "+String.valueOf(((BaseLogicCrafting)pipe.logic).satelliteId)) / 2, 1 * 10 - 4 * 5, 0);
		        }
    		} else {
    			GL11.glScalef(0.25F, -0.25F, 0.00F);
    			
		        GL11.glDisable(GL11.GL_LIGHTING);
		        GL11.glDisable(GL11.GL_LIGHT0);
		        GL11.glDisable(GL11.GL_LIGHT1);
		        GL11.glDisable(GL11.GL_COLOR_MATERIAL);
		        
		        GL11.glTranslatef(0.1F, -0.2F, 0.0F);
		        GL11.glTranslatef(-0.6F, 0.2F, 0.0F);
	
		        GL11.glEnable(GL11.GL_LIGHTING);
		        GL11.glEnable(GL11.GL_LIGHT0);
		        GL11.glEnable(GL11.GL_LIGHT1);
		        GL11.glEnable(GL11.GL_COLOR_MATERIAL);
		
		        GL11.glNormal3f(0.0F, 0.0F, -1.0F * var12);
		        GL11.glDepthMask(false);
	
		        GL11.glTranslatef(2.5F, 0.0F, 0.0F);
		
		        GL11.glScalef(4.0F, 4.0F, 1.0F);
		
		        GL11.glTranslatef(0.0F, +0.08F, 0.0F);
		        
			    GL11.glScalef(var12, var12, var12);
			    name = "Empty";
    		}
    		
	        name = cut(name, var17);
	        
	        var17.drawString(name, -var17.getStringWidth(name) / 2 - 15, 3 * 10 - 4 * 5, 0);

	        GL11.glDepthMask(true);
	        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
	    }
	}
	
	private void renderItem(Icon paricon) {
		Tessellator var3 = Tessellator.instance;
		float var4 = (paricon.getSheetHeight() % 16 * 16 + 0) / 256.0F;
        float var5 = (paricon.getSheetWidth() % 16 * 16 + 16) / 256.0F;
        float var6 = (paricon.getSheetHeight() / 16 * 16 + 0) / 256.0F;
        float var7 = (paricon.getSheetWidth() / 16 * 16 + 16) / 256.0F;
        float var8 = 1.0F;
        float var9 = 0.5F;
        float var10 = 0.25F;
        
		GL11.glPushMatrix();
        var3.startDrawingQuads();
        var3.setNormal(0.0F, 1.0F, 0.0F);
        var3.addVertexWithUV(0.0F - var9, 0.0F - var10, 0.0D, var4, var7);
        var3.addVertexWithUV(var8 - var9, 0.0F - var10, 0.0D, var5, var7);
        var3.addVertexWithUV(var8 - var9, 1.0F - var10, 0.0D, var5, var6);
        var3.addVertexWithUV(0.0F - var9, 1.0F - var10, 0.0D, var4, var6);
        var3.draw();
        GL11.glPopMatrix();
	}
	
	private String cut(String name, FontRenderer renderer) {
		if(renderer.getStringWidth(name) < 90) {
			return name;
		}
		StringBuilder sum = new StringBuilder();
		for(int i=0; i < name.length(); i++) {
			if(renderer.getStringWidth(sum.toString() + name.charAt(i) + "...") < 90) {
				sum.append(name.charAt(i));
			} else {
				return sum.toString() + "...";
			}
		}
		return sum.toString();
	}
	
	private void renderLiquids(Pipe pipe, double x, double y, double z) {
		PipeLiquidTransportLogistics liq = (PipeLiquidTransportLogistics) pipe.transport;
		GL11.glPushMatrix();
		GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		GL11.glTranslatef((float) x, (float) y, (float) z);

		// sides

		boolean sides = false, above = false;

		for (int i = 0; i < 6; ++i) {
			// ILiquidTank tank = liq.getTanks()[i];
			// LiquidStack liquid = tank.getLiquid();
			LiquidStack liquid = liq.renderCache[i];
			// int amount = liquid != null ? liquid.amount : 0;
			// int amount = liquid != null ? liq.renderAmmount[i] : 0;

			if (liquid != null && liquid.amount > 0) {
				DisplayLiquidList d = getListFromBuffer(liquid, pipe.worldObj);

				if (d == null) {
					continue;
				}

				int stage = (int) ((float) liquid.amount / (float) (liq.getSideCapacity()) * (LIQUID_STAGES - 1));

				GL11.glPushMatrix();
				int list = 0;

				switch (ForgeDirection.VALID_DIRECTIONS[i]) {
				case UP:
					above = true;
					list = d.sideVertical[stage];
					break;
				case DOWN:
					GL11.glTranslatef(0, -0.75F, 0);
					list = d.sideVertical[stage];
					break;
				case EAST:
				case WEST:
				case SOUTH:
				case NORTH:
					sides = true;
					// Yes, this is kind of ugly, but was easier than transform the coordinates above.
					GL11.glTranslatef(0.5F, 0.0F, 0.5F);
					GL11.glRotatef(angleY[i], 0, 1, 0);
					GL11.glRotatef(angleZ[i], 0, 0, 1);
					GL11.glTranslatef(-0.5F, 0.0F, -0.5F);
					list = d.sideHorizontal[stage];
					break;
				default:
				}
				bindTextureByName(liquid.canonical().getTextureSheet());
				GL11.glCallList(list);
				GL11.glPopMatrix();
			}
		}
		// CENTER
		// ILiquidTank tank = liq.getTanks()[ForgeDirection.Unknown.ordinal()];
		// LiquidStack liquid = tank.getLiquid();
		LiquidStack liquid = liq.renderCache[ForgeDirection.UNKNOWN.ordinal()];

		// int amount = liquid != null ? liquid.amount : 0;
		// int amount = liquid != null ? liq.renderAmmount[ForgeDirection.Unknown.ordinal()] : 0;
		if (liquid != null && liquid.amount > 0) {
			// DisplayLiquidList d = getListFromBuffer(liq.getTanks()[ForgeDirection.Unknown.ordinal()].getLiquid(), pipe.worldObj);
			DisplayLiquidList d = getListFromBuffer(liquid, pipe.worldObj);

			if (d != null) {
				int stage = (int) ((float) liquid.amount / (float) (liq.getInnerCapacity()) * (LIQUID_STAGES - 1));

				bindTextureByName(liquid.canonical().getTextureSheet());
				
				if (above) {
					GL11.glCallList(d.centerVertical[stage]);
				}

				if (!above || sides) {
					GL11.glCallList(d.centerHorizontal[stage]);
				}
			}

		}
		
		GL11.glPopAttrib();
		GL11.glPopMatrix();
	}

	private DisplayLiquidList getListFromBuffer(LiquidStack stack, World world) {

		int liquidId = stack.itemID;

		if (liquidId == 0)
			return null;

		return getDisplayLiquidLists(liquidId, stack.itemMeta, world);
	}

	private DisplayLiquidList getDisplayLiquidLists(int liquidId, int meta, World world) {
		if (displayLiquidLists.containsKey(liquidId)) {
			HashMap<Integer, DisplayLiquidList> x = displayLiquidLists.get(liquidId);
			if (x.containsKey(meta))
				return x.get(meta);
		} else {
			displayLiquidLists.put(liquidId, new HashMap<Integer, DisplayLiquidList>());
		}

		DisplayLiquidList d = new DisplayLiquidList();
		displayLiquidLists.get(liquidId).put(meta, d);

		BlockInterface block = new BlockInterface();

		if (liquidId < Block.blocksList.length && Block.blocksList[liquidId] != null) {
			block.baseBlock = Block.blocksList[liquidId];
		} else {
			block.baseBlock = Block.waterStill;
			block.texture = Item.itemsList[liquidId].getIconFromDamage(meta);
		}

		float size = Utils.pipeMaxPos - Utils.pipeMinPos;

		// render size

		for (int s = 0; s < LIQUID_STAGES; ++s) {
			float ratio = (float) s / (float) LIQUID_STAGES;

			// SIDE HORIZONTAL

			d.sideHorizontal[s] = GLAllocation.generateDisplayLists(1);
			GL11.glNewList(d.sideHorizontal[s], 4864 /* GL_COMPILE */);

			block.minX = 0.0F;
			block.minZ = Utils.pipeMinPos + 0.01F;

			block.maxX = block.minX + size / 2F + 0.01F;
			block.maxZ = block.minZ + size - 0.02F;

			block.minY = Utils.pipeMinPos + 0.01F;
			block.maxY = block.minY + (size - 0.02F) * ratio;

			RenderEntityBlock.renderBlock(block, world, 0, 0, 0, false, true);

			GL11.glEndList();

			// SIDE VERTICAL

			d.sideVertical[s] = GLAllocation.generateDisplayLists(1);
			GL11.glNewList(d.sideVertical[s], 4864 /* GL_COMPILE */);

			block.minY = Utils.pipeMaxPos - 0.01;
			block.maxY = 1;

			block.minX = 0.5 - (size / 2 - 0.01) * ratio;
			block.maxX = 0.5 + (size / 2 - 0.01) * ratio;

			block.minZ = 0.5 - (size / 2 - 0.01) * ratio;
			block.maxZ = 0.5 + (size / 2 - 0.01) * ratio;

			RenderEntityBlock.renderBlock(block, world, 0, 0, 0, false, true);

			GL11.glEndList();

			// CENTER HORIZONTAL

			d.centerHorizontal[s] = GLAllocation.generateDisplayLists(1);
			GL11.glNewList(d.centerHorizontal[s], 4864 /* GL_COMPILE */);

			block.minX = Utils.pipeMinPos + 0.01;
			block.minZ = Utils.pipeMinPos + 0.01;

			block.maxX = block.minX + size - 0.02;
			block.maxZ = block.minZ + size - 0.02;

			block.minY = Utils.pipeMinPos + 0.01;
			block.maxY = block.minY + (size - 0.02F) * ratio;

			RenderEntityBlock.renderBlock(block, world, 0, 0, 0, false, true);

			GL11.glEndList();

			// CENTER VERTICAL

			d.centerVertical[s] = GLAllocation.generateDisplayLists(1);
			GL11.glNewList(d.centerVertical[s], 4864 /* GL_COMPILE */);

			block.minY = Utils.pipeMinPos + 0.01;
			block.maxY = Utils.pipeMaxPos - 0.01;

			block.minX = 0.5 - (size / 2 - 0.02) * ratio;
			block.maxX = 0.5 + (size / 2 - 0.02) * ratio;

			block.minZ = 0.5 - (size / 2 - 0.02) * ratio;
			block.maxZ = 0.5 + (size / 2 - 0.02) * ratio;

			RenderEntityBlock.renderBlock(block, world, 0, 0, 0, false, true);

			GL11.glEndList();

		}

		return d;
	}
}
