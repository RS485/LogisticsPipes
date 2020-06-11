package logisticspipes.gui.popup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentString;

import logisticspipes.request.resources.IResource;
import logisticspipes.request.resources.IResource.ColorCode;
import logisticspipes.utils.gui.GuiGraphics;
import logisticspipes.utils.gui.SubGuiScreen;
import logisticspipes.utils.item.ItemIdentifierStack;
import logisticspipes.utils.string.StringUtils;

public class GuiRequestPopup extends SubGuiScreen {

	private String[] text;
	private int mWidth = 0;
	private EntityPlayer player;

	public GuiRequestPopup(EntityPlayer player, Object... message) {
		super(200, (message.length * 10) + 40, 0, 0);
		List<String> textArray = new ArrayList<>();
		for (Object o : message) {
			if (o instanceof String) {
				textArray.add((String) o);
			} else if (o instanceof Collection<?>) {
				for (Object oTwo : (Collection<?>) o) {
					if (oTwo instanceof ItemIdentifierStack) {
						textArray.add(((ItemIdentifierStack) oTwo).getFriendlyName());
					}
					if (oTwo instanceof IResource) {
						textArray.add(((IResource) oTwo).getDisplayText(ColorCode.NONE));
					}
				}
			} else {
				textArray.add(o.toString());
			}
		}
		text = textArray.toArray(new String[] {});
		ySize = (text.length * 10) + 40;
		this.player = player;
	}

	@Override
	public void initGui() {
		super.initGui();
		buttonList.clear();
		buttonList.add(new GuiButton(0, xCenter - 55, bottom - 25, 50, 20, "OK"));
		buttonList.add(new GuiButton(1, xCenter + 5, bottom - 25, 50, 20, "Log"));
	}

	@Override
	protected void renderGuiBackground(int mouseX, int mouseY) {
		if (mWidth == 0) {
			int lWidth = 0;
			for (String msg : text) {
				int tWidth = mc.fontRenderer.getStringWidth(msg);
				if (tWidth > lWidth) {
					lWidth = tWidth;
				}
			}
			xSize = mWidth = Math.max(Math.min(lWidth + 20, 400), 120);
			super.initGui();
		}
		GuiGraphics.drawGuiBackGround(mc, guiLeft, guiTop, right, bottom, zLevel, true);
		for (int i = 0; i < text.length; i++) {
			if (text[i] == null) {
				continue;
			}
			String msg = StringUtils.getCuttedString(text[i], mWidth - 10, fontRenderer);
			int stringWidth = mc.fontRenderer.getStringWidth(msg);
			mc.fontRenderer.drawString(msg, xCenter - (stringWidth / 2), guiTop + 10 + (i * 10), 0x404040);
		}
	}

	@Override
	protected void actionPerformed(GuiButton guibutton) {
		switch (guibutton.id) {
			case 0:
				super.exitGui();
				break;
			case 1:
				for (String msg : text) {
					player.sendMessage(new TextComponentString(msg));
				}
				buttonList.get(1).enabled = false;
				break;
		}
	}
}
