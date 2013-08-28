package logisticspipes.utils.gui;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.utils.ItemIdentifier;
import logisticspipes.utils.ItemIdentifierStack;
import logisticspipes.utils.gui.KraphtBaseGuiScreen.Colors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.ForgeHooksClient;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import cpw.mods.fml.client.FMLClientHandler;

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
		renderItemIdentifierStackListIntoGui(_allItems, IItemSearch, page, left, top, columns, items, xSize, ySize, mc, displayAmount, forcenumber, true);
	}

	public static void renderItemIdentifierStackListIntoGui(List<ItemIdentifierStack> _allItems, IItemSearch IItemSearch, int page, int left , int top, int columns, int items, int xSize, int ySize, Minecraft mc, boolean displayAmount, boolean forcenumber, boolean color) {
		renderItemIdentifierStackListIntoGui(_allItems, IItemSearch, page, left, top, columns, items, xSize, ySize, mc, displayAmount, forcenumber, true, false);
	}

	public static void renderItemIdentifierStackListIntoGui(List<ItemIdentifierStack> _allItems, IItemSearch IItemSearch, int page, int left , int top, int columns, int items, int xSize, int ySize, Minecraft mc, boolean displayAmount, boolean forcenumber, boolean color, boolean disableEffect) {
		GL11.glPushMatrix();
		int ppi = 0;
		int column = 0;
		int row = 0;
		FontRenderer fontRenderer = mc.fontRenderer;
		RenderItem renderItem = new RenderItem();
	    RenderBlocks renderBlocks = new RenderBlocks();
		renderItem.renderWithColor = color;
		for(ItemIdentifierStack itemStack : _allItems) {
			if(itemStack == null) {
				column++;
				if (column >= columns){
					row++;
					column = 0;
				}
				ppi++;
				continue;	
			}
			ItemIdentifier item = itemStack.getItem();
			if(IItemSearch!= null && !IItemSearch.itemSearched(item)) continue;
			ppi++;
			
			if (ppi <= items * page) continue;
			if (ppi > items * (page+1)) continue;
			ItemStack st = itemStack.unsafeMakeNormalStack();
			int x = left + xSize * column;
			int y = top + ySize * row;

			GL11.glDisable(2896 /*GL_LIGHTING*/);
			//GL11.glDisable(2929 /*GL_DEPTH_TEST*/);
			
			if(st != null && itemStack.getItem().isValid()) {
				if(disableEffect) {
					if (st != null)
			        {
			            if (!ForgeHooksClient.renderInventoryItem(renderBlocks, mc.renderEngine, st, renderItem.renderWithColor, renderItem.zLevel, x, y))
			            {
			            	renderItem.renderItemIntoGUI(fontRenderer, mc.renderEngine, st, x, y);
			            }
			        }

				} else {
					GL11.glTranslated(0, 0, 3.0);
					renderItem.renderItemAndEffectIntoGUI(fontRenderer, mc.renderEngine, st, x, y);
					GL11.glTranslated(0, 0, -3.0);
				}
			}
			
	        //GL11.glEnable(2929 /*GL_DEPTH_TEST*/);
			GL11.glEnable(2896 /*GL_LIGHTING*/);
			
			if(displayAmount) {
				String s;
				if (st.stackSize == 1 && !forcenumber){
					s = "";
				} else if (st.stackSize < 1000) {
					s = st.stackSize + "";
				} else if (st.stackSize < 100000){
					s = st.stackSize / 1000 + "K";
				} else if (st.stackSize < 1000000){
					s = "0M" + st.stackSize / 100000;
				} else {
					s = st.stackSize / 1000000 + "M";
				}
					
				GL11.glDisable(2896 /*GL_LIGHTING*/);
				GL11.glTranslated(0.0D, 0.0D, 100.0D);
				try {
					drawStringWithShadow(fontRenderer, s, x + 16 - fontRenderer.getStringWidth(s), y + 8, 0xFFFFFF);
					GL11.glTranslated(0.0D, 0.0D, -100.0D);
				} catch (Exception e) {
					GL11.glTranslated(0.0D, 0.0D, -100.0D);
					GL11.glDisable(2929 /*GL_DEPTH_TEST*/);
					fontRenderer.drawStringWithShadow(s, x + 16 - fontRenderer.getStringWidth(s), y + 8, 0xFFFFFF);
					GL11.glEnable(2929 /*GL_DEPTH_TEST*/);
				}
				GL11.glEnable(2896 /*GL_LIGHTING*/);
			}

			column++;
			if (column >= columns){
				row++;
				column = 0;
			}
		}
		GL11.glDisable(2896 /*GL_LIGHTING*/);
		GL11.glPopMatrix();
	}
	

    /**
     * Draws the specified string with a shadow.
     * @throws SecurityException 
     * @throws NoSuchMethodException 
     * @throws InvocationTargetException 
     * @throws IllegalArgumentException 
     * @throws IllegalAccessException 
     * @throws NoSuchFieldException 
     */
    private static int drawStringWithShadow(FontRenderer fontRenderer,String par1Str, int par2, int par3, int par4) throws Exception {
    	Method a = getObfuMethod(fontRenderer.getClass(), "c", "resetStyles");
    	a.setAccessible(true);
    	a.invoke(fontRenderer);

    	Field b = getObfuField(fontRenderer.getClass(), "m", "bidiFlag");
    	b.setAccessible(true);
        if (((Boolean)b.get(fontRenderer)).booleanValue())
        {	
        	Method c = getObfuMethod(fontRenderer.getClass(), "c", "bidiReorder", String.class);
        	c.setAccessible(true);
        	par1Str = (String)c.invoke(fontRenderer, par1Str);
        }
        Method d = getObfuMethod(fontRenderer.getClass(), "b", "renderString", String.class, int.class, int.class, int.class, boolean.class);
        d.setAccessible(true);
        int var5 = ((Integer)d.invoke(fontRenderer, par1Str, par2 + 1, par3 + 1, par4, true)).intValue();

		GL11.glTranslated(0.0D, 0.0D, 1.0D);
        var5 = Math.max(var5, ((Integer)d.invoke(fontRenderer, par1Str, par2, par3, par4, false)).intValue());
		GL11.glTranslated(0.0D, 0.0D, -1.0D);
		
        return var5;
    }
    
    private static Field getObfuField(Class<?> clazz, String name1, String name2) throws SecurityException, NoSuchFieldException {
    	try {
    		return clazz.getDeclaredField(name1);
    	} catch(Exception e) {
    		return clazz.getDeclaredField(name2);
    	}
    }
    
    private static Method getObfuMethod(Class<?> clazz, String name1, String name2, Class<?>... objects) throws NoSuchMethodException, SecurityException {
    	try {
        	return clazz.getDeclaredMethod(name1, objects);
    	} catch(Exception e) {
    		return clazz.getDeclaredMethod(name2, objects);
    	}
    }
    
	private static float zLevel;
	
	public static void displayItemToolTip(Object[] tooltip, Gui gui, float pzLevel, int guiLeft, int guiTop) {
		displayItemToolTip(tooltip, gui, pzLevel, guiLeft, guiTop, false, false);
	}
	
	@SuppressWarnings("unchecked")
	public static void displayItemToolTip(Object[] tooltip, Gui gui, float pzLevel, int guiLeft, int guiTop, boolean forceminecraft, boolean forceAdd) {		
		zLevel = pzLevel;
		if(tooltip != null) {
				try {
					//Use minecraft vanilla code
					Minecraft mc = FMLClientHandler.instance().getClient();
					ItemStack var22 = (ItemStack) tooltip[2];
					List<String> var24 = var22.getTooltip(mc.thePlayer, mc.gameSettings.advancedItemTooltips);

	                if((Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL)) && (tooltip.length < 4 || Boolean.valueOf((Boolean)tooltip[3]))) {
	    				var24.add(1, "\u00a77" + ((ItemStack)tooltip[2]).stackSize);	
					}
	                
	                int var11 = ((Integer)tooltip[0]).intValue() - (forceAdd ? 0 : guiLeft) + 12;
	                int var12 = ((Integer)tooltip[1]).intValue() - (forceAdd ? 0 : guiTop) - 12;
	                drawToolTip(var11, var12,var24,var22.getRarity().rarityColor, forceminecraft);
	            }
	            catch(Exception e1) {}
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
				List<String> var24 = msg;
				
	            if (var24.size() > 0)
	            {
	                int var10 = 0;
	                int var11;
	                int var12;
	                
	                for (var11 = 0; var11 < var24.size(); ++var11)
	                {
	                    var12 = FMLClientHandler.instance().getClient().fontRenderer.getStringWidth(var24.get(var11));

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
	                    String var19 = var24.get(var18);

	                    if (var18 == 0)
	                    {
	                        var19 = "\u00a7" + Integer.toHexString(color) + var19;
	                    }
	                    else
	                    {
	                        var19 = "\u00a77" + var19;
	                    }

	                    FMLClientHandler.instance().getClient().fontRenderer.drawStringWithShadow(var19, var11, var12, -1);
	        	        
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
        float var7 = (par5 >> 24 & 255) / 255.0F;
        float var8 = (par5 >> 16 & 255) / 255.0F;
        float var9 = (par5 >> 8 & 255) / 255.0F;
        float var10 = (par5 & 255) / 255.0F;
        float var11 = (par6 >> 24 & 255) / 255.0F;
        float var12 = (par6 >> 16 & 255) / 255.0F;
        float var13 = (par6 >> 8 & 255) / 255.0F;
        float var14 = (par6 & 255) / 255.0F;
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glShadeModel(GL11.GL_SMOOTH);
        Tessellator var15 = Tessellator.instance;
        var15.startDrawingQuads();
        var15.setColorRGBA_F(var8, var9, var10, var7);
        var15.addVertex(par3, par2, zLevel);
        var15.addVertex(par1, par2, zLevel);
        var15.setColorRGBA_F(var12, var13, var14, var11);
        var15.addVertex(par1, par4, zLevel);
        var15.addVertex(par3, par4, zLevel);
        var15.draw();
        GL11.glShadeModel(GL11.GL_FLAT);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }

    /**
     * Draws a textured rectangle at the stored z-value. Args: x, y, u, v, width, height
     */
    public static void drawTexturedModalRect(double par1, double par2, double par3, double par4, double par5, double par6, float zLevel)
    {
        float var7 = 0.00390625F;
        float var8 = 0.00390625F;
        Tessellator var9 = Tessellator.instance;
        var9.startDrawingQuads();
        var9.addVertexWithUV(par1 + 0, par2 + par6, zLevel, (float)(par3 + 0) * var7, (float)(par4 + par6) * var8);
        var9.addVertexWithUV(par1 + par5, par2 + par6, zLevel, (float)(par3 + par5) * var7, (float)(par4 + par6) * var8);
        var9.addVertexWithUV(par1 + par5, par2 + 0, zLevel, (float)(par3 + par5) * var7, (float)(par4 + 0) * var8);
        var9.addVertexWithUV(par1 + 0, par2 + 0, zLevel, (float)(par3 + 0) * var7, (float)(par4 + 0) * var8);
        var9.draw();
    }
    
    /**
     * Renders the specified text to the screen, center-aligned.
     */
    public static void drawCenteredString(FontRenderer par1FontRenderer, String par2Str, int par3, int par4, int par5)
    {
        par1FontRenderer.drawStringWithShadow(par2Str, par3 - par1FontRenderer.getStringWidth(par2Str) / 2, par4, par5);
    }
    
	public static void drawPlayerInventoryBackground(Minecraft mc, int xOffset, int yOffset) {
		//Player "backpack"
        for(int row = 0; row < 3; row++) {
            for(int column = 0; column < 9; column++)
            {
            	drawSlotBackground(mc, xOffset + column * 18 - 1, yOffset + row * 18 - 1);
            }
        }
        //Player "hotbar"
        for(int i1 = 0; i1 < 9; i1++) {
        	drawSlotBackground(mc, xOffset + i1 * 18 - 1, yOffset + 58 - 1);
        }
	}

	public static void drawPlayerHotbarBackground(Minecraft mc, int xOffset, int yOffset) {
		//Player "hotbar"
        for(int i1 = 0; i1 < 9; i1++) {
        	drawSlotBackground(mc, xOffset + i1 * 18 - 1, yOffset - 1);
        }
	}
	
	private static final ResourceLocation SLOT = new ResourceLocation("logisticspipes", "textures/gui/slot.png");
	
    public static void drawSlotBackground(Minecraft mc, int x, int y) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.func_110577_a(SLOT);
		
		Tessellator var9 = Tessellator.instance;
        var9.startDrawingQuads();
        var9.addVertexWithUV(x		, y + 18	, zLevel, 0	, 1);
        var9.addVertexWithUV(x + 18	, y + 18	, zLevel, 1	, 1);
        var9.addVertexWithUV(x + 18	, y			, zLevel, 1	, 0);
        var9.addVertexWithUV(x		, y			, zLevel, 0	, 0);
        var9.draw();
    }
	
	private static final ResourceLocation BIGSLOT = new ResourceLocation("logisticspipes", "textures/gui/slot-big.png");
	

    public static void drawBigSlotBackground(Minecraft mc, int x, int y) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.func_110577_a(BIGSLOT);
		
		Tessellator var9 = Tessellator.instance;
        var9.startDrawingQuads();
        var9.addVertexWithUV(x		, y + 26	, zLevel, 0	, 1);
        var9.addVertexWithUV(x + 26	, y + 26	, zLevel, 1	, 1);
        var9.addVertexWithUV(x + 26	, y			, zLevel, 1	, 0);
        var9.addVertexWithUV(x		, y			, zLevel, 0	, 0);
        var9.draw();
    }
    
	private static final ResourceLocation SMALLSLOT = new ResourceLocation("logisticspipes", "textures/gui/slot-small.png");
	
    public static void drawSmallSlotBackground(Minecraft mc, int x, int y) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.func_110577_a(SMALLSLOT);
		
		Tessellator var9 = Tessellator.instance;
        var9.startDrawingQuads();
        var9.addVertexWithUV(x		, y + 8		, zLevel, 0	, 1);
        var9.addVertexWithUV(x + 8	, y + 8		, zLevel, 1	, 1);
        var9.addVertexWithUV(x + 8	, y			, zLevel, 1	, 0);
        var9.addVertexWithUV(x		, y			, zLevel, 0	, 0);
        var9.draw();
    }
	
	private static final ResourceLocation ITEMS = new ResourceLocation("textures/gui/icons.png");
	

    public static void renderIconAt(Minecraft mc, int x, int y, float zLevel, Icon icon) {
    	if(icon == null) return;
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    	mc.renderEngine.func_110577_a(ITEMS);
		
		Tessellator var9 = Tessellator.instance;
        var9.startDrawingQuads();
        var9.addVertexWithUV(x		, y + 16	, zLevel, icon.getMinU()	, icon.getMaxV());
        var9.addVertexWithUV(x + 16	, y + 16	, zLevel, icon.getMaxU()	, icon.getMaxV());
        var9.addVertexWithUV(x + 16	, y			, zLevel, icon.getMaxU()	, icon.getMinV());
        var9.addVertexWithUV(x		, y			, zLevel, icon.getMinU()	, icon.getMinV());
        var9.draw();
	}
    
    public static void renderForestryBeeAt(Minecraft mc, int x, int y, float zLevel, String id) {
    	GL11.glDisable(GL11.GL_LIGHTING);
    	mc.renderEngine.func_110577_a(ITEMS);
    	
		for (int i = 0; i < SimpleServiceLocator.forestryProxy.getRenderPassesForAlleleId(id); i++) {
			Icon icon = SimpleServiceLocator.forestryProxy.getIconIndexForAlleleId(id, i);
			if(icon == null) continue;
	        int color = SimpleServiceLocator.forestryProxy.getColorForAlleleId(id, i);
	        float colorR = (color >> 16 & 0xFF) / 255.0F;
	        float colorG = (color >> 8 & 0xFF) / 255.0F;
	        float colorB = (color & 0xFF) / 255.0F;

	        GL11.glColor4f(colorR, colorG, colorB, 1.0F);
			
	        //Render Icon
			Tessellator var9 = Tessellator.instance;
	        var9.startDrawingQuads();
	        var9.addVertexWithUV(x		, y + 16	, zLevel, icon.getMinU()	, icon.getMaxV());
	        var9.addVertexWithUV(x + 16	, y + 16	, zLevel, icon.getMaxU()	, icon.getMaxV());
	        var9.addVertexWithUV(x + 16	, y			, zLevel, icon.getMaxU()	, icon.getMinV());
	        var9.addVertexWithUV(x		, y			, zLevel, icon.getMinU()	, icon.getMinV());
	        var9.draw();
		}
		GL11.glEnable(GL11.GL_LIGHTING);
	}
    
    public static void drawGuiBackGround(Minecraft mc, int guiLeft, int guiTop, int right, int bottom, float zLevel, boolean flag){
    	drawGuiBackGround(mc, guiLeft, guiTop, right, bottom, zLevel, flag, true, true, true, true);
    }
	
	private static final ResourceLocation BACKGROUND = new ResourceLocation("logisticspipes", "textures/gui/GuiBackground.png");
	

    public static void drawGuiBackGround(Minecraft mc, int guiLeft, int guiTop, int right, int bottom, float zLevel, boolean flag, boolean displayTop, boolean displayLeft, boolean displayBottom, boolean displayRight){
		if(flag) {
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		}
		mc.renderEngine.func_110577_a(BACKGROUND);
		
		if(displayTop) {
			//Top Side
			Tessellator var9 = Tessellator.instance;
	        var9.startDrawingQuads();
	        var9.addVertexWithUV(guiLeft + 15	, guiTop + 15	, zLevel, 0.33	, 0.33);
	        var9.addVertexWithUV(right - 15		, guiTop + 15	, zLevel, 0.66	, 0.33);
	        var9.addVertexWithUV(right - 15		, guiTop		, zLevel, 0.66	, 0);
	        var9.addVertexWithUV(guiLeft + 15	, guiTop		, zLevel, 0.33	, 0);
	        var9.draw();
		}

		if(displayLeft) {
			//Left Side
			Tessellator var9 = Tessellator.instance;
	        var9.startDrawingQuads();
	        var9.addVertexWithUV(guiLeft		, bottom - 15	, zLevel, 0	, 0.66);
	        var9.addVertexWithUV(guiLeft + 15	, bottom - 15	, zLevel, 0.33	, 0.66);
	        var9.addVertexWithUV(guiLeft + 15	, guiTop + 15	, zLevel, 0.33	, 0.33);
	        var9.addVertexWithUV(guiLeft		, guiTop + 15	, zLevel, 0	, 0.33);
	        var9.draw();
		}
		
		if(displayBottom) {
	        //Bottom Side
			Tessellator var9 = Tessellator.instance;
	        var9.startDrawingQuads();
	        var9.addVertexWithUV(guiLeft + 15	, bottom		, zLevel, 0.33	, 1);
	        var9.addVertexWithUV(right - 15		, bottom		, zLevel, 0.66	, 1);
	        var9.addVertexWithUV(right - 15		, bottom - 15	, zLevel, 0.66	, 0.66);
	        var9.addVertexWithUV(guiLeft + 15	, bottom - 15	, zLevel, 0.33	, 0.66);
	        var9.draw();
		}
		
		if(displayRight) {
	        //Right Side
			Tessellator var9 = Tessellator.instance;
	        var9.startDrawingQuads();
	        var9.addVertexWithUV(right - 15	, bottom - 15		, zLevel, 0.66	, 0.66);
	        var9.addVertexWithUV(right		, bottom - 15		, zLevel, 1	, 0.66);
	        var9.addVertexWithUV(right		, guiTop + 15		, zLevel, 1	, 0.33);
	        var9.addVertexWithUV(right - 15	, guiTop + 15		, zLevel, 0.66	, 0.33);
	        var9.draw();
		}
		
		if(displayTop && displayLeft) {
			//Top Left
			Tessellator var9 = Tessellator.instance;
	        var9.startDrawingQuads();
	        var9.addVertexWithUV(guiLeft		, guiTop + 15	, zLevel, 0	, 0.33);
	        var9.addVertexWithUV(guiLeft + 15	, guiTop + 15	, zLevel, 0.33	, 0.33);
	        var9.addVertexWithUV(guiLeft + 15	, guiTop		, zLevel, 0.33	, 0);
	        var9.addVertexWithUV(guiLeft		, guiTop		, zLevel, 0	, 0);
	        var9.draw();
		}
        
		if(displayBottom && displayLeft) {
			//Bottom Left
			Tessellator var9 = Tessellator.instance;
	        var9.startDrawingQuads();
	        var9.addVertexWithUV(guiLeft		, bottom		, zLevel, 0	, 1);
	        var9.addVertexWithUV(guiLeft + 15	, bottom		, zLevel, 0.33	, 1);
	        var9.addVertexWithUV(guiLeft + 15	, bottom - 15	, zLevel, 0.33	, 0.66);
	        var9.addVertexWithUV(guiLeft		, bottom - 15	, zLevel, 0	, 0.66);
	        var9.draw();
		}

        if(displayBottom && displayRight) {
    		//Bottom Right
        	Tessellator var9 = Tessellator.instance;
	        var9.startDrawingQuads();
	        var9.addVertexWithUV(right - 15	, bottom			, zLevel, 0.66	, 1);
	        var9.addVertexWithUV(right		, bottom			, zLevel, 1	, 1);
	        var9.addVertexWithUV(right		, bottom - 15		, zLevel, 1	, 0.66);
	        var9.addVertexWithUV(right - 15	, bottom - 15		, zLevel, 0.66	, 0.66);
	        var9.draw();
	    }

	    if(displayTop && displayRight) {
	    	//Top Right
	        Tessellator var9 = Tessellator.instance;
	        var9.startDrawingQuads();
	        var9.addVertexWithUV(right - 15	, guiTop + 15			, zLevel, 0.66	, 0.33);
	        var9.addVertexWithUV(right		, guiTop + 15			, zLevel, 1	, 0.33);
	        var9.addVertexWithUV(right		, guiTop				, zLevel, 1	, 0);
	        var9.addVertexWithUV(right - 15	, guiTop				, zLevel, 0.66	, 0);
	        var9.draw();
	    }

        //Center
	    Tessellator var9 = Tessellator.instance;
        var9.startDrawingQuads();
        var9.addVertexWithUV(guiLeft + 15	, bottom - 15		, zLevel, 0.33	, 0.66);
        var9.addVertexWithUV(right - 15		, bottom - 15		, zLevel, 0.66	, 0.66);
        var9.addVertexWithUV(right - 15		, guiTop + 15		, zLevel, 0.66	, 0.33);
        var9.addVertexWithUV(guiLeft + 15	, guiTop + 15		, zLevel, 0.33	, 0.33);
        var9.draw();
	}
	
    /**
     * Draws a solid color rectangle with the specified coordinates and color.
     */
    public static void drawRect(int par0, int par1, int par2, int par3, int par4)
    {
        int var5;

        if (par0 < par2)
        {
            var5 = par0;
            par0 = par2;
            par2 = var5;
        }

        if (par1 < par3)
        {
            var5 = par1;
            par1 = par3;
            par3 = var5;
        }

        float var10 = (par4 >> 24 & 255) / 255.0F;
        float var6 = (par4 >> 16 & 255) / 255.0F;
        float var7 = (par4 >> 8 & 255) / 255.0F;
        float var8 = (par4 & 255) / 255.0F;
        Tessellator var9 = Tessellator.instance;
        //GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(var6, var7, var8, var10);
        var9.startDrawingQuads();
        var9.addVertex(par0, par3, 0.0D);
        var9.addVertex(par2, par3, 0.0D);
        var9.addVertex(par2, par1, 0.0D);
        var9.addVertex(par0, par1, 0.0D);
        var9.draw();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        //GL11.glDisable(GL11.GL_BLEND);
    }
}
