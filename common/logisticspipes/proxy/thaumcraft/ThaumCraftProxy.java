package logisticspipes.proxy.thaumcraft;

import java.util.List;

import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.interfaces.IThaumCraftProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;

import org.lwjgl.opengl.GL11;

import thaumcraft.api.EnumTag;
import thaumcraft.api.ObjectTags;
import thaumcraft.api.ThaumcraftApiHelper;
import thaumcraft.client.UtilsFX;
import cpw.mods.fml.client.FMLClientHandler;

public class ThaumCraftProxy implements IThaumCraftProxy {
	
	/**
	 * Renders the aspect icons for a given stack downwards starting at x, y.
	 * @param x The x coord of the screen.
	 * @param y The y coord of the screen.
	 * @param item The ItemStack to render aspects for.
	 * @param gui The Gui screen to render on.
	 */
	@Override
	public void renderAspectsDown(ItemStack item, int x, int y, GuiScreen gui) {
		if(!MainProxy.isClient()) return;
		GL11.glPushMatrix();
		Minecraft mc = FMLClientHandler.instance().getClient();
		ObjectTags tags = getTagsForStack(item);
		tags = ThaumcraftApiHelper.getBonusObjectTags(item, tags);
		if (tags != null) {
			int index = 0;
			for (EnumTag tag : tags.getAspectsSorted()) {
				int yPos = y + index * 18;
				UtilsFX.drawTag(mc, x, yPos, tag, tags.getAmount(tag), gui, true, false);
				index++;
			}
		}
		GL11.glPopMatrix();
	}

	/**
	 * Used to get an ObjectTags of all aspects inside a given stack.
	 * @param stack The stack to get ObjectTags for.
	 * @return ObjectTags containing all of the aspects for stack.
	 */
	@Override
	public ObjectTags getTagsForStack(ItemStack stack) {
		if (stack == null) return new ObjectTags();
		ObjectTags ot = ThaumcraftApiHelper.getObjectTags(stack);
		ot = ThaumcraftApiHelper.getBonusObjectTags(stack, ot);
		return ot;
	}

	/**
	 * Used to render a icon of an aspect at a give x and y on top of a given GuiScreen.
	 * @param etag The EnumTag (aspect) to render
	 * @param x
	 * @param y
	 * @param gui The gui to render on.
	 */
	@Override
	public void renderAspectAt(EnumTag etag, int x, int y, GuiScreen gui) {
		if(!MainProxy.isClient()) return;
		if (etag == null) return;
		GL11.glPushMatrix();
		Minecraft mc = FMLClientHandler.instance().getClient();
		UtilsFX.drawTag(mc, x, y, etag, 1, gui, true, false);
		GL11.glPopMatrix();
	}


	/**
	 * Used to render a rectangle of different aspects. Algorithm does top row 
	 * then the next row down, and so on. It will stop when it runs out of 
	 * aspects to render.
	 * @param etag A list of aspects in EnumTag to render.
	 * @param x Starting coordinate, top left.
	 * @param y Starting coordinate, top left.
	 * @param legnth Int of number of columns.
	 * @param width Int of number of rows.
	 * @param gui The GuiScreen to render on.
	 */
	@Override
	public void renderAspectsInGrid(List<EnumTag> etag, int x, int y, int legnth, int width, GuiScreen gui) {
		if(!MainProxy.isClient()) return;
		if (etag.size() == 0) return;
		int xshift = x;
		int yshift = y;
		int currentTag = 0;
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < legnth; j++) {
				renderAspectAt(etag.get(currentTag), xshift, yshift, gui);
				currentTag += 1;
				if(currentTag == etag.size()) return;
				xshift += 18;
			}
			xshift = x;
			yshift += 18;
		}
	}
	
}
