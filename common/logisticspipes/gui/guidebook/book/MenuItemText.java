package logisticspipes.gui.guidebook.book;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;

import logisticspipes.utils.string.StringUtils;

public class MenuItemText extends MenuItem{

	private String PREFIX;
	public String text;

	public MenuItemText(String name, String parentName, int index) {
		super(name, parentName, index);
		this.PREFIX = parentName + name + ".";
	}

	@Override
	public void loadMenuItem() {
		this.text = StringUtils.translate(PREFIX + "text").trim();
	}

	public void drawMenuItem(Minecraft mc,  int mouseX, int mouseY, int x, int y, int sizeX, int sizeY, boolean text) {
		GlStateManager.color(1.0F, 1.0F, 1.0F);
		int text$size = sizeY/2;
		GlStateManager.pushMatrix();
		GlStateManager.translate(x + 5, ((sizeY - 8) / 2.0) + y, 100);
		GlStateManager.scale(text$size/8,text$size/8,0);
		mc.fontRenderer.drawStringWithShadow(this.text, 0, 0, 0xFFFFFF);
		GlStateManager.popMatrix();
		GlStateManager.color(1.0F, 1.0F, 1.0F);
	}

	@Override
	public EnumMenuItemType getType() {
		return EnumMenuItemType.TEXT;
	}
}
