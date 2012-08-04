package net.minecraft.src.krapht.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.src.FontRenderer;
import net.minecraft.src.Tessellator;
import net.minecraft.src.krapht.gui.KraphtBaseGuiScreen.Colors;

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
