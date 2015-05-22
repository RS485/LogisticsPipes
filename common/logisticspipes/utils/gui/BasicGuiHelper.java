package logisticspipes.utils.gui;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import cpw.mods.fml.client.FMLClientHandler;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.utils.gui.LogisticsBaseGuiScreen.Colors;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.item.ItemIdentifierStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraftforge.client.ForgeHooksClient;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

public class BasicGuiHelper {

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
		renderItemIdentifierStackListIntoGui(_allItems, IItemSearch, page, left, top, columns, items, xSize, ySize, mc, displayAmount, forcenumber, color, false);
	}

	public static void renderItemIdentifierStackListIntoGui(List<ItemIdentifierStack> _allItems, IItemSearch IItemSearch, int page, int left, int top, int columns, int items, int xSize, int ySize, Minecraft mc, boolean displayAmount, boolean forcenumber, boolean color, boolean disableEffect) {
		GL11.glPushMatrix();
		int ppi = 0;
		int column = 0;
		int row = 0;
		FontRenderer fontRenderer = mc.fontRenderer;
		RenderItem renderItem = new RenderItem();
		RenderBlocks renderBlocks = new RenderBlocks();
		renderItem.renderWithColor = color;
		for(ItemIdentifierStack itemStack: _allItems) {
			if(itemStack == null) {
				column++;
				if(column >= columns) {
					row++;
					column = 0;
				}
				ppi++;
				continue;
			}
			ItemIdentifier item = itemStack.getItem();
			if(IItemSearch != null && !IItemSearch.itemSearched(item)) continue;
			ppi++;
			
			if(ppi <= items * page) continue;
			if(ppi > items * (page + 1)) continue;
			ItemStack st = itemStack.unsafeMakeNormalStack();
			int x = left + xSize * column;
			int y = top + ySize * row;
			
			GL11.glDisable(GL11.GL_LIGHTING);
			
			if(st != null) {
				if(disableEffect) {
					if(st != null) {
						if( !ForgeHooksClient.renderInventoryItem(renderBlocks, mc.renderEngine, st, renderItem.renderWithColor, renderItem.zLevel, x, y)) {
							renderItem.renderItemIntoGUI(fontRenderer, mc.renderEngine, st, x, y);
						}
					}
				} else {
					GL11.glTranslated(0, 0, 3.0);
					renderItem.renderItemAndEffectIntoGUI(fontRenderer, mc.renderEngine, st, x, y);
					GL11.glTranslated(0, 0, -3.0);
				}
			}
			
			GL11.glEnable(GL11.GL_LIGHTING);
			
			if(displayAmount) {
				String s;
				if(st.stackSize == 1 && !forcenumber) {
					s = "";
				} else if(st.stackSize < 1000) {
					s = st.stackSize + "";
				} else if(st.stackSize < 100000) {
					s = st.stackSize / 1000 + "K";
				} else if(st.stackSize < 1000000) {
					s = "0M" + st.stackSize / 100000;
				} else {
					s = st.stackSize / 1000000 + "M";
				}
				
				GL11.glDisable(GL11.GL_LIGHTING);
				GL11.glTranslated(0.0D, 0.0D, 100.0D);
				SimpleGraphics.drawStringWithTranslatedShadow(fontRenderer, s, x + 16 - fontRenderer.getStringWidth(s), y + 8, 0xFFFFFF);
				GL11.glTranslated(0.0D, 0.0D, -100.0D);
				GL11.glEnable(GL11.GL_LIGHTING);
			}
			
			column++;
			if(column >= columns) {
				row++;
				column = 0;
			}
		}
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glPopMatrix();
	}
    
	private static float zLevel;
	
	public static void displayItemToolTip(Object[] tooltip, Gui gui, float pzLevel, int guiLeft, int guiTop) {
		displayItemToolTip(tooltip, pzLevel, guiLeft, guiTop, false, false);
	}
	
	@SuppressWarnings("unchecked")
	public static void displayItemToolTip(Object[] tooltip, float pzLevel, int guiLeft, int guiTop, boolean forceminecraft, boolean forceAdd) {		
		zLevel = pzLevel;
		if(tooltip != null) {
				try {
					//Use minecraft vanilla code
					Minecraft mc = FMLClientHandler.instance().getClient();
					ItemStack var22 = (ItemStack) tooltip[2];
					
					List<String> var24 = var22.getTooltip(mc.thePlayer, mc.gameSettings.advancedItemTooltips);

	                if(tooltip.length > 4) {
	                	var24.addAll(1, (List<String>) tooltip[4]);
					}
	                
	                if((Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL)) && (tooltip.length < 4 || Boolean.valueOf((Boolean)tooltip[3]))) {
	    				var24.add(1, "\u00a77" + ((ItemStack)tooltip[2]).stackSize);
					}
	                
	                int var11 = ((Integer)tooltip[0]).intValue() - (forceAdd ? 0 : guiLeft) + 12;
	                int var12 = ((Integer)tooltip[1]).intValue() - (forceAdd ? 0 : guiTop) - 12;
	                drawToolTip(var11, var12, var24, var22.getRarity().rarityColor, forceminecraft);
	            }
	            catch(Exception e1) {}
		}
		zLevel = 0;
	}
	
	public static void drawToolTip(int posX, int posY, List<String> msg, EnumChatFormatting rarityColor, boolean forceminecraft) {
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
			
			drawMultilineTip.invoke(GuiManagerObject, new Object[]{posX, posY, msg, rarityColor});
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

					SimpleGraphics.drawGradientRect(var11 - 3, var12 - 4, var11 + var10 + 3, var12 - 3, var15, var15, 0.0);
					SimpleGraphics.drawGradientRect(var11 - 3, var12 + var14 + 3, var11 + var10 + 3, var12 + var14 + 4, var15, var15, 0.0);
					SimpleGraphics.drawGradientRect(var11 - 3, var12 - 3, var11 + var10 + 3, var12 + var14 + 3, var15, var15, 0.0);
					SimpleGraphics.drawGradientRect(var11 - 4, var12 - 3, var11 - 3, var12 + var14 + 3, var15, var15, 0.0);
					SimpleGraphics.drawGradientRect(var11 + var10 + 3, var12 - 3, var11 + var10 + 4, var12 + var14 + 3, var15, var15, 0.0);
					int var16 = 1347420415;
	                int var17 = (var16 & 16711422) >> 1 | var16 & -16777216;
					SimpleGraphics.drawGradientRect(var11 - 3, var12 - 3 + 1, var11 - 3 + 1, var12 + var14 + 3 - 1, var16, var17, 0.0);
					SimpleGraphics.drawGradientRect(var11 + var10 + 2, var12 - 3 + 1, var11 + var10 + 3, var12 + var14 + 3 - 1, var16, var17, 0.0);
					SimpleGraphics.drawGradientRect(var11 - 3, var12 - 3, var11 + var10 + 3, var12 - 3 + 1, var16, var16, 0.0);
					SimpleGraphics.drawGradientRect(var11 - 3, var12 + var14 + 2, var11 + var10 + 3, var12 + var14 + 3, var17, var17, 0.0);

					for (int var18 = 0; var18 < var24.size(); ++var18)
	                {
	                    String var19 = var24.get(var18);

	                    if (var18 == 0)
	                    {
	                        var19 = "\u00a7" + rarityColor.getFormattingCode() + var19;
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

	public static void drawPlayerArmorBackground(Minecraft mc, int xOffset, int yOffset) {
		//Player "armor"
        for(int i1 = 0; i1 < 4; i1++) {
        	drawSlotBackground(mc, xOffset - 1, yOffset - 1 - i1 * 18);
        }
	}

    public static void drawSlotBackground(Minecraft mc, int x, int y) {
    	zLevel = 0;
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(GuiGraphics.SLOT_TEXTURE);
		
		Tessellator var9 = Tessellator.instance;
        var9.startDrawingQuads();
        var9.addVertexWithUV(x		, y + 18	, zLevel, 0	, 1);
        var9.addVertexWithUV(x + 18	, y + 18	, zLevel, 1	, 1);
        var9.addVertexWithUV(x + 18	, y			, zLevel, 1	, 0);
        var9.addVertexWithUV(x		, y			, zLevel, 0	, 0);
        var9.draw();
    }
	
	public static void drawSlotBackground(Minecraft mc, int x, int y, int color) {
		zLevel = 0;
		float colorA = (color >> 24 & 0xFF) / 255.0F;
		float colorR = (color >> 16 & 0xFF) / 255.0F;
		float colorG = (color >> 8 & 0xFF) / 255.0F;
		float colorB = (color & 0xFF) / 255.0F;
		GL11.glColor4f(colorR, colorG, colorB, colorA);
		mc.renderEngine.bindTexture(GuiGraphics.SLOT_TEXTURE);

		Tessellator var9 = Tessellator.instance;
		var9.startDrawingQuads();
		var9.addVertexWithUV(x		, y + 18	, zLevel, 0	, 1);
		var9.addVertexWithUV(x + 18	, y + 18	, zLevel, 1	, 1);
		var9.addVertexWithUV(x + 18	, y			, zLevel, 1	, 0);
		var9.addVertexWithUV(x		, y			, zLevel, 0	, 0);
		var9.draw();

		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    }

	public static void drawBigSlotBackground(Minecraft mc, int x, int y) {
    	zLevel = 0;
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(GuiGraphics.BIG_SLOT_TEXTURE);
		
		Tessellator var9 = Tessellator.instance;
        var9.startDrawingQuads();
        var9.addVertexWithUV(x		, y + 26	, zLevel, 0	, 1);
        var9.addVertexWithUV(x + 26	, y + 26	, zLevel, 1	, 1);
        var9.addVertexWithUV(x + 26	, y			, zLevel, 1	, 0);
        var9.addVertexWithUV(x		, y			, zLevel, 0	, 0);
        var9.draw();
    }

	public static void drawSmallSlotBackground(Minecraft mc, int x, int y) {
    	zLevel = 0;
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(GuiGraphics.SMALL_SLOT_TEXTURE);
		
		Tessellator var9 = Tessellator.instance;
        var9.startDrawingQuads();
        var9.addVertexWithUV(x		, y + 8		, zLevel, 0	, 1);
        var9.addVertexWithUV(x + 8	, y + 8		, zLevel, 1	, 1);
        var9.addVertexWithUV(x + 8	, y			, zLevel, 1	, 0);
        var9.addVertexWithUV(x		, y			, zLevel, 0	, 0);
        var9.draw();
    }

	public static void renderIconAt(Minecraft mc, int x, int y, float zLevel, IIcon icon) {
    	if(icon == null) return;
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    	mc.renderEngine.bindTexture(TextureMap.locationItemsTexture);
		
		Tessellator var9 = Tessellator.instance;
        var9.startDrawingQuads();
        var9.addVertexWithUV(x		, y + 16	, zLevel, icon.getMinU()	, icon.getMaxV());
        var9.addVertexWithUV(x + 16	, y + 16	, zLevel, icon.getMaxU()	, icon.getMaxV());
        var9.addVertexWithUV(x + 16	, y			, zLevel, icon.getMaxU()	, icon.getMinV());
        var9.addVertexWithUV(x		, y			, zLevel, icon.getMinU()	, icon.getMinV());
        var9.draw();
	}

	public static void drawLockBackground(Minecraft mc, int x, int y) {
    	zLevel = 0;
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(GuiGraphics.LOCK_ICON);
		GL11.glEnable(GL11.GL_BLEND);
		
		Tessellator var9 = Tessellator.instance;
        var9.startDrawingQuads();
        var9.addVertexWithUV(x		, y + 15	, zLevel, 0	, 1);
        var9.addVertexWithUV(x + 14	, y + 15	, zLevel, 1	, 1);
        var9.addVertexWithUV(x + 14	, y			, zLevel, 1	, 0);
        var9.addVertexWithUV(x		, y			, zLevel, 0	, 0);
        var9.draw();
    }

	public static void drawLinesBackground(Minecraft mc, int x, int y) {
    	zLevel = 0;
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(GuiGraphics.LINES_ICON);
		GL11.glEnable(GL11.GL_BLEND);
		
		Tessellator var9 = Tessellator.instance;
        var9.startDrawingQuads();
        var9.addVertexWithUV(x		, y + 16	, zLevel, 0	, 1);
        var9.addVertexWithUV(x + 16	, y + 16	, zLevel, 1	, 1);
        var9.addVertexWithUV(x + 16	, y			, zLevel, 1	, 0);
        var9.addVertexWithUV(x		, y			, zLevel, 0	, 0);
        var9.draw();
    }

	public static void drawStatsBackground(Minecraft mc, int x, int y) {
    	zLevel = 0;
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(GuiGraphics.STATS_ICON);
		GL11.glEnable(GL11.GL_BLEND);
		
		Tessellator var9 = Tessellator.instance;
        var9.startDrawingQuads();
        var9.addVertexWithUV(x		, y + 16	, zLevel, 0	, 1);
        var9.addVertexWithUV(x + 16	, y + 16	, zLevel, 1	, 1);
        var9.addVertexWithUV(x + 16	, y			, zLevel, 1	, 0);
        var9.addVertexWithUV(x		, y			, zLevel, 0	, 0);
        var9.draw();
    }

    public static void renderForestryBeeAt(Minecraft mc, int x, int y, float zLevel, String id) {
    	GL11.glDisable(GL11.GL_LIGHTING);
    	mc.renderEngine.bindTexture(TextureMap.locationItemsTexture);

		for (int i = 0; i < SimpleServiceLocator.forestryProxy.getRenderPassesForAlleleId(id); i++) {
			IIcon icon = SimpleServiceLocator.forestryProxy.getIconIndexForAlleleId(id, i);
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

    public static void drawGuiBackGround(Minecraft mc, int guiLeft, int guiTop, int right, int bottom, float zLevel, boolean resetColor) {
    	drawGuiBackGround(mc, guiLeft, guiTop, right, bottom, zLevel, resetColor, true, true, true, true);
    }

	public static void drawGuiBackGround(Minecraft mc, int guiLeft, int guiTop, int right, int bottom, float zLevel, boolean resetColor, boolean displayTop, boolean displayLeft, boolean displayBottom, boolean displayRight){
		if(resetColor) {
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		}
		mc.renderEngine.bindTexture(GuiGraphics.BACKGROUND_TEXTURE);
		
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

}
