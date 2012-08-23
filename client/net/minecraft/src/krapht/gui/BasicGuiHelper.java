package net.minecraft.src.krapht.gui;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.src.FontRenderer;
import net.minecraft.src.Gui;
import net.minecraft.src.ItemRenderer;
import net.minecraft.src.ItemStack;
import net.minecraft.src.ModLoader;
import net.minecraft.src.RenderItem;
import net.minecraft.src.Tessellator;
import net.minecraft.src.buildcraft.krapht.SimpleServiceLocator;
import net.minecraft.src.krapht.ItemIdentifier;
import net.minecraft.src.krapht.ItemIdentifierStack;
import net.minecraft.src.krapht.gui.KraphtBaseGuiScreen.Colors;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

public class BasicGuiHelper {
	
	public static String getCuttedString(String input, int maxLength, FontRenderer renderer) {
		if(renderer.getStringWidth(input) < maxLength) {
			return input;
		}
		input += "...";
		while(renderer.getStringWidth(input) > maxLength && input.length() > 0) {
			input = input.substring(0, input.length() - 4) + "...";
		}
		return input;
	}
	
	public static int ConvertEnumToColor(Colors color){
		switch(color){
			case Black:
				return 0xFF000000;
			case White:
				return 0xFFFFFFFF;
			case DarkGrey:
				return 0xFF555555;
			case MiddleGrey:
				return 0xFF8b8b8b;
			case LightGrey:
				return 0xFFC6C6C6;
			case Red:
				return 0xFFFF0000;
			
			default: 
				return 0;
			}
	}
	
	public static void renderItemIdentifierStackListIntoGui(List<ItemIdentifierStack> _allItems, IItemSearch IItemSearch, int page, int left , int top, int columns, int items, int xSize, int ySize, Minecraft mc, boolean displayAmount, boolean forcenumber) {
		int ppi = 0;
		int column = 0;
		int row = 0;
		FontRenderer fontRenderer = mc.fontRenderer;
		RenderItem renderItem = new RenderItem();
		for(ItemIdentifierStack itemStack : _allItems) {
			ItemIdentifier item = itemStack.getItem();
			if(!IItemSearch.itemSearched(item)) continue;
			ppi++;
			
			if (ppi <= items * page) continue;
			if (ppi > items * (page+1)) continue;
			ItemStack st = itemStack.makeNormalStack();
			int x = left + xSize * column;
			int y = top + ySize * row;

			GL11.glDisable(2896 /*GL_LIGHTING*/);
			
			renderItem.renderItemIntoGUI(fontRenderer, mc.renderEngine, st, x, y);
			if(displayAmount) {
				String s;
				if (st.stackSize == 1 && !forcenumber){
					s = "";
				} else if (st.stackSize < 1000) {
					s = st.stackSize + "";
				} else if (st.stackSize < 1000000){
					s = st.stackSize / 1000 + "K";
				} else {
					s = st.stackSize / 1000000 + "M";
				}
					
				GL11.glDisable(2896 /*GL_LIGHTING*/);
				GL11.glDisable(2929 /*GL_DEPTH_TEST*/);			
				fontRenderer.drawStringWithShadow(s, x + 16 - fontRenderer.getStringWidth(s), y + 8, 0xFFFFFF);
		        GL11.glEnable(2929 /*GL_DEPTH_TEST*/);
				GL11.glEnable(2896 /*GL_LIGHTING*/);
			}

			column++;
			if (column >= columns){
				row++;
				column = 0;
			}
		}
		GL11.glDisable(2896 /*GL_LIGHTING*/);
	}
	
	private static float zLevel;
	
	public static void displayItemToolTip(Object[] tooltip, Gui gui, float pzLevel, int guiLeft, int guiTop) {
		displayItemToolTip(tooltip, gui, pzLevel, guiLeft, guiTop, false);
	}
	
	public static void displayItemToolTip(Object[] tooltip, Gui gui, float pzLevel, int guiLeft, int guiTop, boolean forceminecraft) {		
		zLevel = pzLevel;
		if(tooltip != null) {
			try {
				if(forceminecraft) {
					throw new Exception();
				}
				//Look for NEI
				Class<?> NEIUtils = Class.forName("codechicken.nei.NEIUtils");
				Class<?> NEIConfig = Class.forName("codechicken.nei.NEIConfig");
				Method itemDisplayNameMultiline = NEIUtils.getDeclaredMethod("itemDisplayNameMultiline", new Class[]{ItemStack.class, boolean.class, boolean.class});
				Method showIDs = NEIConfig.getDeclaredMethod("showIDs", new Class[]{});
				Object flagObject = showIDs.invoke(null, new Object[]{});
				boolean flag = Boolean.valueOf((Boolean)flagObject);
				
				List<String> list = (List<String>) itemDisplayNameMultiline.invoke(null, new Object[]{tooltip[2],flag,true});
				
				if((Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL)) && (tooltip.length < 4 || Boolean.valueOf((Boolean)tooltip[3]))) {
					list.add(1, "\u00a77" + ((ItemStack)tooltip[2]).stackSize);	
				}
				
				drawToolTip(((Integer)tooltip[0]).intValue(), ((Integer)tooltip[1]).intValue(),list,((ItemStack)tooltip[2]).getRarity().rarityColor, forceminecraft);
			} catch(Exception e) {
				try {
					//Use minecraft vanilla code
					ItemStack var22 = (ItemStack) tooltip[2];
					List var24 = var22.getItemNameandInformation();

	                if((Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL)) && (tooltip.length < 4 || Boolean.valueOf((Boolean)tooltip[3]))) {
	    				var24.add(1, "\u00a77" + ((ItemStack)tooltip[2]).stackSize);	
					}
	                
	                int var11 = ((Integer)tooltip[0]).intValue() - guiLeft + 12;
	                int var12 = ((Integer)tooltip[1]).intValue() - guiTop - 12;
	                drawToolTip(var11, var12,var24,var22.getRarity().rarityColor, forceminecraft);
	            }
	            catch(Exception e1) {}
			}
		}
	}
	
	public static void drawToolTip(int posX, int posY, List<String> msg, int color, boolean forceminecraft) {
		try {
			if(forceminecraft) {
				throw new Exception();
			}
			
			//Look for NEI
			Class<?> LayoutManager = Class.forName("codechicken.nei.LayoutManager");
			Field GuiManagerField = LayoutManager.getDeclaredField("gui");
			GuiManagerField.setAccessible(true);
			Object GuiManagerObject = GuiManagerField.get(null);
			Class<?> GuiManager = Class.forName("codechicken.nei.GuiManager");
			Method drawMultilineTip = GuiManager.getDeclaredMethod("drawMultilineTip", new Class[]{int.class, int.class, List.class, int.class});
			
			drawMultilineTip.invoke(GuiManagerObject, new Object[]{posX, posY, msg, color});
		} catch(Exception e) {
			try {
				//Use minecraft vanilla code
				List var24 = msg;
				
	            if (var24.size() > 0)
	            {
	                int var10 = 0;
	                int var11;
	                int var12;
	                
	                for (var11 = 0; var11 < var24.size(); ++var11)
	                {
	                    var12 = ModLoader.getMinecraftInstance().fontRenderer.getStringWidth((String)var24.get(var11));

	                    if (var12 > var10)
	                    {
	                        var10 = var12;
	                    }
	                }

	                var11 = posX + 12;
	                var12 = posY - 12;
	                int var14 = 8;

	                if (var24.size() > 1)
	                {
	                    var14 += 2 + (var24.size() - 1) * 10;
	                }

	                GL11.glDisable(2896 /*GL_LIGHTING*/);
        			GL11.glDisable(2929 /*GL_DEPTH_TEST*/);	
        			zLevel = 300.0F;
	                int var15 = -267386864;
	                drawGradientRect(var11 - 3, var12 - 4, var11 + var10 + 3, var12 - 3, var15, var15);
	                drawGradientRect(var11 - 3, var12 + var14 + 3, var11 + var10 + 3, var12 + var14 + 4, var15, var15);
	                drawGradientRect(var11 - 3, var12 - 3, var11 + var10 + 3, var12 + var14 + 3, var15, var15);
	                drawGradientRect(var11 - 4, var12 - 3, var11 - 3, var12 + var14 + 3, var15, var15);
	                drawGradientRect(var11 + var10 + 3, var12 - 3, var11 + var10 + 4, var12 + var14 + 3, var15, var15);
	                int var16 = 1347420415;
	                int var17 = (var16 & 16711422) >> 1 | var16 & -16777216;
	                drawGradientRect(var11 - 3, var12 - 3 + 1, var11 - 3 + 1, var12 + var14 + 3 - 1, var16, var17);
	                drawGradientRect(var11 + var10 + 2, var12 - 3 + 1, var11 + var10 + 3, var12 + var14 + 3 - 1, var16, var17);
	                drawGradientRect(var11 - 3, var12 - 3, var11 + var10 + 3, var12 - 3 + 1, var16, var16);
	                drawGradientRect(var11 - 3, var12 + var14 + 2, var11 + var10 + 3, var12 + var14 + 3, var17, var17);

	                for (int var18 = 0; var18 < var24.size(); ++var18)
	                {
	                    String var19 = (String)var24.get(var18);

	                    if (var18 == 0)
	                    {
	                        var19 = "\u00a7" + Integer.toHexString(color) + var19;
	                    }
	                    else
	                    {
	                        var19 = "\u00a77" + var19;
	                    }

	                    ModLoader.getMinecraftInstance().fontRenderer.drawStringWithShadow(var19, var11, var12, -1);
	        	        
	                    if (var18 == 0)
	                    {
	                        var12 += 2;
	                    }

	                    var12 += 10;
	                }

	                zLevel = 0.0F;
	                
	                GL11.glEnable(2929 /*GL_DEPTH_TEST*/);
        			GL11.glEnable(2896 /*GL_LIGHTING*/);
	            }
            }
            catch(Exception e1) {}
		}
	}
	
    private static void drawGradientRect(int par1, int par2, int par3, int par4, int par5, int par6)
    {
        float var7 = (float)(par5 >> 24 & 255) / 255.0F;
        float var8 = (float)(par5 >> 16 & 255) / 255.0F;
        float var9 = (float)(par5 >> 8 & 255) / 255.0F;
        float var10 = (float)(par5 & 255) / 255.0F;
        float var11 = (float)(par6 >> 24 & 255) / 255.0F;
        float var12 = (float)(par6 >> 16 & 255) / 255.0F;
        float var13 = (float)(par6 >> 8 & 255) / 255.0F;
        float var14 = (float)(par6 & 255) / 255.0F;
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glShadeModel(GL11.GL_SMOOTH);
        Tessellator var15 = Tessellator.instance;
        var15.startDrawingQuads();
        var15.setColorRGBA_F(var8, var9, var10, var7);
        var15.addVertex((double)par3, (double)par2, (double)zLevel);
        var15.addVertex((double)par1, (double)par2, (double)zLevel);
        var15.setColorRGBA_F(var12, var13, var14, var11);
        var15.addVertex((double)par1, (double)par4, (double)zLevel);
        var15.addVertex((double)par3, (double)par4, (double)zLevel);
        var15.draw();
        GL11.glShadeModel(GL11.GL_FLAT);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }

    public static void drawSlotBackground(Minecraft mc, int x, int y) {
		int i = mc.renderEngine.getTexture("/logisticspipes/gui/slot.png");
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(i);
		
		Tessellator var9 = Tessellator.instance;
        var9.startDrawingQuads();
        var9.addVertexWithUV(x		, y + 18	, (double)zLevel, 0	, 1);
        var9.addVertexWithUV(x + 18	, y + 18	, (double)zLevel, 1	, 1);
        var9.addVertexWithUV(x + 18	, y			, (double)zLevel, 1	, 0);
        var9.addVertexWithUV(x		, y			, (double)zLevel, 0	, 0);
        var9.draw();
    }
    

    public static void drawSmallSlotBackground(Minecraft mc, int x, int y) {
		int i = mc.renderEngine.getTexture("/logisticspipes/gui/slot-small.png");
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(i);
		
		Tessellator var9 = Tessellator.instance;
        var9.startDrawingQuads();
        var9.addVertexWithUV(x		, y + 8		, (double)zLevel, 0	, 1);
        var9.addVertexWithUV(x + 8	, y + 8		, (double)zLevel, 1	, 1);
        var9.addVertexWithUV(x + 8	, y			, (double)zLevel, 1	, 0);
        var9.addVertexWithUV(x		, y			, (double)zLevel, 0	, 0);
        var9.draw();
    }
    
    public static void renderIconAt(Minecraft mc, int x, int y, float zLevel, int iconIndex, String textureFile) {
		int i = mc.renderEngine.getTexture(textureFile);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(i);
		
		double iconX = ((double)((iconIndex % 16) * 16)) / 256;
		double iconXright = ((double)(((iconIndex % 16) + 1) * 16)) / 256;
		double iconY = ((double)((iconIndex / 16) * 16)) / 256;
		double iconYbottom = ((double)(((iconIndex / 16) + 1) * 16)) / 256;
		
		Tessellator var9 = Tessellator.instance;
        var9.startDrawingQuads();
        var9.addVertexWithUV(x		, y + 16	, (double)zLevel, iconX			, iconYbottom);
        var9.addVertexWithUV(x + 16	, y + 16	, (double)zLevel, iconXright	, iconYbottom);
        var9.addVertexWithUV(x + 16	, y			, (double)zLevel, iconXright	, iconY);
        var9.addVertexWithUV(x		, y			, (double)zLevel, iconX			, iconY);
        var9.draw();
	}
    
    public static void renderForestryBeeAt(Minecraft mc, int x, int y, float zLevel, int id) {
		int tex = mc.renderEngine.getTexture("/gfx/forestry/items/bees.png");
		//GL11.glDisable(2896 /*Light*/);
		mc.renderEngine.bindTexture(tex);
		
		for (int i = 0; i < SimpleServiceLocator.forestryProxy.getRenderPassesForAlleleId(id); i++) {
			int iconIndex = SimpleServiceLocator.forestryProxy.getIconIndexForAlleleId(id, i);
	        int color = SimpleServiceLocator.forestryProxy.getColorForAlleleId(id, i);
	        float colorR = (color >> 16 & 0xFF) / 255.0F;
	        float colorG = (color >> 8 & 0xFF) / 255.0F;
	        float colorB = (color & 0xFF) / 255.0F;

	        GL11.glColor4f(colorR, colorG, colorB, 1.0F);
			//Render Icon
			double iconX = ((double)((iconIndex % 16) * 16)) / 256;
			double iconXright = ((double)(((iconIndex % 16) + 1) * 16)) / 256;
			double iconY = ((double)((iconIndex / 16) * 16)) / 256;
			double iconYbottom = ((double)(((iconIndex / 16) + 1) * 16)) / 256;
			
			Tessellator var9 = Tessellator.instance;
	        var9.startDrawingQuads();
	        var9.addVertexWithUV(x		, y + 16	, (double)zLevel, iconX			, iconYbottom);
	        var9.addVertexWithUV(x + 16	, y + 16	, (double)zLevel, iconXright	, iconYbottom);
	        var9.addVertexWithUV(x + 16	, y			, (double)zLevel, iconXright	, iconY);
	        var9.addVertexWithUV(x		, y			, (double)zLevel, iconX			, iconY);
	        var9.draw();
	      }
	}
    
	public static void drawGuiBackGround(Minecraft mc, int guiLeft, int guiTop, int right, int bottom, float zLevel){

		int i = mc.renderEngine.getTexture("/logisticspipes/gui/GuiBackground.png");
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(i);

		//Top Side
		Tessellator var9 = Tessellator.instance;
        var9.startDrawingQuads();
        var9.addVertexWithUV(guiLeft + 5	, guiTop + 15	, (double)zLevel, 0.33	, 0.33);
        var9.addVertexWithUV(right - 5		, guiTop + 15	, (double)zLevel, 0.66	, 0.33);
        var9.addVertexWithUV(right - 5		, guiTop		, (double)zLevel, 0.66	, 0);
        var9.addVertexWithUV(guiLeft + 5	, guiTop		, (double)zLevel, 0.33	, 0);
        var9.draw();

        //Left Side
        var9 = Tessellator.instance;
        var9.startDrawingQuads();
        var9.addVertexWithUV(guiLeft		, bottom -5		, (double)zLevel, 0	, 0.66);
        var9.addVertexWithUV(guiLeft + 15	, bottom - 5	, (double)zLevel, 0.33	, 0.66);
        var9.addVertexWithUV(guiLeft + 15	, guiTop + 5	, (double)zLevel, 0.33	, 0.33);
        var9.addVertexWithUV(guiLeft		, guiTop + 5	, (double)zLevel, 0	, 0.33);
        var9.draw();

        //Bottom Side
        var9 = Tessellator.instance;
        var9.startDrawingQuads();
        var9.addVertexWithUV(guiLeft + 5	, bottom		, (double)zLevel, 0.33	, 1);
        var9.addVertexWithUV(right - 5		, bottom		, (double)zLevel, 0.66	, 1);
        var9.addVertexWithUV(right - 5		, bottom - 15	, (double)zLevel, 0.66	, 0.66);
        var9.addVertexWithUV(guiLeft + 5	, bottom - 15	, (double)zLevel, 0.33	, 0.66);
        var9.draw();

        //Right Side
        var9 = Tessellator.instance;
        var9.startDrawingQuads();
        var9.addVertexWithUV(right - 15	, bottom - 5		, (double)zLevel, 0.66	, 0.66);
        var9.addVertexWithUV(right		, bottom - 5		, (double)zLevel, 1	, 0.66);
        var9.addVertexWithUV(right		, guiTop + 5		, (double)zLevel, 1	, 0.33);
        var9.addVertexWithUV(right - 15	, guiTop + 5		, (double)zLevel, 0.66	, 0.33);
        var9.draw();
		
		//Top Left
		var9 = Tessellator.instance;
        var9.startDrawingQuads();
        var9.addVertexWithUV(guiLeft		, guiTop + 15	, (double)zLevel, 0	, 0.33);
        var9.addVertexWithUV(guiLeft + 15	, guiTop + 15	, (double)zLevel, 0.33	, 0.33);
        var9.addVertexWithUV(guiLeft + 15	, guiTop		, (double)zLevel, 0.33	, 0);
        var9.addVertexWithUV(guiLeft		, guiTop		, (double)zLevel, 0	, 0);
        var9.draw();
        
        //Bottom Left
        var9 = Tessellator.instance;
        var9.startDrawingQuads();
        var9.addVertexWithUV(guiLeft		, bottom		, (double)zLevel, 0	, 1);
        var9.addVertexWithUV(guiLeft + 15	, bottom		, (double)zLevel, 0.33	, 1);
        var9.addVertexWithUV(guiLeft + 15	, bottom - 15	, (double)zLevel, 0.33	, 0.66);
        var9.addVertexWithUV(guiLeft		, bottom - 15	, (double)zLevel, 0	, 0.66);
        var9.draw();

        //Bottom Right
        var9 = Tessellator.instance;
        var9.startDrawingQuads();
        var9.addVertexWithUV(right - 15	, bottom			, (double)zLevel, 0.66	, 1);
        var9.addVertexWithUV(right		, bottom			, (double)zLevel, 1	, 1);
        var9.addVertexWithUV(right		, bottom - 15		, (double)zLevel, 1	, 0.66);
        var9.addVertexWithUV(right - 15	, bottom - 15		, (double)zLevel, 0.66	, 0.66);
        var9.draw();

        //Top Right
        var9 = Tessellator.instance;
        var9.startDrawingQuads();
        var9.addVertexWithUV(right - 15	, guiTop + 15			, (double)zLevel, 0.66	, 0.33);
        var9.addVertexWithUV(right		, guiTop + 15			, (double)zLevel, 1	, 0.33);
        var9.addVertexWithUV(right		, guiTop				, (double)zLevel, 1	, 0);
        var9.addVertexWithUV(right - 15	, guiTop				, (double)zLevel, 0.66	, 0);
        var9.draw();

        //Center
        var9 = Tessellator.instance;
        var9.startDrawingQuads();
        var9.addVertexWithUV(guiLeft + 15	, bottom - 15		, (double)zLevel, 0.33	, 0.66);
        var9.addVertexWithUV(right - 15		, bottom - 15		, (double)zLevel, 0.66	, 0.66);
        var9.addVertexWithUV(right - 15		, guiTop + 15		, (double)zLevel, 0.66	, 0.33);
        var9.addVertexWithUV(guiLeft + 15	, guiTop + 15		, (double)zLevel, 0.33	, 0.33);
        var9.draw();
		
	}
}
