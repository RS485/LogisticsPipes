package logisticspipes.utils.gui;

import javax.annotation.Nonnull;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

public class GuiCheckBox extends GuiButton {

	private boolean state;

	public GuiCheckBox(int par1, int par2, int par3, int par4, int par5, boolean startState) {
		super(par1, par2, par3, par4, par5, "");
		state = startState;
	}

	/**
	 * Draws this button to the screen.
	 */
	@Override
	public void drawButton(@Nonnull Minecraft minecraft, int par2, int par3, float partial) {
		if (visible) {
			boolean var5 = par2 >= x && par3 >= y && par2 < x + width && par3 < y + height;
			int var6 = getHoverState(var5);
			//GL11.glBindTexture(GL11.GL_TEXTURE_2D, minecraft.renderEngine.getTexture("/logisticspipes/gui/checkbox-" + (state?"on":"out") + "" + (var6 == 2?"-mouse":"") + ".png"));
			minecraft.renderEngine.bindTexture(new ResourceLocation("logisticspipes", "textures/gui/checkbox-" + (state ? "on" : "out") + "" + (var6 == 2 ? "-mouse" : "") + ".png"));
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

			Tessellator tessellator = Tessellator.getInstance();
			BufferBuilder vertexbuffer = tessellator.getBuffer();
			vertexbuffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

			vertexbuffer.pos(x, y + height, zLevel).tex(0, 1).endVertex();
			vertexbuffer.pos(x + width, y + height, zLevel).tex(1, 1).endVertex();
			vertexbuffer.pos(x + width, y, zLevel).tex(1, 0).endVertex();
			vertexbuffer.pos(x, y, zLevel).tex(0, 0).endVertex();

			tessellator.draw();

			mouseDragged(minecraft, par2, par3);
		}
	}

	public boolean change() {
		return state = !state;
	}

	public boolean getState() {
		return state;
	}

	public void setState(boolean flag) {
		state = flag;
	}
}
