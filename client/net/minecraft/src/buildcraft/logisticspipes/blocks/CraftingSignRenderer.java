package net.minecraft.src.buildcraft.logisticspipes.blocks;

import static net.minecraft.src.forge.IItemRenderer.ItemRenderType.INVENTORY;
import static net.minecraft.src.forge.IItemRenderer.ItemRendererHelper.INVENTORY_BLOCK;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.src.Block;
import net.minecraft.src.EntityItem;
import net.minecraft.src.FontRenderer;
import net.minecraft.src.Item;
import net.minecraft.src.ItemBlock;
import net.minecraft.src.ItemRenderer;
import net.minecraft.src.ItemStack;
import net.minecraft.src.ModelSign;
import net.minecraft.src.OpenGlHelper;
import net.minecraft.src.RenderBlocks;
import net.minecraft.src.RenderHelper;
import net.minecraft.src.RenderItem;
import net.minecraft.src.RenderManager;
import net.minecraft.src.Tessellator;
import net.minecraft.src.TileEntity;
import net.minecraft.src.TileEntityRenderer;
import net.minecraft.src.TileEntitySign;
import net.minecraft.src.TileEntitySpecialRenderer;
import net.minecraft.src.buildcraft.krapht.CoreRoutedPipe;
import net.minecraft.src.buildcraft.krapht.logic.LogicCrafting;
import net.minecraft.src.buildcraft.krapht.pipes.PipeItemsCraftingLogistics;
import net.minecraft.src.buildcraft.krapht.pipes.PipeItemsCraftingLogisticsMK2;
import net.minecraft.src.forge.IItemRenderer;
import net.minecraft.src.forge.MinecraftForgeClient;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

public class CraftingSignRenderer extends TileEntitySpecialRenderer {

    /** The ModelSign instance used by the TileEntitySignRenderer */
    private ModelSign modelSign = new ModelSign();

	private RenderItem renderItem = new RenderItem();
	private RenderBlocks renderBlocks = new RenderBlocks();
	RenderManager rendermanager = RenderManager.instance;
	
	private static long timesub = System.currentTimeMillis();
	//private static int ID = 1;
	
    public CraftingSignRenderer() {
    	setTileEntityRenderer(TileEntityRenderer.instance);
    	renderItem.setRenderManager(rendermanager);
    }

	public void renderTileEntitySignAt(LogisticsTileEntiy par1TileEntitySign, double par2, double par4, double par6, float par8)
    {	
		PipeItemsCraftingLogistics pipe = par1TileEntitySign.getAttachedSignOwnerPipe();
		GL11.glPushMatrix();
        float var10 = 0.6666667F;
        float var12 = 0;
        if(pipe != null) {
			int disX = pipe.xCoord - par1TileEntitySign.xCoord;
			int disZ = pipe.zCoord - par1TileEntitySign.zCoord;
	        
	        int var16 = par1TileEntitySign.getBlockMetadata();
	        if (disZ > 0)
	        {
	            var12 = 180.0F;
	        }
	        if (disX > 0)
	        {
	            var12 = 90.0F;
	        }
	        if (disX < 0)
	        {
	            var12 = -90.0F;
	        }
        }
        
        GL11.glTranslatef((float)par2 + 0.5F, (float)par4 + 0.75F * var10, (float)par6 + 0.5F);
        GL11.glRotatef(-var12, 0.0F, 1.0F, 0.0F);
        GL11.glTranslatef(0.0F, -0.3125F, -0.4375F);
        this.modelSign.signStick.showModel = false;

        this.bindTextureByName("/item/sign.png");
        GL11.glPushMatrix();
        GL11.glScalef(var10, -var10, -var10);
        this.modelSign.renderSign();
        GL11.glPopMatrix();
        FontRenderer var17 = this.getFontRenderer();
        var12 = 0.016666668F * var10;
        GL11.glTranslatef(-0.32F, 0.5F * var10 + 0.08F, 0.07F * var10);
        
        /*if(!Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) && ((System.currentTimeMillis() - timesub > 500 && !Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) || (System.currentTimeMillis() - timesub > 100 && Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)))) {
        	timesub = System.currentTimeMillis();
        	ID++;
        	if(ID >= Item.itemsList.length) {
        		ID = 1;
        	} else {
        		while(Item.itemsList[ID] == null) {
        			ID++;
        			if(ID >= Item.itemsList.length) {
                		ID = 1;
        			}
        		}
        	}
        }*/

        if(pipe != null) {
        	LogicCrafting craftingLogic = (LogicCrafting) pipe.logic;
    		ItemStack itemstack = craftingLogic.getCraftedItem();
    		String name = "";
    		if(itemstack != null) {
	        	Item item = itemstack.getItem();
		        
		        this.bindTextureByName(item.getTextureFile());
		        
		        IItemRenderer customRenderer = MinecraftForgeClient.getItemRenderer(itemstack, INVENTORY);
		        
		        if(item instanceof ItemBlock && RenderBlocks.renderItemIn3d(Block.blocksList[item.shiftedIndex].getRenderType())) {
		            GL11.glScalef(0.20F, -0.20F, 0.00F);
		            
			        GL11.glRotatef(210.0F, 1.0F, 0.0F, 0.0F);
			        GL11.glRotatef(-45.0F, 0.0F, 1.0F, 0.0F);
			
			        renderBlocks.useInventoryTint = false;
			        
			        GL11.glDisable(GL11.GL_LIGHTING);
			        GL11.glDisable(GL11.GL_LIGHT0);
			        GL11.glDisable(GL11.GL_LIGHT1);
			        GL11.glDisable(GL11.GL_COLOR_MATERIAL);
			        
			        renderBlocks.renderBlockAsItem(Block.blocksList[item.shiftedIndex], /* Damage */ 0, 1.0F);
			        
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
		        } else if(customRenderer != null) {
		        	if(customRenderer.shouldUseRenderHelper(INVENTORY, itemstack, INVENTORY_BLOCK)) {
		        		GL11.glScalef(0.20F, -0.20F, 0.00F);
		                
		    	        GL11.glRotatef(210.0F, 1.0F, 0.0F, 0.0F);
		    	        GL11.glRotatef(-45.0F, 0.0F, 1.0F, 0.0F);
		    	
		    	        GL11.glDisable(GL11.GL_LIGHTING);
		    	        GL11.glDisable(GL11.GL_LIGHT0);
		    	        GL11.glDisable(GL11.GL_LIGHT1);
		    	        GL11.glDisable(GL11.GL_COLOR_MATERIAL);
		    	        
		    	        renderBlocks.useInventoryTint = false;
		    	        
		    	        customRenderer.renderItem(INVENTORY, itemstack, renderBlocks);
		    	        
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
		    	        
		    	        renderBlocks.useInventoryTint = false;
		    	        
		    	        customRenderer.renderItem(INVENTORY, itemstack, renderBlocks);
		    	        
		    	        GL11.glEnable(GL11.GL_LIGHTING);
		    	        GL11.glEnable(GL11.GL_LIGHT0);
		    	        GL11.glEnable(GL11.GL_LIGHT1);
		    	        GL11.glEnable(GL11.GL_COLOR_MATERIAL);
		        	}
		        } else {
		        	GL11.glScalef(0.25F, -0.25F, 0.00F);
		            
			        GL11.glRotatef(180.0F, 1.0F, 0.0F, 0.0F);
			        GL11.glRotatef(0.0F, 0.0F, 1.0F, 0.0F);
			        
			        GL11.glDisable(GL11.GL_LIGHTING);
			        GL11.glDisable(GL11.GL_LIGHT0);
			        GL11.glDisable(GL11.GL_LIGHT1);
			        GL11.glDisable(GL11.GL_COLOR_MATERIAL);
			        
			        GL11.glTranslatef(0.1F, -0.2F, 0.0F);
		
			        if (item.requiresMultipleRenderPasses())
		            {
		                for (int var14 = 0; var14 < item.getRenderPasses(itemstack.getItemDamage()); ++var14)
		                {
		                    int var15 = item.func_46057_a(itemstack.getItemDamage(), var14);
		                    renderItem(var15);
		                }
		            }
		            else
		            {
		            	renderItem(item.getIconIndex(new ItemStack(item,1)));
		            }
			        
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
		        	name = item.getItemDisplayName(new ItemStack(item,1));
		        } catch(Exception e) {
		        	try {
		        		name = item.getItemName();
		        	} catch(Exception e1) {}
		        }
		        
		        var17.drawString("ID: "+String.valueOf(item.shiftedIndex), -var17.getStringWidth("ID: "+String.valueOf(item.shiftedIndex)) / 2, 0 * 10 - 4 * 5, 0);
		        if(((LogicCrafting)pipe.logic).satelliteId != 0) {
		        	var17.drawString("Sat ID: "+String.valueOf(((LogicCrafting)pipe.logic).satelliteId), -var17.getStringWidth("Sat ID: "+String.valueOf(((LogicCrafting)pipe.logic).satelliteId)) / 2, 1 * 10 - 4 * 5, 0);
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
        GL11.glPopMatrix();
    }
	
	private void renderItem(int par1) {
		Tessellator var3 = Tessellator.instance;
		float var4 = (float)(par1 % 16 * 16 + 0) / 256.0F;
        float var5 = (float)(par1 % 16 * 16 + 16) / 256.0F;
        float var6 = (float)(par1 / 16 * 16 + 0) / 256.0F;
        float var7 = (float)(par1 / 16 * 16 + 16) / 256.0F;
        float var8 = 1.0F;
        float var9 = 0.5F;
        float var10 = 0.25F;
        
		GL11.glPushMatrix();
        var3.startDrawingQuads();
        var3.setNormal(0.0F, 1.0F, 0.0F);
        var3.addVertexWithUV((double)(0.0F - var9), (double)(0.0F - var10), 0.0D, (double)var4, (double)var7);
        var3.addVertexWithUV((double)(var8 - var9), (double)(0.0F - var10), 0.0D, (double)var5, (double)var7);
        var3.addVertexWithUV((double)(var8 - var9), (double)(1.0F - var10), 0.0D, (double)var5, (double)var6);
        var3.addVertexWithUV((double)(0.0F - var9), (double)(1.0F - var10), 0.0D, (double)var4, (double)var6);
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
	
	@Override
	public void renderTileEntityAt(TileEntity par1TileEntity, double par2, double par4, double par6, float par8) {
        this.renderTileEntitySignAt((LogisticsTileEntiy)par1TileEntity, par2, par4, par6, par8);
    }
}
