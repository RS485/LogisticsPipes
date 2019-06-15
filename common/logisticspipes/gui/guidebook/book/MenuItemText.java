package logisticspipes.gui.guidebook.book;

import net.minecraft.client.Minecraft;

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

	@Override
	public void drawMenuItemFrame(int x, int y, int sizeX, int sizeY, Minecraft mc, int mouseX, int mouseY) {
		super.drawMenuItemFrame(x, y, sizeX, sizeY, mc, mouseX, mouseY);
	}
}
