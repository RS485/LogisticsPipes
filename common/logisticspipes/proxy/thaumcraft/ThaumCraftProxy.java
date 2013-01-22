package logisticspipes.proxy.thaumcraft;

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
	
	@Override
	public void renderAspectsDown(ItemStack item, int x, int y, GuiScreen gui) {
		if(!MainProxy.isClient()) return;
		GL11.glPushMatrix();
		Minecraft mc = FMLClientHandler.instance().getClient();
		ObjectTags tags = ThaumcraftApiHelper.getObjectTags(item);
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

}
