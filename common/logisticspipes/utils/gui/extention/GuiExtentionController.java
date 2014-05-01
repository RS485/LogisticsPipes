package logisticspipes.utils.gui.extention;

import java.util.ArrayList;
import java.util.List;

import logisticspipes.utils.gui.BasicGuiHelper;
import lombok.Setter;
import net.minecraft.client.Minecraft;

public class GuiExtentionController {

	private final List<GuiExtention> extentions = new ArrayList<GuiExtention>();
	private final List<GuiExtention> extentionsToRemove = new ArrayList<GuiExtention>();
	@Setter
	private int maxBottom;
	private GuiExtention currentlyExtended = null;
	
	public void render(int xPos, int yPos) {
		yPos += 4;
		if(currentlyExtended == null) {
			for(GuiExtention extention:extentions) {
				extention.setExtending(false);
				int left = xPos - extention.getCurrentWidth();
				int bottom = yPos + extention.getCurrentHeight();
				extention.update(left, yPos);
				BasicGuiHelper.drawGuiBackGround(Minecraft.getMinecraft(), left, yPos, xPos + 15, bottom, 0, true, true, true, true, false);
				extention.renderForground(left, yPos);
				yPos = bottom;
			}
		} else {
			if(currentlyExtended.isExtending()) {
				int left = xPos - currentlyExtended.getCurrentWidth();
				int bottom = currentlyExtended.getCurrentYPos() + currentlyExtended.getCurrentHeight();
				currentlyExtended.update(left, yPos);
				BasicGuiHelper.drawGuiBackGround(Minecraft.getMinecraft(), left, currentlyExtended.getCurrentYPos(), xPos + 15, bottom, 0, true, true, true, true, false);
				currentlyExtended.renderForground(left, currentlyExtended.getCurrentYPos());
			} else {
				for(GuiExtention extention:extentions) {
					if(extention == currentlyExtended) break;
					extention.setExtending(false);
					int bottom = yPos + extention.getCurrentHeight();
					yPos = bottom;
				}
				int left = xPos - currentlyExtended.getCurrentWidth();
				int bottom = currentlyExtended.getCurrentYPos() + currentlyExtended.getCurrentHeight();
				currentlyExtended.update(left, yPos);
				BasicGuiHelper.drawGuiBackGround(Minecraft.getMinecraft(), left, currentlyExtended.getCurrentYPos(), xPos + 15, bottom, 0, true, true, true, true, false);
				currentlyExtended.renderForground(left, currentlyExtended.getCurrentYPos());
				if(currentlyExtended.isFullyRetracted()) {
					currentlyExtended = null;
				}
			}
		}
		if(currentlyExtended != null && extentionsToRemove.contains(currentlyExtended)) {
			currentlyExtended = null;
		}
		extentions.removeAll(extentionsToRemove);
		extentionsToRemove.clear();
	}
	
	public void addExtention(GuiExtention extention) {
		extentions.add(extention);
	}

	public void removeExtention(GuiExtention extention) {
		extentionsToRemove.add(extention);
	}

	public void mouseClicked(int x, int y, int k) {
		if(currentlyExtended == null) {
			for(GuiExtention extention:extentions) {
				if(x > extention.getCurrentXPos() && x < extention.getCurrentXPos() + extention.getCurrentWidth() && y > extention.getCurrentYPos() && y < extention.getCurrentYPos() + extention.getCurrentHeight()) {
					currentlyExtended = extention;
					currentlyExtended.setExtending(true);
				}
			}
		} else {
			if(x > currentlyExtended.getCurrentXPos() && x < currentlyExtended.getCurrentXPos() + currentlyExtended.getCurrentWidth() && y > currentlyExtended.getCurrentYPos() && y < currentlyExtended.getCurrentYPos() + currentlyExtended.getCurrentHeight()) {
				currentlyExtended.setExtending(false);
			}
		}
	}

	public void mouseOver(int i, int j) {
		int x = i;
		int y = j;
		if(currentlyExtended == null) {
			for(GuiExtention extention:extentions) {
				if(x > extention.getCurrentXPos() && x < extention.getCurrentXPos() + extention.getCurrentWidth() && y > extention.getCurrentYPos() && y < extention.getCurrentYPos() + extention.getCurrentHeight()) {
					extention.handleMouseOverAt(x, y);
					return;
				}
			}
		} else {
			if(x > currentlyExtended.getCurrentXPos() && x < currentlyExtended.getCurrentXPos() + currentlyExtended.getCurrentWidth() && y > currentlyExtended.getCurrentYPos() && y < currentlyExtended.getCurrentYPos() + currentlyExtended.getCurrentHeight()) {
				currentlyExtended.handleMouseOverAt(x, y);
				return;
			}
		}
	}
}
